import {
    FetchJobBossFailExp,
    NotMatchException,
    PlatformError,
    PublishLimitExp,
    PublishStopExp,
    PushReqException
} from "../exp";
import {scrollElementToBottom, simulateScrollToEnd, TampermonkeyApi, Tools} from "./utils";
import logger, {LogLevel} from '../logging'
import axiosOriginal from "axios";
import {PushResultStatus, PushStatus} from "../enums";
import {Message} from "../webSocket/protobuf";
import {LogRecorder} from "../logging/record";
import {pushResultCount, UserStore} from "../stores";
import {userRemoteLoad} from "../stores/remote";
import {AiPower} from "./aiPower";

let pushResultCounter: any;
let userStore: any;


export enum PlatformTypeEnum {
    Boss,
    // ZhiLian,//智联
    // 前职无忧
    LiePin,
    UnKnow,
}

export interface ElementP {
    el: Element,
    p?: string
}


export interface Platform {
    name: string,
    urlList: string[],
    curUrl: string,

    getMountEle(): Promise<ElementP>;

    getRenderComponent(): any;

    startPush(): Promise<void>;

    pausePush(): void;

    getPlatformType(): PlatformTypeEnum;
}


export abstract class AbsPlatform implements Platform {
    abstract name: string;
    abstract curUrl: string;
    abstract urlList: string[];
    protected logRecorder: LogRecorder = new LogRecorder('recorder');

    protected pushStatus: PushStatus = PushStatus.NOT_START
    protected _pushMock: boolean = false;
    private _selfDefPushCountLimit = -1


    set pushMock(value: boolean) {
        this._pushMock = value;
    }

    set selfDefPushCountLimit(value: number) {
        this._selfDefPushCountLimit = value;
    }

    get selfDefPushCountLimit(): number {
        return this._selfDefPushCountLimit;
    }

    abstract getPlatformType(): PlatformTypeEnum;

    abstract getMountEle(): Promise<ElementP>;

    abstract getRenderComponent(): Promise<any>;

    async startPush() {
        this.logRecorder.info("开始投递")
        // 每次投递前清空单次成功计数器
        pushResultCounter.clearOnceSuccessCount()
        this.pushStatus = PushStatus.PUSHING;
        this.startPreHandler()
        do {
            // 获取jobDetail集合并过滤
            let jobList = this.getJobList();
            for (const jobDetail of jobList) {
                try {
                    this.preMatchJob();
                    await this.matchJob(jobDetail);
                    this.pushPreHandler(jobDetail);
                    const pushResult = await this.push(jobDetail);
                    await this.pushAfterHandler(pushResult, jobDetail);
                } catch (error) {
                    switch (true) {
                        case error instanceof NotMatchException:
                            if (this.logRecorder.getLogLevel() === LogLevel.Debug) {
                                this.logRecorder.info(`工作【${error.jobTitle}】被过滤 原因：${error.message} 当前值:${error.data}`)
                            } else {
                                this.logRecorder.info(`工作【${error.jobTitle}】被过滤 原因：${error.message}`)
                            }
                            pushResultCounter.notMatchIncr()
                            break;

                        case error instanceof PushReqException:
                            this.logRecorder.warn(`工作【${error.jobTitle}】投递失败 原因：${error.message}`)
                            pushResultCounter.failIncr()
                            break

                        case error instanceof FetchJobBossFailExp:
                            this.logRecorder.warn(`工作【${error.jobTitle}】发送自定义招呼语失败 原因：${error.message}`)
                            break

                        // 投递停止；手动停止.结束链路
                        case error instanceof PublishStopExp:
                            this.logRecorder.info("手动暂停投递 " + error.message)
                            return;
                        // 投递限制；平台限制.结束链路
                        case error instanceof PublishLimitExp:
                            this.logRecorder.info("停止投递 " + error.message)
                            return;

                        default:
                            logger.error("未捕获异常--->", error)
                    }
                }
            }
        } while (await this.next())
        this.logRecorder.info("结束投递")
    }

    next = async () => {
        let next = this.hasNext();
        if (!next) {
            this.logRecorder.info("无下一页数据")
            return false;
        }
        await Tools.sleep(userStore.user.preference.npi * 1000)
        this.acquireDataPre();
        await Tools.sleep(3000)
        return next
    };

    pausePush(): void {
    }

