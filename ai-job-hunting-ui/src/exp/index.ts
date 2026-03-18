import {PlatformTypeEnum} from "../platform/platform";

export class AIJobHuntingError extends Error {
    constructor(message: string) {
        super(message);
    }
}

export class PlatformError extends AIJobHuntingError {
    private platform: PlatformTypeEnum;

    constructor(platformType: PlatformTypeEnum, message: string) {
        super(message);
        this.platform = platformType
    }
}

/**
 * 投递异常
 */
export class PushException extends AIJobHuntingError {

}

/**
 * 投递是不匹配异常
 */
export class NotMatchException extends PushException {
    jobTitle: string;
    data: any;


    constructor(jobTitle: string, data: any, message: string = '') {
        super(message);
        this.jobTitle = jobTitle;
        this.data = data;
    }
}

/**
 * 投递接口请求异常
 */
export class PushReqException extends PushException {
    jobTitle: string;


    constructor(jobTitle: string, message: string = '') {
        super(message);
        this.jobTitle = jobTitle;
    }

}


export class FetchJobBossFailExp extends PushException {
    jobTitle: string;

    constructor(jobTitle: string, message: string = '') {
        super(message);
        this.jobTitle = jobTitle;
    }
}


/**
 * 投递停止，手动暂停
 */
export class PublishStopExp extends PushException {

}

/**
 * 投递限制
 */
export class PublishLimitExp extends PushException {

}
