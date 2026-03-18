<template>
    <div class="ai-config">
        <el-collapse v-model="activeCollapseNames">
            <el-collapse-item name="tune" title=">模型微调(点击展开收起)" class="tune-form">
                <div class="tune-form">
                    <el-form label-width="120px">
                        <el-form-item label="用户提示词">
                            <el-input
                                v-model="form.userPrompt"
                                type="textarea"
                                :rows="6"
                                :maxlength="5000"
                                show-word-limit
                                placeholder="请输入用于微调的用户提示词，将作为AI坐席的部分系统提示词使用 示例如下：
## 语气风格
- 使用比较轻快活泼的风格交流，同时保持积极专业的沟通态度
- 避免过于正式或刻板的表达，保持对话流畅性

## 信息处理
- 用数据化成果突出个人价值，如完成3个百万级项目
- 对薪资、到岗时间等敏感问题采用策略性回应

## 以上仅作为示例写法，无实际意义
"
                            />
                        </el-form-item>

                        <el-form-item>
                            <el-button type="primary" @click="handleSavePrompt">保存</el-button>
                            <el-button type="warning" @click="openDebugDialog">调试</el-button>
                        </el-form-item>
                    </el-form>
                </div>
            </el-collapse-item>

            <el-collapse-item name="api" title=">自有API(点击展开收起)" class="tune-form">
                <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="config-form">

                    <div style="display: flex;">
                        <el-tooltip
                            class="box-item"
                            effect="dark"
                            content="测试不通过，测试通过后才可保存生效"
                            placement="bottom"
                            :visible="!form.testPassed && form.status === 1"
                        >
                            <el-form-item label="启用自有API" prop="status">
                                <el-switch
                                    v-model="form.status"
                                    :active-value="1"
                                    :inactive-value="0"
                                    @change="handleStatusChange"
                                />
                            </el-form-item>
                        </el-tooltip>

                        <el-form-item class="select-opt-item" label="提供商" prop="provider">
                            <el-select v-model="form.provider" placeholder="请选择大模型提供商"
                                       @change="handleProviderChange">
                                <el-option
                                    v-for="option in providerOptions"
                                    :key="option.value"
                                    :label="option.label"
                                    :value="option.value"
                                />
                            </el-select>
                        </el-form-item>

                        <el-form-item class="select-opt-item" label="模型名称" prop="modelName">
                            <el-select
                                v-model="form.modelName"
                                placeholder="请选择或输入模型名称"
                                filterable
                                allow-create
                                default-first-option
                            >
                                <el-option
                                    v-for="model in availableModels"
                                    :key="model"
                                    :label="model"
                                    :value="model"
                                />
                            </el-select>
                        </el-form-item>

                    </div>

                    <el-form-item label="API KEY" prop="apiKey">
                        <el-input v-model="form.apiKey" placeholder="请输入API Key" show-password/>
                    </el-form-item>

                    <el-form-item label="BASE URL" prop="baseUrl">
                        <el-input v-model="form.baseUrl" placeholder="选择大模型提供商自动获取BASE URL"/>
                    </el-form-item>

                    <el-form-item label="Completions" prop="completionsPath">
                        <el-input v-model="form.completionsPath" placeholder="不用填写 默认：/chat/completions"/>
                    </el-form-item>

                    <el-form-item label="超时时间" prop="timeout">
                        <el-input-number v-model="form.timeout" :min="1" :max="120"/>
                        <span class="unit">秒</span>
                    </el-form-item>

                    <el-form-item>
                        <el-button type="info" @click="handleTempSave">暂存</el-button>
                        <el-button type="success" :loading="isTestLoading" @click="handleTest">测试</el-button>
                        <el-tooltip
                            class="box-item"
                            effect="dark"
                            content="请先测试；测试通过后才可保存生效"
                            placement="bottom"
                        >
                            <el-button type="primary" @click="handleSave" :disabled="!form.testPassed">保存</el-button>
                        </el-tooltip>
                    </el-form-item>
                </el-form>
            </el-collapse-item>
        </el-collapse>

        <el-dialog v-model="debugDialogVisible" title="调试用户提示词" width="800px">
            <div class="chat-history">
                <el-empty v-show="debugHistory.length===0" description="暂无历史消息，请在下方开始你的调试吧" />
                <div v-for="(m,idx) in debugHistory" :key="idx" class="chat-row"
                     :class="m.role === 'user' ? 'from-user' : 'from-ai'">
                    <div class="bubble">
                        <div class="meta">{{ mapRoleTitle(m.role) }}</div>
                        <div class="content">{{ m.content }}</div>
                        <div v-if="m.role === 'assistant'" class="tags">
                            <el-tag v-for="(t,i) in (m.answerTypes||[])" :key="'a-'+i" size="small" type="info">
                                {{ mapAnswerType(t) }}
                            </el-tag>
                            <el-tag v-for="(t,i) in (m.operationTypes||[])" :key="'o-'+i" size="small" type="success">
                                {{ mapOperationType(t) }}
                            </el-tag>
                        </div>
                    </div>
                </div>
            </div>
            <div class="chat-composer">
                <div class="composer-input">
                    <el-input
                        v-model="debugQuestion"
                        type="textarea"
                        :autosize="{ minRows: 3, maxRows: 8 }"
                        :maxlength="5000"
                        show-word-limit
                        placeholder="作为招聘的HR角色提出你的问题,AI坐席将结合你的偏好设置与微调提示词给出最终回答"
                        clearable
                    />
                    <el-button class="send-btn" type="primary" :loading="isDebugLoading" @click="handleSendDebug">发送
                    </el-button>
                </div>
            </div>
            <template #footer>
                <el-button type="warning" :disabled="isDebugLoading || debugHistory.length===0"
                           @click="handleClearHistory">清空历史
                </el-button>
                <el-button @click="debugDialogVisible = false">关闭</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import {ref, onMounted, watch} from 'vue'
