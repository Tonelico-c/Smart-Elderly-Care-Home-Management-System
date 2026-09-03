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

    /**
     * AI健康分析：对指定体检报告生成通俗易懂的健康分析（流式返回）
     *
     * @param appointmentId 预约（体检报告）id
     * @param elderId       当前登录老人id（从token中解析，用于归属校验）
     * @throws ServiceException 报告不存在、不属于当前老人、体检未完成或暂无结果时抛出
     */
    Flux<String> analyzeExamReport(Long appointmentId, Long elderId);
}