    abstract hasNext(): boolean;

    abstract acquireDataPre(): void;

    abstract startPreHandler(): void;
    abstract getJobList(): JobDetail[];

    abstract matchJob(jobDetail: JobDetail): Promise<boolean>;

    abstract pushPreHandler(jobDetail: JobDetail): JobDetail;

    preMatchJob(): void {
        // 投递前检查，避免无意义的匹配过滤
        if (this._selfDefPushCountLimit !== -1 && pushResultCounter.onceSuccessCount >= this._selfDefPushCountLimit) {
            throw new PublishLimitExp("自定义投递次数限制")
        }
        if (this.pushStatus == PushStatus.PAUSE) {
            throw new PublishStopExp("手动暂停投递")
        }
    }

    async push(jobDetail: JobDetail): Promise<PushResult> {
        if (this.pushStatus == PushStatus.PAUSE) {
            throw new PublishStopExp("手动暂停投递")
        }

        if (this._selfDefPushCountLimit !== -1 && pushResultCounter.onceSuccessCount >= this._selfDefPushCountLimit) {
            throw new PublishLimitExp("自定义投递次数限制")
        }

        // 检查投递限制
        let limitResult = this.isLimit(jobDetail);
        if (limitResult.limit) {
            throw new PublishLimitExp(limitResult.msg)
        }

        if (this._pushMock) {
            let jobTitle = this.getJobKey(jobDetail);
            logger.debug("mock投递 ", jobTitle)
            return {
                message: 'Success',
                code: 0
            }
        }
        return await this.doPush(jobDetail);
    }

    isLimit(jobDetail: JobDetail): { limit: boolean, msg: string } {
        return {
            limit: false,
            msg: this.getJobKey(jobDetail)
        }
    }

    abstract doPush(jobDetail: JobDetail): Promise<any>;

    abstract pushAfterHandler(pushResult: PushResult, jobDetail: JobDetail): Promise<any> ;

    abstract getJobKey(jobDetail: JobDetail): string;

    getFistJobDetail(): JobDetail {
        return this.getJobList()[0]
    }
}


class BossPlatform extends AbsPlatform {
    curUrl: string;
    name = "Boss";
    urlList = ["/web/geek", "overseas"];
    lastHeight = 0;


    constructor(curUrl: string) {
        super();
        this.curUrl = curUrl;
    }

    getPlatformType(): PlatformTypeEnum {
        return PlatformTypeEnum.Boss;
    }


    getMountEle(): Promise<ElementP> {
        return new Promise<ElementP>((resolve) => {
            let count: number = 0;
            let interval = setInterval(() => {
                let element: Element | null = null;
                let p = "";
                if (this.curUrl.includes("www.zhipin.com/web/geek/chat")) {
                    element = document.querySelector(".chat-conversation");
                }
                if (this.curUrl.includes("www.zhipin.com/web/geek/job-recommend")) {
                    element = document.querySelector(".recommend-search-inner");
                    // element = document.querySelector(".recommend-result-inner");
                    p = "end";
                }
                if (this.curUrl.includes("www.zhipin.com/web/geek/jobs")) {
                    element = document.querySelector(".job-recommend-result");
                    // p = "end";
                } else if (this.curUrl.includes("www.zhipin.com/web/geek/job")) {
                    element = document.querySelector(".page-job-inner");
                }

                if (this.curUrl.includes("overseas")) {
                    element = document.querySelector(".mod-header");
                }

                if (element !== null) {
                    clearInterval(interval);
                    return resolve({
                        el: element as Element,
                        p: p
                    })
                }
                if (count >= 3) {
                    clearInterval(interval);
                    logger.error(PlatformTypeEnum.Boss, "获取平台挂载元素失败")
                    return document.createElement("div")
                }
                count++;
            }, 300);
        })
    }


    async getRenderComponent(): Promise<any> {
        if (this.curUrl.includes("www.zhipin.com/web/geek/chat")) {
            let promise = import('../components/ui/BossMessage.vue');
            return promise.then(item => item.default)
        }
        if (this.curUrl.includes("www.zhipin.com/web/geek/job") || this.curUrl.includes("overseas")) {
            let promise = import('../components/ui/BossJobList.vue');
            return promise.then(item => item.default)
        }

    }

    startPreHandler(): void {
        this.lastHeight = 0;
    }

