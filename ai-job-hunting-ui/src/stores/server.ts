import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { TampermonkeyApi } from "../platform/utils"

const SERVER_URL_KEY = 'custom_server_url'
export const DEFAULT_SERVER_URL = 'https://43.138.246.37/'

export const ServerStore = defineStore('server', () => {
    // 从 GM_getValue 获取保存的地址，如果没有则使用默认地址
    const baseUrl = ref(TampermonkeyApi.GmGetValue(SERVER_URL_KEY, DEFAULT_SERVER_URL))
    const status = ref<'online' | 'offline' | 'checking'>('checking')
    const lastError = ref('')

    // 计算属性：当前是否为在线模式
    const isOnline = computed(() => status.value === 'online')

    /**
     * 更新服务器地址并持久化
     */
    function setBaseUrl(url: string) {
        if (!url.endsWith('/')) {
            url += '/'
        }
        baseUrl.value = url
        TampermonkeyApi.GmSetValue(SERVER_URL_KEY, url)
        status.value = 'checking'
    }

    /**
     * 重置为默认服务器地址
     */
    function resetBaseUrl() {
        setBaseUrl(DEFAULT_SERVER_URL)
    }

    /**
     * 设置当前状态
     */
    function setStatus(newStatus: 'online' | 'offline' | 'checking', error: string = '') {
        status.value = newStatus
        lastError.value = error
    }

    /**
     * 获取基于当前服务器地址的本地镜像 Key
     */
    function getMirrorKey(type: string) {
        const safeUrl = baseUrl.value.replace(/[^a-zA-Z0-9]/g, '_')
        return `mirror_${type}_${safeUrl}`
    }

    /**
     * 获取全局镜像 Key（不绑定特定服务器）
     */
    function getGlobalMirrorKey(type: string) {
        return `mirror_${type}_global_latest`
    }

    /**
     * 测试连接状态
     */
    async function checkConnection() {
        status.value = 'checking'
        try {
            const response = await fetch(`${baseUrl.value}api/user/userinfo`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': localStorage.getItem('Authorization') || ''
                },
                body: JSON.stringify({})
            })
            if (response.ok) {
                status.value = 'online'
            } else {
                status.value = 'offline'
                lastError.value = `HTTP ${response.status}`
            }
        } catch (e) {
            status.value = 'offline'
            lastError.value = '网络连接失败'
        }
    }

    return {
        baseUrl,
        status,
        lastError,
        isOnline,
        setBaseUrl,
        resetBaseUrl,
        setStatus,
        getMirrorKey,
        getGlobalMirrorKey,
        checkConnection
    }
})
