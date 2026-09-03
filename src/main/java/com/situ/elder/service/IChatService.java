package com.situ.elder.service;

import reactor.core.publisher.Flux;

public interface IChatService {
    /**
     * @param conversationId 会话id（老人id），用于隔离不同用户的上下文记忆
     */
    String chat(String message, Integer conversationId);

    /**
     * @param conversationId 会话id（老人id），用于隔离不同用户的上下文记忆
     */
    Flux<String> chatStream(String message, Integer conversationId);
}