import axios from '../../axios'
import {ElMessage} from '../../utils/tools'
import {ElNotification} from 'element-plus'
import {Tools, TampermonkeyApi} from "../../platform/utils";
import {ServerStore} from "../../stores/server";

const serverStore = ServerStore();

interface AiConfig {
    userId: number
    provider: number
    modelName: string
    apiKey: string
    baseUrl: string
    timeout: number
    completionsPath: string
    testPassed: number
    status: number
    userPrompt?: string
}

interface ProviderDetail {
    code: number
    desc: string
    defaultBaseUrl: string
}

// Provider options
const providerOptions = [
    { label: '自定义', value: 0 },
    {label: 'Deepseek', value: 1},
    {label: '火山引擎', value: 2},
    {label: '硅基流动', value: 3},
    {label: '月之暗面', value: 4},
    {label: 'Open Router', value: 5}
]

// Model options for each provider
const modelOptions = {
    0: [], // Custom
    1: ['deepseek-chat', 'deepseek-reasoner'],
    2: ['deepseek-r1-250120', '...'],
    3: ['deepseek-ai/DeepSeek-V3', '...'],
    4: ['moonshot-v1-8k', 'moonshot-v1-32k', 'moonshot-v1-128k'],
    5: ['deepseek/deepseek-chat-v3-0324:free', '...']
}

const formRef = ref()
const availableModels = ref<string[]>([])
const providerDetails = ref<Record<number, ProviderDetail>>({})

// Form data
const form = ref<AiConfig>({
    userId: 0,
    provider: 1,
    modelName: '',
    apiKey: '',
    baseUrl: '',
    timeout: 60,
    completionsPath: '',
    testPassed: 0,
    status: 0,
    userPrompt: ''
})

const isTestLoading = ref(false)
const activeCollapseNames = ref<string[]>(['tune'])
const debugDialogVisible = ref(false)
const debugQuestion = ref('')
const isDebugLoading = ref(false)
const debugHistory = ref<Array<{
    role: 'user' | 'assistant',
    content: string,
    answerTypes?: number[],
    operationTypes?: number[]
}>>([])
// Form validation rules
const rules = {
    provider: [{required: true, message: '请选择提供商类型', trigger: 'change'}],
    modelName: [
        {required: true, message: '请输入模型名称', trigger: 'change'},
        {
            validator: (rule: any, value: string, callback: Function) => {
                if (value === '...') {
                    callback(new Error('请选择具体模型名或输入模型名称'))
                } else {
                    callback()
                }
            }, trigger: 'change'
        }
    ],
    apiKey: [{required: true, message: '请输入API Key', trigger: 'change'}],
    timeout: [{required: true, message: '请输入超时时间', trigger: 'change'}],
    baseUrl: [{required: true, message: 'Base URL 不能为空', trigger: 'change'}]
}

