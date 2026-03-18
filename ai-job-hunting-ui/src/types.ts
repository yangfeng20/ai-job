export enum BizCodeEnum {

    NOT_LOGIN = 401,
    PARAM_ERROR = 410,
    INTERNAL_SERVER_ERROR = 500,

    USER_NOT_EXIST = 2000,
    PROMOTION_CODE_EXPIRED = 2001,
    PRODUCT_NOT_AUTHORIZED = 5001,
}

export interface Pair<K, V> {
    key: K,
    value: V,
}