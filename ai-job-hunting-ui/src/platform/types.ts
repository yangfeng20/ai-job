interface JobDetail {

    getJobKey(): string;

}

interface BossJobDetail extends JobDetail {
    securityId: string
    bossAvatar: string
    bossCert: number
    encryptBossId: string
    bossName: string
    bossTitle: string
    goldHunter: number
    /**
     * boss是否在线
     */
    bossOnline: boolean
    encryptJobId: string
    expectId: number
    jobName: string
    lid: string
    salaryDesc: string
    jobLabels: Array<string>
    jobValidStatus: number
    iconWord: string
    skills: Array<string>
    jobExperience: string
    daysPerWeekDesc: string
    leastMonthDesc: string
    jobDegree: string
    cityName: string
    areaDistrict: string
    businessDistrict: string
    jobType: number
    proxyJob: number
    proxyType: number
    anonymous: number
    outland: number
    optimal: number
    iconFlagList: Array<unknown>
    itemId: number
    city: number
    isShield: number
    atsDirectPost: boolean
    gps: null
    lastModifyTime: number
    encryptBrandId: string
    brandName: string
    brandLogo: string
    brandStageName: string
    brandIndustry: string
    brandScaleName: string
    welfareList: Array<string>
    industry: number
    contact: boolean
    processed: boolean,
}

interface PushResult {
    code: number,
    message: string,
}

/**
 * boss招聘者信息
 */
interface BossUserInfo {
    /**
     * uid
     */
    bossId: number,
    /**
     * encryptUid
     */
    encryptBossId: string,
    /**
     * 标识工作岗位的id
     */
    securityId: string,
    /**
     * encryptJobId
     */
    encryptJobId: number,

    jobTitle: string,

}
