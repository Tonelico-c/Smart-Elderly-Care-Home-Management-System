package com.situ.elder.pojo.vo;

import lombok.Data;

/**
 * 供 AI 工具（Tool Calling）返回给大模型的老人信息视图对象。
 * <p>
 * 为什么不用 Elder 实体或直接复用 ElderInfoVO：
 * 1. Elder 实体里有 password（密码哈希）、idCardNo（身份证号）等字段，
 *    而 Spring AI 会把工具返回值序列化成 JSON 发给第三方大模型，
 *    直接返回实体就等于把这些敏感信息一起发了出去；
 * 2. 这里只保留"老人自己会问到"的字段，遵循最小必要原则。
 * <p>
 * 后续如果新增字段，请先判断它是不是真的需要让大模型看到。
 */
@Data
public class ElderAiInfoVO {

    /**
     * 老人姓名
     */
    private String name;

    /**
     * 年龄（由出生日期计算，不是数据库字段）
     */
    private Integer age;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 出生日期（yyyy-MM-dd）
     */
    private String birthday;

    /**
     * 家庭住址
     */
    private String address;

    /**
     * 楼栋名称
     */
    private String buildingName;

    /**
     * 房间号
     */
    private String roomNo;

    /**
     * 床位号
     */
    private String bedNo;
}