    getJobList(): BossJobDetail[] {
        // boss 推荐职位页面 或者jobs页面
        if (this.curUrl.includes("jobs")) {
            let elementNodeList = document.querySelectorAll<any>(".job-card-wrap");
            let jobList = Array.from(elementNodeList).map(item => item.__vue__.data).filter(job => !job.processed) as BossJobDetail[];
            if (elementNodeList.length != 0 && jobList.length == 0) {
                this.logRecorder.info("当前筛选条件下岗位均已投递")
            }
            return jobList;
        }
        if (this.curUrl.includes("job-recommend")) {
            let elementNodeList = document.querySelectorAll<any>(".job-card-wrap");
            return Array.from(elementNodeList).map(item => item.__vue__.data).filter(job => !job.contact) as BossJobDetail[];
        }
        if (this.curUrl.includes("overseas")) {
            let elementNodeList = document.querySelectorAll<any>(".job-card-box");
            return Array.from(elementNodeList).map(item => item.__vue__.data).filter(job => !job.contact) as BossJobDetail[];
        }
        let elementNodeList = document.querySelectorAll<any>(".job-card-wrapper");
        return Array.from(elementNodeList).map(item => item.__vue__.data) as BossJobDetail[];
    }

    hasNext(): boolean {
        logger.debug("hasNext")
        if (this.curUrl.includes("jobs")) {
            return this.lastHeight != document.querySelector(".job-list-container")?.scrollHeight
        }
        if (this.curUrl.includes("overseas")) {
            return this.lastHeight != document.querySelector(".job-list")?.scrollHeight
        }
        if (this.curUrl.includes("job-recommend")) {
            return !!document.querySelector("#footer");
        }
        let nextPageBtn = document.querySelector(".ui-icon-arrow-right") as any;
        if (nextPageBtn === null) {
            return false;
        }
        return nextPageBtn.parentElement.className !== "disabled";
    }

    acquireDataPre(): void {
        // 在等待下一页时点击了停止，不继续获取下一页数据
        if (this.pushStatus == PushStatus.PAUSE) {
            return;
        }
        if (this.curUrl.includes("jobs")) {
            this.lastHeight = document.querySelector(".job-list-container")?.scrollHeight as number
            simulateScrollToEnd().then(() => {
                logger.info("获取下一页成功")
            }).catch(e => {
                this.logRecorder.warn("获取下一页失败", e)
            })
            return;
        } else if (this.curUrl.includes("job-recommend")) {
            simulateScrollToEnd().then(() => {
                logger.info("获取下一页成功")
            }).catch(e => {
                this.logRecorder.warn("获取下一页失败", e)
            })
            return;
        }else if (this.curUrl.includes("overseas")) {
            this.lastHeight = document.querySelector(".job-list")?.scrollHeight as number
            simulateScrollToEnd().then(() => {
                logger.info("获取下一页成功")
            }).catch(e => {
                this.logRecorder.warn("获取下一页失败", e)
            })
            return;
        }
        // 点击下一页
        document.querySelector<any>(".ui-icon-arrow-right").click();
    }