// 获取所有供应商详情
const fetchAllProviderDetails = async () => {
    try {
        const response = await axios.get('/api/user/ai/config/all/provider')
        if (response.data.code === 200) {
            const details = response.data.data
            // 将供应商详情转换为以code为键的对象
            providerDetails.value = details.reduce((acc: Record<number, ProviderDetail>, detail: ProviderDetail) => {
                acc[detail.code] = detail
                return acc
            }, {})
        }
    } catch (error) {
        ElMessage({
            type: 'error',
            message: '获取供应商信息失败'
        })
    }
}

// 存储最后一次从接口获取的配置数据
const lastFetchedConfig = ref<AiConfig | null>(null)

// 比较当前表单数据和最后一次获取的配置数据
const compareWithLastConfig = () => {
    if (!lastFetchedConfig.value) return false

    const currentConfig = form.value
    const normalizeCompletionsPath = (path: string | null | undefined) => {
        return !path || path.trim() === '' ? '' : path
    }
    return (
        currentConfig.provider === lastFetchedConfig.value.provider &&
        currentConfig.modelName === lastFetchedConfig.value.modelName &&
        currentConfig.apiKey === lastFetchedConfig.value.apiKey &&
        currentConfig.baseUrl === lastFetchedConfig.value.baseUrl &&
        normalizeCompletionsPath(currentConfig.completionsPath) === normalizeCompletionsPath(lastFetchedConfig.value.completionsPath)
    )
}

// Handle provider change
const handleProviderChange = (value: number, keepModelName: boolean = false) => {
    // Update available models
    availableModels.value = modelOptions[value as keyof typeof modelOptions] || []
    // Clear model name when provider changes, unless keepModelName is true
    if (!keepModelName) {
        form.value.modelName = ''
    }
    // 从已获取的供应商详情中设置baseUrl
    if (value !== 0 && providerDetails.value[value]) {
        form.value.baseUrl = providerDetails.value[value].defaultBaseUrl
    }

    // 检查数据是否变化并更新testPassed
    const isDataUnchanged = compareWithLastConfig()
    if (!isDataUnchanged) {
        form.value.testPassed = 0
    }
}

// Fetch current configuration
const fetchConfig = async () => {
    try {
        const response = await axios.get('/api/user/ai/config/current')
        if (response.data.code === 200) {
            let config = response.data.data
            if (!config) {
                config = {
                    status: 0,
                    provider: 1,
                    timeout: 60,
                }
            }
            form.value = {...form.value, ...config}
            
            // 成功拉取，更新镜像
            const mirrorKey = serverStore.getMirrorKey('ai_config')
            const globalMirrorKey = serverStore.getGlobalMirrorKey('ai_config')
            TampermonkeyApi.GmSetValue(mirrorKey, config)
            TampermonkeyApi.GmSetValue(globalMirrorKey, config)
            
            // 保存获取到的配置
            lastFetchedConfig.value = {...config}
            // Update available models based on provider, but keep the modelName
            handleProviderChange(config.provider, true)
        }
    } catch (error) {
        // 请求失败，尝试从本地镜像加载
        const mirrorKey = serverStore.getMirrorKey('ai_config')
        let mirrorData = TampermonkeyApi.GmGetValue(mirrorKey, null)
        
        if (!mirrorData) {
            const globalMirrorKey = serverStore.getGlobalMirrorKey('ai_config')
            mirrorData = TampermonkeyApi.GmGetValue(globalMirrorKey, null)
        }
        
        if (mirrorData) {
            form.value = {...form.value, ...mirrorData}
            handleProviderChange(mirrorData.provider, true)
            ElNotification({
                title: '使用本地配置',
                message: '服务器连接失败，已加载本地 AI 配置镜像',
                type: 'warning',
                duration: 3000
            })
        } else {
            ElMessage({
                type: 'error',
                message: '获取 AI 配置失败，且无本地镜像'
            })
        }
    }
}

