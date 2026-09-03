package com.situ.elder.controller.app;

import com.situ.elder.service.IChatService;
import com.situ.elder.utils.JwtUtil;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/app/chat")
public class ChatController {
    @Autowired
    private IChatService chatService;

    @PostMapping("/chat")
    public Result<String> chat(String message, @RequestHeader("Authorization") String token) {
        // 以老人id作为会话id，隔离不同用户的聊天记忆
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer conversationId = (Integer) map.get("id");
        return Result.ok("聊天成功", chatService.chat(message, conversationId));
    }

    //处理流式聊天请求，返回服务器发送事件（SSE）格式的响应流
    @PostMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(String message, @RequestHeader("Authorization") String token) {
        // 以老人id作为会话id，隔离不同用户的聊天记忆
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer conversationId = (Integer) map.get("id");
        return chatService.chatStream(message, conversationId);
    }
}