    async matchJob(jobDetail: BossJobDetail) {
        // 标记为已处理
        jobDetail.processed = true
        const jobTitle = this.getJobKey(jobDetail)
        // 已经沟通过
        if (jobDetail.contact) {
            throw new NotMatchException(jobTitle, jobDetail.contact, '已经沟通过')
        }
        // 过滤猎头
        if (userStore.user.preference.fhE && jobDetail.goldHunter === 1) {
            throw new NotMatchException(jobTitle, jobDetail.goldHunter, '过滤猎头')
        }
        // 仅投递在线boss
        if (userStore.user.preference.polE && !jobDetail.bossOnline) {
            throw new NotMatchException(jobTitle, jobDetail.bossOnline, '仅投递在线boss')
        }

        // 不满足配置公司名
        let companyNameInclude: string[] = userStore.user.preference.cni;
        if (userStore.user.preference.cniE && !Tools.fuzzyMatch(companyNameInclude, jobDetail.brandName, true)) {
            throw new NotMatchException(jobTitle, jobDetail.brandName, '不满足配置公司名')
        }

        // 满足排除公司名
        let companyNameExclude: string[] = userStore.user.preference.cne;
        if (userStore.user.preference.cneE && Tools.fuzzyMatch(companyNameExclude, jobDetail.brandName, false)) {
            throw new NotMatchException(jobTitle, jobDetail.brandName, '满足排除公司名')
        }

        // 不满足配置工作名
        let jobNameInclude: string[] = userStore.user.preference.jni;
        if (userStore.user.preference.jniE && !Tools.fuzzyMatch(jobNameInclude, jobDetail.jobName, true)) {
            throw new NotMatchException(jobTitle, jobDetail.jobName, '不满足配置工作名')
        }

        // 满足排除工作名
        let jobNameExclude: string[] = userStore.user.preference.jne;
        if (userStore.user.preference.jneE && Tools.fuzzyMatch(jobNameExclude, jobDetail.jobName, false)) {
            throw new NotMatchException(jobTitle, jobDetail.jobName, '满足排除工作名')
        }

        // 不满足薪资范围 薪资类型（实习中的天维度）不需要特殊处理
        let pageSalaryRange = jobDetail.salaryDesc.split(".")[0];
        if (userStore.user.preference.srE && !Tools.isRangeOverlap(userStore.user.preference.sr, pageSalaryRange)) {
            throw new NotMatchException(jobTitle, pageSalaryRange, '不满足薪资范围')
        }

        // 公司规模
        let pageCompanyScaleRange = userStore.user.preference.csr;
        if (userStore.user.preference.csrE && !Tools.isRangeOverlap(pageCompanyScaleRange, jobDetail.brandScaleName)) {
            throw new NotMatchException(jobTitle, jobDetail.brandScaleName, '不满足公司规模范围')
        }

        // 通过接口获取工作详情扩展信息
        let jobDetailExt = await this.obtainBossJobDetailExt(jobDetail);
        logger.debug(`获取工作【${jobTitle}】详情扩展信息用于过滤 `, jobDetail)

        //  活跃度
        let activeTimeDesc = jobDetailExt.activeTimeDesc;
        if (!this.bossIsActive(activeTimeDesc)) {
            throw new NotMatchException(jobTitle, activeTimeDesc, '不满足活跃度检查')
        }

        // 工作内容排除
        let jobContent = jobDetailExt.postDescription;
        let jobContentExclude: string[] = userStore.user.preference.jce;
        if (userStore.user.preference.jceE && Tools.fuzzyMatch(jobContentExclude, jobContent, false)) {
            throw new NotMatchException(jobTitle, jobContent, '满足排除工作内容')
        }

        // 工作内容包含
        let jobContentInclude: string[] = userStore.user.preference.jci;
        if (userStore.user.preference.jciE && !Tools.fuzzyMatch(jobContentInclude, jobContent, true)) {
            throw new NotMatchException(jobTitle, jobContent, '不满足工作内容')
        }

        // ai过滤
        if (userStore.user.preference.afE && userStore.user.preference.af) {
            let filterResp = await AiPower.filter(userStore.user.preference.af, JSON.stringify(this.unpackBaseInfo(jobDetail)), JSON.stringify(this.unpackExtInfo(jobDetailExt)));
            let filterResult = filterResp ? filterResp?.data?.data : null;
            if (filterResult && filterResult?.filter) {
                throw new NotMatchException(jobTitle, filterResult.reason, 'AI过滤')
            }
        }

        // 重新检测投递（页面点击投递后，页面数据不会变，所有标签中获取到的是否沟通过有可能是旧的，需要重新校验）
        if (this.isCommunication(jobDetailExt)) {
            throw new NotMatchException(jobTitle, jobDetailExt.friendStatus, '已经沟通过')
        }

        return true;
    }

    unpackBaseInfo(jobDetail: BossJobDetail): {} {
        return {
            jobName: jobDetail.jobName,
            salaryDesc: jobDetail.salaryDesc,
            jobLabels: jobDetail.jobLabels,
            skills: jobDetail.skills,
            jobExperience: jobDetail.jobExperience,
            jobDegree: jobDetail.jobDegree,
            cityName: jobDetail.cityName,
            areaDistrict: jobDetail.areaDistrict,
            businessDistrict: jobDetail.businessDistrict,
            brandName: jobDetail.brandName,
            brandStageName: jobDetail.brandStageName,
            brandIndustry: jobDetail.brandIndustry,
            brandScaleName: jobDetail.brandScaleName,
            welfareList: jobDetail.welfareList,
        }
    }
    unpackExtInfo(jobDetailExt: any): {} {
        return {
            postDescription: jobDetailExt.postDescription,
            address: jobDetailExt.address,
            activeTimeDesc: jobDetailExt.activeTimeDesc
        }
    }

