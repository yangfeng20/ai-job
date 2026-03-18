import {PlatformTypeEnum} from "../platform/platform";

export type User = {
    phone: string
    email: string
    resumeId: string
    aiSeatStatus: number,
    inviteCode: string,
    bindInviteCode: string,
    preference: PreferenceConfig
    preferenceMap: Map<PlatformTypeEnum, PreferenceConfig>

}

export type PreferenceConfig = {
    /**
     * 公司名包含开关
     */
    cniE: boolean,
    /**
     * 公司名包含
     */
    cni: string[],
    /**
     * 公司名排除开关
     */
    cneE: boolean,
    /**
     * 公司名排除
     */
    cne: string[],
    /**
     * 工作名包含开关
     */
    jniE: boolean,
    /**
     * 工作名包含
     */
    jni: string[],
    /**
     * 工作名排除开关
     */
    jneE: boolean,
    /**
     * 工作名排除
     */
    jne: string[],
    /**
     * 工作名内容排除开关
     */
    jceE: boolean,
    /**
     * 工作名内容排除
     */
    jce: string[],
    /**
     * 工作名内容包含开关
     */
    jciE: boolean,
    /**
     * 工作名内容包含
     */
    jci: string[],
    /**
     * 薪资范围开关
     */
    srE: boolean,
    /**
     * 薪资范围类型
     */
    srT: number,
    /**
     * 薪资范围
     */
    sr: string,
    /**
     * 公司规模范围开关
     */
    csrE: boolean,
    /**
     * 公司规模范围
     */
    csr: string,
    /**
     * 过滤猎头开关
     */
    fhE: boolean,
    /**
     * 仅投递在线boss
     */
    polE: boolean,
    /**
     * 发送自定义招呼语
     */
    cgE: boolean,
    /**
     * 自定义招呼语
     */
    cg: string,
    /**
     * ai过滤开关
     */
    afE: boolean,
    /**
     * ai过滤条件
     */
    af: string,
    /**
     * 发送自定义图片简历
     */
    cIE: boolean,
    /**
     * 自定义图片地址
     */
    cI: string,
    /**
     * 预设问题开关
     */
    ppE: boolean,
    /**
     * 预设问题
     */
    pp: string,
    /**
     * 拒绝挽留开关
     */
    rfE: boolean,
    /**
     * 拒绝挽留
     */
    rf: string,
    /**
     * 每轮邮件开启
     */
    ermE: boolean,
    /**
     * 延迟回复开关
     */
    drE: boolean,
    /**
     * 延迟回复
     */
    dr: number,
    /**
     * 高意向ai坐席停止开关
     */
    hiaE: boolean,
    /**
     * 对话聊天轮数 Chat rounds （高意向邮件开关） 开关
     */
    crE: boolean,
    /**
     * 对话聊天轮数 count
     */
    crC: string,
    /**
     * 对话聊天轮数 Key G关键字
     */
    crK: string[],
    /**
     * 投递间隔时间（秒）
     */
    pi: number,
    /**
     * 翻页间隔时间（秒）
     */
    npi: number,
}

/**
 * 回答类型枚举
 */
export enum JobSeekerClonedAnswerTypeEnum {
    MSG_TEXT = 1,
    BOSS_OPERATION = 2,
    STOP = 3,
    AI_SERVICE_EXCEPTION = 4,

}

/**
 * boss操作类型
 */
export enum BossOperationTypeEnum {
    SEND_RESUME = 1,
}
