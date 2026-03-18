import {Tools} from "../platform/utils";
import logger from "../logging";
import {LoginStore} from "../stores";
import axios from '../axios'
import {ElMessage as originalElMessage, MessageParams} from "element-plus";
import {LogRecorder} from "../logging/record";
import axiosOriginal from "axios";
import {GM_xmlhttpRequest} from "$";

const logRecorder = new LogRecorder();
let loginIng = false;


export const silentlyLogin = async (bossUserId: string) => {
    // 保证登录时仅有一个函数执行 防止重复登录(最大等待 500 * 6 = 3000ms)
    let loginCount = 0
    while (loginIng && loginCount < 6) {
        logger.info("login... ", loginCount)
        await Tools.sleep(500)
        loginCount++
    }
    loginIng = true;
    let loginStore = LoginStore();

    // 等待token
    let token = Tools.window?._PAGE?.token;
    let count = 0
    while (!token && count < 3) {
        await Tools.sleep(300)
        token = Tools.window?._PAGE?.token;
        count++
    }

    if (!token) {
        logRecorder.info("未登录Boss，静默登录结束")
        return Promise.reject(new Error("未登录Boss，静默登录失败"));
    }
    if (!bossUserId) {
        bossUserId = Tools.window?._PAGE?.uid;
    }
    if (loginStore.login) {
        logger.info("已经登录，静默登录结束")
        loginIng = false;
        return Promise.resolve();
    }

    return await axios.post("/api/user/silently/login?uniqueId=" + bossUserId).then(async resp => {
        // 用户不存在，直接导入信息注册
        if (resp.data.code === 2000) {
            logRecorder.info("开始自动注册")
            await handlerImport({value: false})
            loginStore.loginSuccess()
            return;
        }
        localStorage.setItem('Authorization', resp.data.data);
        loginStore.loginSuccess()
        logRecorder.info("静默登录成功")
    }).catch(e => {
        logRecorder.error("静默登录失败", e)
        loginStore.loginFail()
        return Promise.reject(e)
    }).finally(() => {
        loginIng = false;
    })

}

export const isProdEnv = (): boolean => {
    return import.meta.env.MODE === 'production';
}


const wrapMessage = (options: any) => {
    if (typeof options === 'string') {
        return "[AI助理] " + options
    }
    if (options && options.message) {
        options.message = "[AI助理] " + options.message
    }
    return options
}

export const ElMessage = ((options: any) => {
    return originalElMessage(wrapMessage(options))
}) as typeof originalElMessage

Object.assign(ElMessage, originalElMessage)

const methods = ['success', 'warning', 'info', 'error'] as const
methods.forEach(type => {
    (ElMessage as any)[type] = (options: any) => {
        return (originalElMessage as any)[type](wrapMessage(options))
    }
})

// ajax请求；不会跨域
export async function fetchWithGM_request(url: string, options: any) {
    return new Promise((resolve, reject) => {
        GM_xmlhttpRequest({
            method: options.method || 'GET',
            url: url,
            headers: options.headers,
            responseType: options.responseType || 'json', // 默认为json，根据需要修改
            data: options.data,
            onload: response => {
                if (response.status === 200) {
                    resolve(response);
                } else {
                    reject(new Error(`Request failed with status: ${response.status}`));
                }
            },
            onerror: () => {
                reject(new Error('Network error'));
            },
            ontimeout: () => {
                reject(new Error('Request timed out'));
            }
        });
    });
}


export const loginInterceptor = (): boolean => {
    const token = Tools.window?._PAGE?.token;
    if (!token) {
        ElMessage({
            message: "请先登录Boss",
            type: 'error',
            duration: 3000
        })
        return false;
    }

    return true;
}


export const handlerImport = async (importResumeLoading: { value: boolean }) => {

    if (!loginInterceptor()) {
        return;
    }
    const token = Tools.window?._PAGE?.token;
    let bossUserId = Tools.window?._PAGE?.uid
    if (!bossUserId) {
        ElMessage({
            message: "未获取到Boss userId 请刷新页面重试",
            type: 'error',
            duration: 3000
        })
        return;
    }

    importResumeLoading.value = true;
    // 获取简历id
    let resumeInfoResp = await axiosOriginal.get("https://www.zhipin.com/wapi/zpgeek/resume/sidebar.json", {headers: {"Zp_token": token}} as {})
    let zpData = resumeInfoResp.data.zpData;
    if (!zpData.attachmentList || zpData.attachmentList.length == 0) {
        importResumeLoading.value = false;
        ElMessage({
            message: "请先在BOSS个人中心上传附件简历；作为ai坐席定制化回复的基础",
            type: 'error',
            duration: 3000
        })
        return;
    }
    let resumeId = zpData.attachmentList[0].resumeId

    // 获取简历文件
    let resumeFileResp: any = await fetchWithGM_request("https://docdownload.zhipin.com/wflow/zpgeek/download/download4geek?resumeId=" + resumeId,
        {headers: {"Zp_token": token}, responseType: 'arraybuffer'} as {})
    let fileBlob = new Blob([resumeFileResp.response], {type: 'application/pdf'});

    // 导入简历
    let formData = new FormData();
    formData.append("file", fileBlob)
    formData.append("resumeId", resumeId)
    formData.append("uniqueId", bossUserId)
    let importResp = await axios.post("/api/user/import/resume", formData, {headers: {'Content-Type': "multipart/form-data"}})
    if (importResp.data.code != 200) {
        ElMessage({
            message: "导入简历失败" + importResp.data.data.msg,
            type: 'error',
            duration: 3000
        })
        importResumeLoading.value = false;
        return;
    }
    let loginResp = await axios.post("/api/user/silently/login?uniqueId=" + bossUserId)
    localStorage.setItem('Authorization', loginResp.data.data);
    if (!importResp.data.data.email) {
        importResumeLoading.value = false;
        return;
    }
    ElMessage({
        message: "导入简历成功",
        type: 'success',
        duration: 3000
    });
    importResumeLoading.value = false;
}
