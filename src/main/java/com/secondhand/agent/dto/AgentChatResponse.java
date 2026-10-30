package com.secondhand.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AgentChatResponse {

    private String sessionId;
    private String reply;

    /**
     * 本轮对话实际调用过的工具名，便于前端展示「AI 正在查询…」
     */
    private List<String> toolsUsed;
}
