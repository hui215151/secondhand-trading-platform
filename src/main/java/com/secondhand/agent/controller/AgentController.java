package com.secondhand.agent.controller;

import com.secondhand.agent.dto.AgentChatRequest;
import com.secondhand.agent.dto.AgentChatResponse;
import com.secondhand.agent.service.AgentService;
import com.secondhand.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * AI 助手对话接口（需登录，userId 由 JWT 拦截器注入）
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @PostMapping("/chat")
    public Result<AgentChatResponse> chat(HttpServletRequest request,
                                          @Validated @RequestBody AgentChatRequest chatRequest) {
        Long userId = (Long) request.getAttribute("userId");

        // 会话ID为空时开启新会话
        String sessionId = StringUtils.hasText(chatRequest.getSessionId())
                ? chatRequest.getSessionId()
                : UUID.randomUUID().toString().replace("-", "");

        return Result.success(agentService.chat(userId, sessionId, chatRequest.getMessage()));
    }
}
