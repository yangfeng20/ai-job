import {LoginStore, UserStore} from "./index";
import {PreferenceConfig} from "./types";
import {ServerStore} from "./server";
import {TampermonkeyApi} from "../platform/utils";
import logging from "../logging";
import {silentlyLogin} from "../utils/tools"
import axios from "../axios";
import {LogRecorder} from "../logging/record";

const logRecorder = new LogRecorder();

export function userRemoteLoad() {
    logRecorder.info("加载用户偏好配置")
    const userStore = UserStore()
    const loginStore = LoginStore();
    const serverStore = ServerStore();

    if (loginStore.loginFailStatus){
        return;
    }

    // 先尝试静默登录
    silentlyLogin("").then(_ => {
        logging.debug("调用接口加载用户偏好配置")
        return axios.post("/api/user/userinfo", {})
    }).then(resp => {
        userStore.user = resp?.data?.data
        if (!userStore?.user){
            throw new Error("用户偏好配置为空")
        }

        // 成功获取数据，同时存入特定服务器镜像和全局最新镜像
        const mirrorKey = serverStore.getMirrorKey('user_config')
        const globalMirrorKey = serverStore.getGlobalMirrorKey('user_config')
        TampermonkeyApi.GmSetValue(mirrorKey, userStore.user)
        TampermonkeyApi.GmSetValue(globalMirrorKey, userStore.user)

        userStore.user.preference.pi = userStore.user.preference.pi || 3
        userStore.user.preference.npi = userStore.user.preference.npi || 6
        logRecorder.info("从服务器加载配置成功")
    }).catch(error => {
        logRecorder.warn("从服务器加载配置失败，尝试读取本地镜像", error.message)

        // 1. 优先尝试从当前服务器的镜像加载
        const mirrorKey = serverStore.getMirrorKey('user_config')
        let mirrorData = TampermonkeyApi.GmGetValue(mirrorKey, null)

        // 2. 如果当前服务器没有镜像，尝试从全局最新镜像加载
        if (!mirrorData) {
            const globalMirrorKey = serverStore.getGlobalMirrorKey('user_config')
            mirrorData = TampermonkeyApi.GmGetValue(globalMirrorKey, null)
            if (mirrorData) {
                logRecorder.info("已从全局最新镜像回退加载配置")
            }
        }

        if (mirrorData) {
            userStore.user = mirrorData
            logRecorder.info("已加载本地镜像配置 (离线模式)")
        } else {
            loginStore.loginFail()
            logRecorder.error("加载配置失败：无服务器数据且无本地镜像")
        }
    })
.finally(() => {
        if (!userStore.user.preference) {
            userStore.user.preference = {} as PreferenceConfig
        }
    })
}
