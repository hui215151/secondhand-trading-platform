package com.secondhand.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具注册器：收集所有 AgentTool，生成 OpenAI function calling 的 tools 定义并负责分发执行。
 */
@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, AgentTool> toolMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ToolRegistry(List<AgentTool> tools) {
        for (AgentTool tool : tools) {
            toolMap.put(tool.name(), tool);
            log.info("注册 AI 工具：{}", tool.name());
        }
    }

    /** 生成传给大模型的 tools 数组 */
    public List<Map<String, Object>> toolDefinitions() {
        List<Map<String, Object>> definitions = new ArrayList<>();
        for (AgentTool tool : toolMap.values()) {
            Map<String, Object> function = new HashMap<>();
            function.put("name", tool.name());
            function.put("description", tool.description());
            function.put("parameters", tool.parameters());

            Map<String, Object> definition = new HashMap<>();
            definition.put("type", "function");
            definition.put("function", function);
            definitions.add(definition);
        }
        return definitions;
    }

    /** 按名称执行工具；未知工具返回错误信息而不是抛异常，避免打断对话 */
    public Object execute(String name, Long userId, String argumentsJson) {
        AgentTool tool = toolMap.get(name);
        if (tool == null) {
            return errorResult("未知工具：" + name);
        }
        try {
            JsonNode args = argumentsJson == null || argumentsJson.isEmpty()
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(argumentsJson);
            Object result = tool.execute(userId, args);
            return result == null ? "无数据" : result;
        } catch (Exception e) {
            log.error("工具执行失败：{}，{}", name, e.getMessage());
            return errorResult("工具执行失败：" + e.getMessage());
        }
    }

    private Map<String, String> errorResult(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("error", message);
        return map;
    }
}
