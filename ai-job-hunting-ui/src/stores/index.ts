import {reactive, ref} from 'vue'
import {defineStore} from 'pinia'
import {PreferenceConfig, User} from "./types";
import logger from "../logging";
import platform, {PlatformTypeEnum} from "../platform/platform";
import {TampermonkeyApi} from "../platform/utils";

export const pushResultCount = defineStore('pushResultCount', () => {
    const notMatchCount = ref(0)
    const successCount = ref(TampermonkeyApi.GmGetValue(TampermonkeyApi.PUSH_SUCCESS_COUNT, 0))
    const onceSuccessCount = ref(0)
    const failCount = ref(TampermonkeyApi.GmGetValue(TampermonkeyApi.PUSH_FAIL_COUNT, 0))

    function notMatchIncr() {
        notMatchCount.value++
    }

    function successIncr() {
        successCount.value++
        onceSuccessCount.value++
        TampermonkeyApi.GmSetValue(TampermonkeyApi.PUSH_SUCCESS_COUNT, successCount.value)
    }

    function failIncr() {
        failCount.value++
        TampermonkeyApi.GmSetValue(TampermonkeyApi.PUSH_FAIL_COUNT, failCount.value)
    }

    function clearOnceSuccessCount() {
        onceSuccessCount.value = 0
    }

    return {
        notMatchIncr,
        successIncr,
        notMatchCount,
        successCount,
        failCount,
        failIncr,
        onceSuccessCount,
        clearOnceSuccessCount
    }
})

export const UserStore = defineStore('ai-user', () => {

    const platformType = ref<number>()
    const user = reactive<User>(getLocalUser())
    return {
        user,
        platformType
    };
})


export const LoginStore = defineStore('LoginStore', () => {

    const login = ref<false | true>()
    const loginFailStatus = ref<false | true>()

    function loginSuccess() {
        login.value = true
    }

    function loginFail() {
        loginFailStatus.value = true
    }

    return {
        login, loginSuccess, loginFailStatus, loginFail
    };
})


function getLocalUser(): User {
    const map = new Map<PlatformTypeEnum, PreferenceConfig>();
    let jsonData = localStorage.getItem("ai-job-user");
    if (jsonData === null) {
        jsonData = '{"phone":"","email":"","preference":{},"preferenceMap":{}}'
    }
    let user = JSON.parse(jsonData) as User;
    logger.debug("获取本地用户配置", user)
    return user;
}


export const ProductStore = defineStore('ProductStore', () => {

    const showProduct = ref<false | true>(false)

    function setShowProduct(show: boolean) {
        showProduct.value = show
    }

    return {
        showProduct, setShowProduct
    };
})