    pausePush() {
        this.pushStatus = PushStatus.PAUSE
    }

    getJobKey(jobDetail: BossJobDetail): string {
        return jobDetail.jobName + "-" + jobDetail.cityName + jobDetail.areaDistrict + jobDetail.businessDistrict;
    }


    isLimit(jobDetail: JobDetail): { limit: boolean; msg: string } {
        return {
            limit: TampermonkeyApi.GmGetValue(TampermonkeyApi.PUSH_LIMIT, false),
            msg: "Boss投递限制每天100次"
        }
    }

    async doPush(jobDetail: BossJobDetail, errorMsg = '', retries = 3): Promise<any> {
        const jobTitle = this.getJobKey(jobDetail)

        if (retries === 3) {
            logger.debug("正在投递：" + jobTitle)
        }

        // 重试结束；抛出异常结束
        if (retries === 0) {
            throw new PushReqException(jobTitle, errorMsg)
        }

        // 投递请求url
        let publishUrl = `https://www.zhipin.com/wapi/zpgeek/friend/add.json?securityId=` +
            `${jobDetail.securityId}&jobId=${jobDetail.encryptJobId}&lid=${jobDetail.lid}`

        let pushResp: any = {code: PushResultStatus.NOT_START, message: ""};
        try {
            // 避免频繁，每次投递前延时
            await Tools.sleep(userStore.user.preference.pi * 1000)
            pushResp = await axiosOriginal.post(publishUrl, null, {headers: {"Zp_token": Tools.getCookieValue("bst")}});
        } catch (error: any) {
            // 重试投递
            logger.debug(`工作【${jobTitle}】投递失败; 正在等待重试; 原因：${error.message}`)
            await Tools.sleep(800);
            return await this.doPush(jobDetail, error.message, retries - 1);
        }

        if (pushResp.data.code === PushResultStatus.FAIL && pushResp.data?.zpData?.bizData?.chatRemindDialog?.content) {
            // 过滤开聊提示，实际投递成功
            if (pushResp.data?.zpData?.bizData?.chatRemindDialog?.content.include("您今天已与120位BOSS沟通")) {
                logger.debug(`当天已投递超过120次 工作【${jobTitle}】已修正为投递成功`)
                return {
                    code: PushResultStatus.SUCCESS,
                    message: "Success"
                }
            }
            // 某些条件不满足，boss限制投递，无需重试，在结果处理器中处理
            return {
                code: 1,
                message: pushResp.data?.zpData?.bizData?.chatRemindDialog?.content
            }
        }
        // 避免频繁
        await Tools.sleep(800);
        return pushResp.data;
    }

    private bossDataCache: Map<string, any> = new Map();

    private async requestBossDataByCache(jobDetail: BossJobDetail): Promise<any> {
        let cacheKey = `${jobDetail.encryptBossId}-${jobDetail.securityId}`;

        // 先检查缓存中是否有数据
        if (this.bossDataCache.has(cacheKey)) {
            return this.bossDataCache.get(cacheKey);
        }

        // 缓存请求结果
        const result = await this.requestBossData(jobDetail);
        this.bossDataCache.set(cacheKey, result);
        return result;
    }

    async requestBossData(jobDetail: BossJobDetail, errorMsg: string = "", retries = 3): Promise<any> {
        let jobTitle = this.getJobKey(jobDetail);

        if (retries === 0) {
            throw new FetchJobBossFailExp(jobTitle, errorMsg || "获取boss数据重试多次失败");
        }
        const url = "https://www.zhipin.com/wapi/zpchat/geek/getBossData";
        const token = Tools.getCookieValue("bst");
        if (!token) {
            throw new FetchJobBossFailExp(jobTitle, "未获取到zp-token");
        }

        const data = new FormData();
        data.append("bossId", jobDetail.encryptBossId);
        data.append("securityId", jobDetail.securityId);
        data.append("bossSrc", "0");

        let resp: any;
        try {
            resp = await axiosOriginal({url, data: data, method: "POST", headers: {Zp_token: token}});
        } catch (e: any) {
            return this.requestBossData(jobDetail, e.message, retries - 1);
        }

        if (resp.data.code !== 0) {
            throw new FetchJobBossFailExp(jobTitle, resp.data.message);
        }
        return resp.data.zpData
    }


