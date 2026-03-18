import axios from "axios";
import {ElMessage} from "./utils/tools";
import {BizCodeEnum} from "./types";
import {ProductStore} from "./stores";
import {ServerStore} from "./stores/server";


/**
 * 创建自定义的axios对象，实现请求拦截器和响应拦截器
 *
 */
const request = axios.create({
    // 超时时间
    timeout: 10000,
    // 格式为json数据；字符编码utf-8
    headers: {
        'Content-Type': 'application/json; charset=utf-8',
    }
})

/**
 * 请求拦截器
 */
request.interceptors.request.use(req => {
        try {
            const serverStore = ServerStore()
            req.baseURL = serverStore.baseUrl
        } catch (e) {
            // 如果在 Pinia 初始化前调用了 axios，回退到默认地址
            req.baseURL = 'https://43.138.246.37/'
        }

        let authorization = localStorage.getItem('Authorization');
        // 请求时携带token
        if (authorization) {
            req.headers['Authorization'] = authorization;
        }
        return req;
    }
)

function handlerErrorCode(result: any) {
    if (!result || result?.code < 5000) {
        return;
    }
    if (result.code === BizCodeEnum.PRODUCT_NOT_AUTHORIZED) {
        const productStore = ProductStore()
        productStore.setShowProduct(true)
    }
}

/**
 * 响应拦截器
 */
request.interceptors.response.use((resp: any) => {
        const serverStore = ServerStore()
        serverStore.setStatus('online')

        // http的响应状态码会进入这里
        let result = resp.data;
        if (result.code === 200) {
            return resp;
        }
        //  调用方处理
        if (result.code >= 2000 && result.code < 5000) {
            return resp;
        }

        if (result.code === 401) {
            let authorization = localStorage.getItem('Authorization');
            if (authorization) {
                ElMessage({
                    type: "error",
                    message: "登录过期，请刷新页面重试"
                });
                return;
            }
            return Promise.reject(result.message)
        }

        if (!result.code || result.code === 500 || result.code >= 5000) {
            // 代码执行下来，说明code不为200，或者result有问题【使用弹窗提示,可能为空白】
            ElMessage({
                type: "error",
                message: result.message ? result.message : '系统异常'
            });
            handlerErrorCode(result);
        }
        // 使请求不进入正常的响应处理函数
        return Promise.reject(result.message)
    },

    /**
     * 当
     * http的状态码不为200时
     * @param error
     */
    error => {
        const serverStore = ServerStore()

        // 到达前端axios设置的超时时间
        if (error.code === 'ECONNABORTED') {
            serverStore.setStatus('offline', '请求超时')
            // 弹窗提示
            ElMessage({
                message: '网络超时',
                type: 'error',
                grouping: true,
                duration: 2000
            })
            // 使请求不进入正常的响应处理函数
            return Promise.reject("time out")
        }
        // 网络不通，无法访问后端，或者服务器不在线
        if (error.code === 'ERR_NETWORK') {
            serverStore.setStatus('offline', '无法访问服务器')
            // 弹窗提示
            ElMessage({
                message: '系统异常,请稍后重试',
                type: 'error',
                grouping: true,
                duration: 2000
            })
            // 使请求不进入正常的响应处理函数
            return Promise.reject(() => {
            })
        }

        // 获取后端返回的错误信息
        if (error.response && error.response.data) {
            error.message = error.response.data.message;
        }

        if (error.response && error.response.status === 404) {
            error.message = "资源未找到";
        }

        // 弹窗提示，3秒
        ElMessage({
            message: error.message,
            type: 'error',
            grouping: true,
            duration: 3000
        })

        // 使请求不进入正常的响应处理函数
        return Promise.reject(error)
    })


/**
 * 暴露自定义的axios
 */
export default request;
