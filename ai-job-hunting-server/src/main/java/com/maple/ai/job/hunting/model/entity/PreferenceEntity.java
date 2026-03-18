package com.maple.ai.job.hunting.model.entity;

import lombok.Data;

import java.util.List;

/**
 * 偏好设置实体类。
 *
 * @author maple
 * @since 2024/09/02
 */

@Data
public class PreferenceEntity {

    /**
     * 公司名包含开关
     */
    private boolean cniE;

    /**
     * 公司名包含
     */
    private List<String> cni;

    /**
     * 公司名排除开关
     */
    private boolean cneE;

    /**
     * 公司名排除
     */
    private List<String> cne;

    /**
     * 工作名包含开关
     */
    private boolean jniE;

    /**
     * 工作名包含
     */
    private List<String> jni;

    /**
     * 工作名内容排除开关
     */
    private boolean jceE;

    /**
     * 工作名内容排除
     */
    private List<String> jce;

    /**
     * 薪资范围开关
     */
    private boolean srE;

    /**
     * 薪资范围类型
     */
    private int srT;

    /**
     * 薪资范围
     */
    private String sr;

    /**
     * 公司规模范围开关
     */
    private boolean csrE;

    /**
     * 公司规模范围
     */
    private String csr;

    /**
     * 过滤猎头开关
     */
    private boolean fhE;

    /**
     * 发送自定义招呼语
     */
    private boolean cgE;

    /**
     * 自定义招呼语
     */
    private String cg;

    /**
     * 预设问题开关
     */
    private boolean ppE;

    /**
     * 预设问题
     */
    private String pp;

    /**
     * 拒绝挽留开关
     */
    private boolean rfE;

    /**
     * 拒绝挽留
     */
    private String rf;

    /**
     * 每轮邮件开启
     */
    private boolean ermE;

    /**
     * 高意向ai坐席停止开关
     */
    private boolean hiaE;

    /**
     * 对话聊天轮数 Chat rounds （高意向邮件开关） 开关
     */
    private boolean crE;

    /**
     * 对话聊天轮数 count
     */
    private Integer crC;

    /**
     * 对话聊天轮数 Key 关键字
     */
    private List<String> crK;
}