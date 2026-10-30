package com.secondhand.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * AI 助手工具：一个工具 = 大模型可调用的一项平台能力。
 * 新能力只需实现本接口并注册为 Spring Bean，即可被大模型调用。
 */
public interface AgentTool {

    /** 工具名（大模型 function calling 的 name） */
    String name();

    /** 工具描述（告诉大模型什么时候该用它） */
    String description();

    /** 入参 JSON Schema（OpenAI function calling 格式） */
    Map<String, Object> parameters();

    /**
     * 执行工具
     * @param userId 当前登录用户ID（由服务端注入，不信任模型传入）
     * @param args   大模型生成的入参 JSON
     * @return 返回给大模型的结果（会被序列化为 JSON 字符串）
     */
    Object execute(Long userId, JsonNode args);
}
