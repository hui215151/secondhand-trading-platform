package com.secondhand.agent.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AgentChatRequest {

    /**
     * 会话ID：同一会话保留上下文；为空时由服务端生成新会话
     */
    private String sessionId;

    @NotBlank(message = "消息不能为空")
    private String message;
}
