/**
 * 投递状态
 */
export enum PushStatus {
    NOT_START,
    PUSHING,
    PAUSE,
    LIMIT,
}


/**
 * 投递结果状态
 */
export enum PushResultStatus {
    NOT_START = -1,
    SUCCESS,
    FAIL,
}