// 监听表单数据变化
watch(() => ({
    provider: form.value.provider,
    modelName: form.value.modelName,
    apiKey: form.value.apiKey,
    baseUrl: form.value.baseUrl,
    completionsPath: form.value.completionsPath,
    timeout: form.value.timeout,
    status: form.value.status
}), () => {
    const isDataUnchanged = compareWithLastConfig()
    if (!isDataUnchanged) {
        form.value.testPassed = 0
    }
    if (lastFetchedConfig.value?.testPassed && isDataUnchanged) {
        form.value.testPassed = 1
    }
}, {deep: true})
// 更新本地配置镜像
const updateAiConfigMirror = (config: any) => {
    const mirrorKey = serverStore.getMirrorKey('ai_config')
    const globalMirrorKey = serverStore.getGlobalMirrorKey('ai_config')
    TampermonkeyApi.GmSetValue(mirrorKey, config)
    TampermonkeyApi.GmSetValue(globalMirrorKey, config)
}

// Handle save configuration
const handleSave = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid: boolean) => {
        if (valid) {
            // 先保存到本地镜像
            updateAiConfigMirror(form.value)
            
            try {
                const {userPrompt, ...rest} = form.value
                const response = await axios.post('/api/user/ai/config/save', rest)
                if (response.data.code === 200) {
                    ElMessage({
                        type: 'success',
                        message: '保存成功并同步至服务器'
                    })
                }
            } catch (e) {
                ElNotification({
                    title: '保存至本地',
                    message: '服务器离线，配置仅保存在本地镜像',
                    type: 'warning'
                })
            }
        }
    })
}

const handleTempSave = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid: boolean) => {
        if (valid) {
            // 先保存到本地镜像
            updateAiConfigMirror(form.value)
            
            try {
                // 请求时排除from表单中的userPrompt
                const {userPrompt, ...rest} = form.value
                const response = await axios.post('/api/user/ai/config/temp/save', rest)
                if (response.data.code === 200) {
                    ElMessage({
                        type: 'success',
                        message: '保存成功并同步至服务器'
                    })
                    await fetchConfig()
                }
            } catch (error) {
                ElNotification({
                    title: '保存至本地',
                    message: '服务器离线，配置仅保存在本地镜像',
                    type: 'warning'
                })
            }
        }
    })
}
const handleSavePrompt = async () => {
    // 提示词也属于配置镜像的一部分
    updateAiConfigMirror(form.value)
    
    try {
        const resp = await axios.post('/api/user/ai/config/temp/save', {
            userPrompt: form.value.userPrompt || '',
            userId: form.value.userId
        })
        if (resp.data.code === 200) {
            ElMessage({type: 'success', message: '保存成功并同步至服务器'})
        }
    } catch (e) {
        ElNotification({
            title: '保存至本地',
            message: '服务器离线，提示词仅保存在本地镜像',
            type: 'warning'
        })
    }
}
// Handle test configuration
const handleTest = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid: boolean) => {
        if (!valid) {
            return;
        }
        isTestLoading.value = true
        try {
            const response = await axios.post('/api/user/ai/config/test', form.value, {timeout: form.value.timeout * 1000 - 200})
            if (response.data.code === 200) {
                ElNotification({
                    title: '测试通过',
                    message: response.data.data,
                    type: 'success'
                })
                form.value.testPassed = 1
                return;
            }

            ElNotification({
                title: '测试失败',
                message: response.data.message,
                type: 'error',
                customClass: 'test-failed-notification',
            })
        } catch (e: any) {
            ElNotification({
                title: '测试失败',
                message: e,
                type: 'error',
                customClass: 'test-failed-notification',
            })
        } finally {
            isTestLoading.value = false
        }
    })
}

// Handle status change
const handleStatusChange = () => {
    ElNotification({
        title: '自有ApiKey提示',
        message: '需要点击保存按钮后生效',
        type: 'info',
        duration: 3000
    })
}

const openDebugDialog = () => {
    debugDialogVisible.value = true
}

