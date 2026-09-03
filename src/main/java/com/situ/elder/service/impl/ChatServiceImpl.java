package com.situ.elder.service.impl;

import com.situ.elder.exception.ServiceException;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.ExamAppointment;
import com.situ.elder.pojo.entity.ExamPackage;
import com.situ.elder.pojo.vo.ExamAppointmentItemVO;
import com.situ.elder.service.IChatService;
import com.situ.elder.service.IElderService;
import com.situ.elder.service.IExamAppointmentService;
import com.situ.elder.service.IExamPackageService;
import com.situ.elder.tools.ElderTools;
import com.situ.elder.tools.ExamAppointmentTools;
import com.situ.elder.tools.TimeTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl implements IChatService {
    private static final String STREAM_END_MARK = "[END]";
    private static final String EMPTY_INPUT_REPLY = "请输入您想咨询的问题";
    private static final String ERROR_REPLY = "抱歉，小智暂时无法回复，请稍后再试。";

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private IElderService elderService;
    @Autowired
    private IExamAppointmentService examAppointmentService;
    @Autowired
    private IExamPackageService examPackageService;

    @Override
    public String chat(String message, Integer conversationId) {
        if (ObjectUtils.isEmpty(message)) {
            return EMPTY_INPUT_REPLY;
        }
        try {
            return chatClient.prompt()
                    .user(message)
                    // 会话记忆按 conversationId（老人id）隔离，避免不同用户上下文串扰
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .tools(new ElderTools(conversationId,elderService))
                    .tools(new ExamAppointmentTools(conversationId, examAppointmentService))
                    .tools(new TimeTools())
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI调用失败, conversationId: {}", conversationId, e);
            return ERROR_REPLY;
        }
    }

    @Override
    public Flux<String> chatStream(String message, Integer conversationId) {
        if (ObjectUtils.isEmpty(message)) {
            return Flux.just(EMPTY_INPUT_REPLY, STREAM_END_MARK);
        }
        return chatClient.prompt()
                .user(message)
                // 会话记忆按 conversationId（老人id）隔离，避免不同用户上下文串扰
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(new ElderTools(conversationId,elderService))
                .tools(new ExamAppointmentTools(conversationId, examAppointmentService))
                .tools(new TimeTools())
                .stream()
                .content()
                // 模型调用失败时也要发出提示和结束标记，避免前端一直处于等待状态
                .onErrorResume(e -> {
                    log.error("AI流式调用失败, conversationId: {}", conversationId, e);
                    return Flux.just(ERROR_REPLY, STREAM_END_MARK);
                })
                // 在流结束时添加结束标记
                .concatWith(Flux.just(STREAM_END_MARK));
    }

    /**
     * AI健康分析：对指定体检报告生成通俗易懂的健康分析（流式返回）
     * <p>
     * 整体流程：
     * <ol>
     *     <li>校验报告存在、属于当前老人（不能分析他人的报告）、体检状态为已完成；</li>
     *     <li>查出报告的项目明细，把老人姓名、套餐名称、体检日期和每项的
     *         结果/参考范围/是否异常拼成提示词，交给大模型分析；
     *         没有结果的项目（待检查/未完成）不纳入分析；</li>
     *     <li>以流式返回分析内容。分析结果同时写入该老人的会话记忆，
     *         老人后续在智能咨询中可以针对这次分析继续追问。</li>
     * </ol>
     *
     * @param appointmentId 预约（体检报告）id
     * @param elderId       当前登录老人 id（从 token 中解析，用于归属校验）
     * @throws ServiceException 报告不存在、不属于当前老人、体检未完成或暂无结果时抛出
     */
    @Override
    public Flux<String> analyzeExamReport(Long appointmentId, Long elderId) {
        // 1. 校验报告存在且属于当前老人
        ExamAppointment examAppointment = examAppointmentService.getById(appointmentId);
        if (examAppointment == null) {
            throw new ServiceException("体检报告不存在");
        }
        if (!examAppointment.getElderId().equals(elderId)) {
            throw new ServiceException("无权查看他人的体检报告");
        }
        if (examAppointment.getStatus() == null || examAppointment.getStatus() != 2) {
            throw new ServiceException("该体检尚未完成，暂无体检结果可分析");
        }

        // 2. 查询报告明细并组装提示词
        List<ExamAppointmentItemVO> itemList = examAppointmentService.listItems(appointmentId);
        Elder elder = elderService.getById(elderId);
        ExamPackage examPackage = examPackageService.getById(examAppointment.getPackageId());

        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下体检报告数据，为老人生成一份通俗易懂的健康分析。\n\n");
        prompt.append("【基本信息】\n");
        prompt.append("体检人：").append(elder != null ? elder.getName() : "未知").append('\n');
        prompt.append("体检套餐：").append(examPackage != null ? examPackage.getName() : "未知").append('\n');
        prompt.append("体检日期：").append(examAppointment.getAppointmentDate())
                .append(' ').append(examAppointment.getAppointmentTime()).append('\n');
        prompt.append("\n【体检项目结果】\n");

        int index = 0;
        for (ExamAppointmentItemVO item : itemList) {
            String result = formatItemResult(item);
            //没有结果的项目（待检查/未完成）不纳入分析
            if (result == null) {
                continue;
            }
            index++;
            prompt.append(index).append(". ").append(item.getItemName()).append("：").append(result);
            String reference = formatItemReference(item);
            if (reference != null) {
                prompt.append("（参考范围：").append(reference).append("）");
            }
            if (item.getAbnormal() != null && item.getAbnormal() == 1) {
                prompt.append("——异常");
            }
            if (!ObjectUtils.isEmpty(item.getRemark())) {
                prompt.append("，备注：").append(item.getRemark());
            }
            prompt.append('\n');
        }
        if (index == 0) {
            throw new ServiceException("该报告暂无体检结果，无法分析");
        }

        prompt.append("\n【分析要求】\n");
        prompt.append("1. 先用1~2句话总结这次体检的整体情况；\n");
        prompt.append("2. 逐项点评异常指标，解释其含义和可能的影响，语言温和，不制造恐慌；\n");
        prompt.append("3. 针对异常指标给出日常生活建议（饮食、运动、作息、监测等）；\n");
        prompt.append("4. 如有需要复诊或咨询医生的情况，明确指出；\n");
        prompt.append("5. 全文使用短句，适合老年人阅读。");

        // 3. 流式返回分析结果，会话记忆按老人id隔离
        return chatClient.prompt()
                .user(prompt.toString())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, elderId))
                .stream()
                .content()
                .onErrorResume(e -> {
                    log.error("AI健康分析调用失败, appointmentId: {}", appointmentId, e);
                    return Flux.just(ERROR_REPLY);
                })
                .concatWith(Flux.just(STREAM_END_MARK));
    }

    /**
     * 格式化单个项目的体检结果
     *
     * @param item 体检项目明细
     * @return 数值型返回"数值 单位"，文本型返回文本内容；尚无结果时返回 null
     */
    private String formatItemResult(ExamAppointmentItemVO item) {
        if (item.getResultType() != null && item.getResultType() == 1) {
            if (item.getResultValue() == null) {
                return null;
            }
            //优先用明细上的单位快照，快照缺失时用项目当前单位
            String unit = ObjectUtils.isEmpty(item.getResultUnit()) ? item.getUnit() : item.getResultUnit();
            return item.getResultValue() + (ObjectUtils.isEmpty(unit) ? "" : " " + unit);
        }
        return ObjectUtils.isEmpty(item.getResultText()) ? null : item.getResultText();
    }

    /**
     * 格式化单个项目的参考范围
     *
     * @param item 体检项目明细
     * @return 形如"90~139 mmHg"；没有参考范围时返回 null
     */
    private String formatItemReference(ExamAppointmentItemVO item) {
        if (item.getReferenceMin() == null && item.getReferenceMax() == null) {
            return null;
        }
        String unit = ObjectUtils.isEmpty(item.getReferenceUnit()) ? "" : " " + item.getReferenceUnit();
        return (item.getReferenceMin() != null ? item.getReferenceMin() : "-")
                + "~"
                + (item.getReferenceMax() != null ? item.getReferenceMax() : "-")
                + unit;
    }
}