    async pushAfterHandler(pushResult: PushResult, jobDetail: BossJobDetail): Promise<any> {
        const jobTitle = this.getJobKey(jobDetail)

        if (pushResult.message === 'Success' && pushResult.code === 0) {
            pushResultCounter.successIncr()
            this.logRecorder.info(`工作【${jobTitle}】 投递成功`)

            try {
                // 投递后发送自定义图片
                await this.pushAfterSendImage(jobDetail);
            } catch (e) {
            }
            try {
                // 投递后发送自定义消息
                await this.pushAfterSendMsg(jobDetail);
            } catch (e) {
            }

            // 标记为已沟通，在推荐页面中下一页会获取之前的数据，所以需要标记为已沟通
            jobDetail.contact = true
            return jobDetail
        }

        if (pushResult.message.includes("今日沟通人数已达上限")) {
            throw new PublishLimitExp(pushResult.message)
        }
        throw new PushReqException(jobTitle, pushResult.message)
    }

    /**
     * 投递后发送自定义消息
     */
    async pushAfterSendMsg(jobDetail: BossJobDetail) {
        if (!userStore.user.preference.cgE || this._pushMock) {
            return;
        }
        let bossData = await this.requestBossDataByCache(jobDetail);

        let customGreeting = userStore.user.preference.cg;
        // 发送websocket消息
        let message = new Message({
            form_uid: Tools.window._PAGE.uid.toString(),
            to_uid: bossData.data.bossId.toString(),
            to_name: jobDetail.encryptBossId,
            content: customGreeting,
            image: undefined,
        });
        message.send()
    }

    /**
     * 投递后发送自定义图片
     */
    async pushAfterSendImage(jobDetail: BossJobDetail) {
        if (!userStore.user.preference.cIE || this._pushMock) {
            return;
        }
        let bossData = await this.requestBossDataByCache(jobDetail);
        let customerImageSet = userStore.user.preference.cI;
        if (!customerImageSet) {
            return;
        }
        let message = new Message({
            form_uid: Tools.window._PAGE.uid.toString(),
            to_uid: bossData.data.bossId.toString(),
            to_name: jobDetail.encryptBossId,
            content: "",
            image: {
                originImage: customerImageSet.split("===")[0],
                tinyImage: customerImageSet.split("===")[1],
            },
        });
        message.send()
    }

    pushPreHandler(jobDetail: JobDetail): JobDetail {
        return jobDetail;
    }

    async obtainBossJobDetailExt(jobDetail: BossJobDetail, message = '', retries = 3): Promise<any> {

        if (retries === 0) {
            logger.warn(`获取工作详情扩展信息异常,用于活跃度过滤以及工作内容过滤; 原因：${message}`)
            throw new NotMatchException(this.getJobKey(jobDetail), message, "获取工作详情扩展信息异常")
        }
        let params = `lid=${jobDetail.lid}&securityId=${jobDetail.securityId}&sessionId=`
        try {
            let resp = await axiosOriginal.get("https://www.zhipin.com/wapi/zpgeek/job/card.json?" + params, {timeout: 5000})
            return resp.data.zpData.jobCard
        } catch (error: any) {
            logger.debug("获取详情页异常正在重试:", error)
            return this.obtainBossJobDetailExt(jobDetail, error.message, retries - 1)
        }
    }

    bossIsActive(activeText: string) {
        return !(activeText.includes("月") || activeText.includes("年") || activeText.includes("周"));
    }

    isCommunication(jobCardJson: any) {
        return jobCardJson?.friendStatus === 1;
    }


}

const platformList: any[] = [BossPlatform]


class PlatformFactory {
    public static getInstance(url: string): Platform {
        for (const PlatformClass of platformList) {
            const platformInstance = new PlatformClass(url) as Platform;
            if (platformInstance.urlList.some(platformUrl => url.includes(platformUrl))) {
                // 计数器赋值
                pushResultCounter = pushResultCount();
                userStore = UserStore();
                userStore.platformType = platformInstance.getPlatformType();
                userRemoteLoad()
                return platformInstance;
            }
        }
        throw new PlatformError(PlatformTypeEnum.UnKnow, "错误的平台");
    }
}


export default PlatformFactory