const jobKey = ref('')
const getJobKey = () => {
    if (jobKey.value) {
        return jobKey.value
    }
    let key = 'ask-debug-' + Tools.window._PAGE.uid + "-" + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    jobKey.value = key
    return key
}

const handleSendDebug = async () => {
    if (!debugQuestion.value) {
        ElMessage({type: 'warning', message: '请输入问题'})
        return
    }
    if (debugHistory.value.length >= 20) {
        ElMessage({type: 'warning', message: '总对话长度不能超过20条，请先清空历史消息重试'})
        return
    }
    const question = debugQuestion.value
    debugHistory.value.push({role: 'user', content: question})
    debugQuestion.value = ''
    isDebugLoading.value = true
    try {
        const payload: any = {
            jobKey: getJobKey(),
            question,
            jobInfo: {},
            userPrompt: form.value.userPrompt || '',
            messageList: debugHistory.value.slice(0, debugHistory.value.length - 1)
        }
        const resp = await axios.post('/api/user/ai/config/debug', payload, {
            timeout: 60000,
            headers: {'Content-Type': 'application/json'}
        })
        const data = resp?.data?.data || {}
        const answer = data?.answerContent || ''
        const answerTypes: number[] = Array.isArray(data?.answerTypeList) ? data.answerTypeList : []
        const operationTypes: number[] = Array.isArray(data?.operationTypeList) ? data.operationTypeList : []
        debugHistory.value.push({role: 'assistant', content: answer, answerTypes, operationTypes})
    } catch (e) {
        ElMessage({type: 'error', message: '调试失败'})
    } finally {
        isDebugLoading.value = false
    }
}

const handleClearHistory = () => {
    debugHistory.value = []
    jobKey.value = ''
}

const mapAnswerType = (t: number) => {
    if (t === 0) return 'NULL'
    if (t === 1) return '发送消息'
    if (t === 2) return 'BOSS操作'
    if (t === 3) return '不回复当前消息'
    if (t === 4) return 'AI服务异常'
    return String(t)
}

const mapOperationType = (t: number) => {
    if (t === 0) return 'NULL'
    if (t === 1) return '发送简历'
    return String(t)
}

const mapRoleTitle = (role: 'user' | 'assistant') => {
    if (role === 'user') return 'HR'
    return 'AI坐席'
}

// Fetch data on mount
onMounted(() => {
    fetchAllProviderDetails()
    fetchConfig()
})
</script>

<style scoped>
.ai-config {
    padding: 15px 1px 1px;
    background: white;
}

.config-form {
    margin: 0px 0;
}

.unit {
    margin-left: 8px;
}

.select-opt-item {
    width: 400px;
}

:global(.test-failed-notification) {
    width: 600px !important;
}

.tune-form {
    margin-bottom: 10px;
}

.debug-form {
    margin-bottom: 10px;
}

.chat-history {
    max-height: 420px;
    overflow-y: auto;
    padding: 8px 4px;
    background: #fafafa;
    border: 1px solid #eee;
    border-radius: 6px;
}

.chat-composer {
    display: flex;
    gap: 10px;
    margin-top: 10px;
}

.composer-input {
    position: relative;
    width: 100%;
}

.composer-input :deep(.el-textarea__inner) {
    padding-right: 84px;
    padding-bottom: 50px;
}

.composer-input :deep(.el-input__count) {
    bottom: 40px;
    right: 8px;
}

.send-btn {
    position: absolute;
    right: 8px;
    bottom: 8px;
}

.chat-row {
    display: flex;
    margin: 8px 0;
}

.chat-row.from-user {
    justify-content: flex-start;
}

.chat-row.from-ai {
    justify-content: flex-end;
}

.bubble {
    max-width: 80%;
    padding: 8px 10px;
    border-radius: 8px;
    background: #fff;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.from-user .bubble {
    background: #f5f7fa;
}

.from-ai .bubble {
    background: #e8f6f3;
}

.bubble .content {
    white-space: pre-wrap;
    word-break: break-word;
    font-size: 13px;
}

.bubble .meta {
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
}

.bubble .tags {
    margin-top: 6px;
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
}

.tune-form {
    padding: 0 10px;
    font-weight: bold;
}

</style>
