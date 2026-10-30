package com.secondhand.agent.service;

import com.secondhand.agent.dto.AgentChatResponse;
import com.secondhand.agent.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 助手核心服务：维护会话上下文，循环调用大模型，
 * 当模型要求调用工具时本地执行并把结果回传，直到模型给出最终回复。
 */
@Slf4j
@Service
public class AgentService {

    @Autowired
    private ToolRegistry toolRegistry;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("agentRestTemplate")
    private RestTemplate restTemplate;

    @Value("${agent.base-url}")
    private String baseUrl;

    @Value("${agent.api-key}")
    private String apiKey;

    @Value("${agent.model}")
    private String model;

    @Value("${agent.max-rounds:5}")
    private int maxRounds;

    private static final String SESSION_KEY_PREFIX = "agent:chat:";
    private static final long SESSION_TTL_MINUTES = 30;
    /** 最多保留最近 20 条对话（不含 system），防止上下文无限膨胀 */
    private static final int MAX_HISTORY = 20;

    private static final String SYSTEM_PROMPT =
            "你是校园二手交易平台的 AI 助手。平台有两类业务：\n" +
            "1）闲置交易：用户之间买卖二手商品；\n" +
            "2）限时秒杀：热销商品在活动期间以秒杀价限量抢购。\n" +
            "你可以调用工具查询在售商品、商品详情、秒杀活动、用户订单和秒杀结果。\n" +
            "回答要求：简洁友好，金额带单位元；查不到数据时如实说明并给出建议；\n" +
            "不要编造商品或订单信息，数据一律以工具返回为准。";

    @SuppressWarnings("unchecked")
    public AgentChatResponse chat(Long userId, String sessionId, String message) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("你的")) {
            throw new RuntimeException("AI 助手未配置 API Key，请先在 application.yml 配置 agent.api-key");
        }

        String redisKey = SESSION_KEY_PREFIX + userId + ":" + sessionId;
        Object cached = redisTemplate.opsForValue().get(redisKey);
        List<Map<String, Object>> messages = cached instanceof List
                ? new ArrayList<>((List<Map<String, Object>>) cached)
                : new ArrayList<>();

        if (messages.isEmpty()) {
            Map<String, Object> system = new HashMap<>();
            system.put("role", "system");
            system.put("content", SYSTEM_PROMPT);
            messages.add(system);
        }

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", message);
        messages.add(userMsg);

        List<String> toolsUsed = new ArrayList<>();
        String reply = "抱歉，我暂时无法回答这个问题";

        for (int round = 0; round < maxRounds; round++) {
            Map<String, Object> assistant = callLlm(messages);

            Object toolCallsObj = assistant.get("tool_calls");
            if (toolCallsObj instanceof List && !((List<?>) toolCallsObj).isEmpty()) {
                // 模型要求调用工具：先记录 assistant 消息，再逐个执行工具并回填结果
                messages.add(assistant);
                for (Object item : (List<?>) toolCallsObj) {
                    Map<String, Object> toolCall = (Map<String, Object>) item;
                    String callId = String.valueOf(toolCall.get("id"));
                    Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
                    String name = String.valueOf(function.get("name"));
                    String arguments = String.valueOf(function.get("arguments"));

                    toolsUsed.add(name);
                    log.info("AI 调用工具：{}，参数：{}", name, arguments);
                    Object result = toolRegistry.execute(name, userId, arguments);

                    Map<String, Object> toolMsg = new HashMap<>();
                    toolMsg.put("role", "tool");
                    toolMsg.put("tool_call_id", callId);
                    toolMsg.put("content", toJson(result));
                    messages.add(toolMsg);
                }
            } else {
                // 模型给出最终回复
                Object content = assistant.get("content");
                if (content != null) {
                    reply = String.valueOf(content);
                }
                Map<String, Object> finalMsg = new HashMap<>();
                finalMsg.put("role", "assistant");
                finalMsg.put("content", reply);
                messages.add(finalMsg);
                break;
            }
        }

        saveSession(redisKey, messages);
        return new AgentChatResponse(sessionId, reply, toolsUsed);
    }

    /** 调用 OpenAI 兼容的 chat/completions 接口，返回 choices[0].message */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callLlm(List<Map<String, Object>> messages) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("tools", toolRegistry.toolDefinitions());
        body.put("temperature", 0.3);
        body.put("max_tokens", 1024);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> response = restTemplate.postForObject(
                baseUrl + "/chat/completions",
                new HttpEntity<>(body, headers),
                Map.class
        );

        if (response == null || !(response.get("choices") instanceof List)
                || ((List<?>) response.get("choices")).isEmpty()) {
            throw new RuntimeException("AI 服务返回异常，请稍后重试");
        }

        Map<String, Object> choice = (Map<String, Object>) ((List<?>) response.get("choices")).get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");
        if (message == null) {
            throw new RuntimeException("AI 服务返回异常，请稍后重试");
        }
        return message;
    }

    /** 会话写回 Redis：system 永远保留，其余只留最近 20 条，TTL 30 分钟 */
    private void saveSession(String redisKey, List<Map<String, Object>> messages) {
        List<Map<String, Object>> trimmed = new ArrayList<>();
        if (!messages.isEmpty() && "system".equals(messages.get(0).get("role"))) {
            trimmed.add(messages.get(0));
        }
        int from = Math.max(trimmed.size(), messages.size() - MAX_HISTORY);
        trimmed.addAll(messages.subList(from, messages.size()));
        redisTemplate.opsForValue().set(redisKey, trimmed, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
    }

    private String toJson(Object result) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(result);
        } catch (Exception e) {
            return String.valueOf(result);
        }
    }
}
