<template>
    <el-form
        ref="ruleFormRef"
        :model="userStore.user"
        :rules="rules as FormRules<RuleForm>"
        label-position="right"
        label-width="auto"
        class="form-preference"
        size="large"
        status-icon>

        <div>
            <div v-if="Tools.window.location.href.includes('job-recommend')">
                <el-text class="mx-1 top-title" type="danger">!!!请前往顶部【搜索】按钮所在页面保存偏好设置!!!</el-text>
                <br/>
                <br/>
            </div>
            <el-text class="mx-1 top-title" type="warning">账号信息</el-text>
            <div style="display: flex;margin-top: 10px">
                <el-form-item label="手机号" prop="phone" style="margin-left: -6px;">
                    <el-input v-model="userStore.user.phone"/>
                </el-form-item>

                <el-form-item label="通知邮箱" prop="email">
                    <el-input v-model="userStore.user.email"/>
                </el-form-item>
            </div>

            <el-text class="mx-1 top-title" type="warning">投递设置</el-text>
            <div style="display: flex;margin-top: 10px">
                <el-form-item prop="companyInclude" style="margin-left: -40px;">
                    <template #label>
                        <el-checkbox v-model="userStore.user.preference.cniE" label="" size="large"/>
                        公司名包含
                    </template>
                    <el-select v-model="userStore.user.preference.cni"
                               multiple
                               filterable
                               remote
                               allow-create
                               default-first-option
                               :reserve-keyword="false"
                               placeholder="公司名包含"
                               style="width: 240px">
                        <el-option v-for="(item,inx) in ['请输入公司名']"
                                   :key="inx"
                                   :label="item"
                                   :value="item"/>
                    </el-select>
                </el-form-item>

                <el-form-item label="公司名排除" prop="companyExclude">
                    <template #label>
                        <el-checkbox v-model="userStore.user.preference.cneE" label="" size="large"/>
                        公司名排除&nbsp;&nbsp;&nbsp;
                    </template>
                    <el-select v-model="userStore.user.preference.cne"
                               multiple
                               filterable
                               remote
                               allow-create
                               default-first-option
                               :reserve-keyword="false"
                               placeholder="公司名排除"
                               style="width: 240px">
                        <el-option v-for="(item,inx) in ['请输入公司名']"
                                   :key="inx"
                                   :label="item"
                                   :value="item"/>
                    </el-select>
                </el-form-item>
            </div>

            <div style="display: flex">
                <el-form-item label="工作名包含" style="margin-left: -40px;" prop="jobNameInclude">
                    <template #label>
                        <el-checkbox v-model="userStore.user.preference.jniE" label="" size="large"/>
                        工作名包含
                    </template>
                    <el-select v-model="userStore.user.preference.jni"
                               multiple
                               filterable
                               remote
                               allow-create
                               default-first-option
                               :reserve-keyword="false"
                               placeholder="工作名包含"
                               style="width: 240px">
                        <el-option v-for="(item,inx) in ['请输入工作名']"
                                   :key="inx"
                                   :label="item"
                                   :value="item"/>
                    </el-select>
                </el-form-item>

                <el-form-item label="工作名排除" prop="jobContentExclude">
                    <template #label>
                        <el-checkbox v-model="userStore.user.preference.jneE" label="" size="large"/>
                        工作名排除&nbsp;&nbsp;&nbsp;
                    </template>
                    <el-select v-model="userStore.user.preference.jne"
                               multiple
                               filterable
                               remote
                               allow-create
                               default-first-option
                               :reserve-keyword="false"
                               placeholder="工作名排除"
                               style="width: 240px">
                        <el-option v-for="(item,inx) in ['请输入岗位名称']"
                                   :key="inx"
                                   :label="item"
                                   :value="item"/>
                    </el-select>
                </el-form-item>
            </div>

            <div style="display: flex">
                <el-form-item label="工作内容包含" style="margin-left: -40px;" prop="jobContentInclude">
                    <template #label>
                        <el-checkbox v-model="userStore.user.preference.jciE" label="" size="large"/>
                        &nbsp;&nbsp;&nbsp;&nbsp;内容包含
                    </template>
                    <el-select v-model="userStore.user.preference.jci"
                               multiple
                               filterable
                               remote
                               allow-create
                               default-first-option
                               :reserve-keyword="false"
                               placeholder="工作内容包含"
                               style="width: 240px">
                        <el-option v-for="(item,inx) in ['请输入工作内容']"
                                   :key="inx"
                                   :label="item"
                                   :value="item"/>
                    </el-select>
                </el-form-item>

                <el-form-item label="工作内容排除" prop="jobContentExclude">
                    <template #label>
                        <el-checkbox v-model="userStore.user.preference.jceE" label="" size="large"/>
                        工作内容排除
                    </template>
                    <el-select v-model="userStore.user.preference.jce"
                               multiple
                               filterable
                               remote
                               allow-create
                               default-first-option
                               :reserve-keyword="false"
                               placeholder="工作内容排除"
                               style="width: 240px">
                        <el-option v-for="(item,inx) in ['请输入工作内容字符串']"
                                   :key="inx"
                                   :label="item"
                                   :value="item"/>
                    </el-select>
                </el-form-item>
            </div>

            <!--            <div class="form-bottom">-->
            <div style="display: flex">
                <div style="display: flex;height: 40px">
                    <el-checkbox v-model="userStore.user.preference.srE" label="" size="large"/>
                    <el-input class="input-opt"
                              v-model="userStore.user.preference.sr"
                              style="width: 324px"
                              placeholder="薪资范围 例:9-15">
                        <template #prepend>
                            <el-select v-model="userStore.user.preference.srT" placeholder="月薪(k)"
                                       style="width: 100px">
                                <el-option label="月薪(k)" value="1"/>
                                <el-option label="日薪" value="2"/>
                            </el-select>
                        </template>
                    </el-input>
                </div>

                <el-form-item label="公司规模范围" prop="jobContentExclude" style="margin-left: 0;">
                    <template #label>
                        <el-checkbox v-model="userStore.user.preference.csrE" label="" size="large"/>
                        公司规模范围
                    </template>
                    <el-input v-model="userStore.user.preference.csr" placeholder="公司规模范围 例:10-5000"
                              style="width: 242px"/>
                </el-form-item>
            </div>

            <el-form-item label="AI过滤(语义匹配)" prop="aiFilter">
                <template #label>
                    <el-checkbox v-model="userStore.user.preference.afE" label="" size="large"/>
                    <el-tooltip effect="dark" raw-content content="
    批量投递时AI会通过你的提示词过滤筛选相应岗位<p/><span style='color:red;'>未在【产品列表】中购买【ai过滤】产品请勿开启,页面会报错
    </span><br/>过滤提示词举例：我希望找到武汉的java岗位，薪资至少20K，不考虑学历要求为本科及以下、或者需要超过10年工作经验的职位。
    </span><br/>与简历信息不互通，如果依赖您的某些信息，请通过提示词告知AI
    " placement="bottom">
                    AI 过滤(语义匹配)
                    </el-tooltip>
                </template>
                <el-input type="textarea" v-model="userStore.user.preference.af"/>
            </el-form-item>

            <el-form-item label="发送自定义招呼语" prop="jobContentExclude">
                <template #label>
                    <el-checkbox v-model="userStore.user.preference.cgE" label="" size="large"/>
                    发送自定义招呼语
                </template>
                <el-input type="textarea" v-model="userStore.user.preference.cg"/>
                <el-button size="small" type="primary" @click="generateGreet" :disabled="isGenerating">AI生成招呼语</el-button>
            </el-form-item>

            <el-form-item label="发送图片简历" prop="jobContentExclude" class="form-item-upload" style="margin-left: 0;">
                <template #label>
                    <el-checkbox v-model="userStore.user.preference.cIE" label="" size="large"/>
                    发送图片简历&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                </template>

                <el-upload
                    action="https://www.zhipin.com/wapi/zpupload/image/uploadSingle"
                    :before-upload="beforeUpload"
                    :on-success="handleUploadSuccess"
                    :show-file-list="false"
                    :data="uploadData"
                    :headers='{"Zp_token": Tools.getCookieValue("bst")}'>
                    <el-button size="small" type="primary">选择图片简历</el-button>
                </el-upload>
                <el-tag v-if="userStore.user.preference.cI" type="success" size="small" style="margin-left: 5px;">已上传</el-tag>
            </el-form-item>

            <div style="display: flex;margin-bottom: 10px;">
                <el-checkbox v-model="userStore.user.preference.fhE" label="" size="large">过滤猎头</el-checkbox>
                <el-checkbox v-model="userStore.user.preference.polE" label="" size="large">仅投递boss在线岗位
                </el-checkbox>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <p class="time-interval">投递间隔</p>
                <el-input-number v-model="userStore.user.preference.pi" :min="3" :max="60"
                                 size="small"></el-input-number>
                <p class="time-interval">秒</p>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <p class="time-interval">翻页间隔</p>
                <el-input-number v-model="userStore.user.preference.npi" :min="6" :max="60"
                                 size="small"></el-input-number>
                <p class="time-interval">秒</p>
            </div>

            <el-text class="mx-1 top-title" type="warning">交互设置</el-text>

            <el-form-item label="预测问题" prop="jobContentExclude" style="margin-top: 10px;">
                <template #label>
                    <el-checkbox v-model="userStore.user.preference.ppE" label="" size="large"/>
                    预设问题&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                </template>
                <el-input type="textarea" v-model="userStore.user.preference.pp"/>
            </el-form-item>

            <el-form-item label="拒绝挽留" prop="jobContentExclude">
                <template #label>
                    <el-checkbox v-model="userStore.user.preference.rfE" label="" size="large"/>
                    拒绝挽留&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                </template>
                <el-input type="textarea" v-model="userStore.user.preference.rf"/>
            </el-form-item>

            <div style="display: flex;">
                <el-checkbox style="padding-top: 6px" v-model="userStore.user.preference.hiaE" label="" size="large">高意向停止AI坐席
                </el-checkbox>
                <el-text type="primary" style="margin-top: -20px;">&nbsp;&nbsp;高意向条件:</el-text>
                <el-form-item label="对话聊天轮数" prop="crC" style="margin-left:-30px;">
                    <template #label>
                        <el-text class="mx-1" type="primary" style="margin-top: 5px;">对话轮数 >=</el-text>
                    </template>
                    <el-text class="mx-1" type="primary" style="margin-top: 5px;">
                        <el-input type="number" style="width: 50px" size="small"
                                  v-model="userStore.user.preference.crC"/>
                    </el-text>

                    <el-form-item label="对话聊天轮数关键字" prop="crC" style="margin-left: 0;margin-top: 3px;">
                        <template #label>
                            <el-text class="mx-1" type="primary">OR&nbsp;&nbsp;&nbsp;包含关键字</el-text>
                        </template>
                        <el-select v-model="userStore.user.preference.crK"
                                   multiple
                                   filterable
                                   remote
                                   allow-create
                                   default-first-option
                                   :reserve-keyword="false"
                                   placeholder="包含关键字"
                                   style="min-width:200px;width: 100%">
                            <el-option v-for="(item,inx) in ['请输入包含关键字']"
                                       :key="inx"
                                       :label="item"
                                       :value="item"/>
                        </el-select>
                    </el-form-item>
                </el-form-item>
            </div>

            <el-form-item>

                <el-checkbox v-model="userStore.user.preference.drE" label="" size="large">AI坐席延迟回复
                </el-checkbox>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <el-input-number v-model="userStore.user.preference.dr" :min="0" :max="30"  size="small"></el-input-number>
                &nbsp;秒
            </el-form-item>

            <el-text class="mx-1 top-title" type="warning">邮件通知</el-text>
            <div style="display: flex;margin-top: 10px">
                <el-checkbox v-model="userStore.user.preference.ermE" label="" size="large">每轮交流邮件通知
                </el-checkbox>
                <el-checkbox v-model="userStore.user.preference.crE" label="" size="large">
                    <el-text class="mx-1" type="danger">高意向邮件通知</el-text>
                </el-checkbox>
            </div>

            <el-form-item>
                <el-button type="primary" @click="submitForm(ruleFormRef)">保存偏好设置</el-button>
                <el-button @click="resetForm(ruleFormRef)">清除偏好设置</el-button>
                <el-button @click="exportSetting">导出偏好设置</el-button>
                <el-button @click="importSetting">导入偏好设置</el-button>
            </el-form-item>
        </div>
    </el-form>
</template>

<script lang="ts" setup>
import {inject, reactive, ref} from 'vue'
import {FormInstance, FormRules, ElNotification, ElMessageBox} from 'element-plus';
import {ElMessage} from "../../utils/tools";
import {UserStore} from '../../stores'
import {AxiosInstance} from "axios";
import {PreferenceConfig} from "../../stores/types";
import {loginInterceptor} from "../../utils/tools";
import {Tools} from "../../platform/utils";
import {AbsPlatform} from "../../platform/platform";

import {ServerStore} from "../../stores/server";
import {TampermonkeyApi} from "../../platform/utils";

const axios = inject('$axios') as AxiosInstance
const platform = inject('$platform') as AbsPlatform;
const userStore = UserStore();
const serverStore = ServerStore();

interface RuleForm {
    phone: string,
    email: string,
    companyIncludeEnable: boolean,
    companyInclude: string[],

    companyExcludeEnable: boolean,
    companyExclude: string[],

    jobNameIncludeEnable: boolean,
    jobNameInclude: string[],

    jobContentExcludeEnable: boolean,
    jobContentExclude: string[],

    salaryRangeEnable: boolean,
    salaryType: string,
    salaryRange: string,

    companyScaleRangeEnable: boolean,
    companyScaleRange: string,

    sendCustomizeGreetEnable: boolean,
    customizeGreet: string,
}

const ruleFormRef = ref<FormInstance>()


const validateEmail = (rule: any, value: string, callback: (error?: Error) => void) => {
    if (value === '') {
        callback(new Error('请输入邮箱'));
    } else if (!/^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(value)) {
        callback(new Error('请输入正确的邮箱'));
    } else {
        callback();
    }
};

const rules = reactive<FormRules<RuleForm>>({
    phone: [{required: true, message: '请输入手机号；作为偏好设置唯一键', trigger: 'blur'}],
    email: [{
        required: true,
        message: '请输入邮件地址；将通过邮件通知您投递进度',
        validator: validateEmail,
        trigger: 'blur'
    }],
})


const exportSetting = async () => {
    const preference = { ...userStore.user.preference };
    const exportData = JSON.stringify(preference, null, 2);
    try {
        await navigator.clipboard.writeText(exportData);
        ElNotification({
            title: '导出成功',
            message: '偏好设置已复制到剪贴板',
            type: 'success',
            duration: 2000
        });
    } catch (error) {
        ElNotification({
            title: '导出失败',
            message: '复制到剪贴板时出错',
            type: 'error',
            duration: 2000
        });
    }
}

const importSetting = async () => {
    ElMessageBox.prompt('请粘贴导出的偏好设置配置', '导入偏好设置', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '在此粘贴配置内容',
    }).then(({ value }) => {
        try {
            const importedPreference = JSON.parse(value);
            userStore.user.preference = { ...importedPreference };
            ElNotification({
                title: '导入成功',
                message: '偏好设置已导入，请点击保存偏好设置以持久化保存',
                type: 'success',
                duration: 3000
            });
        } catch (error) {
            ElNotification({
                title: '导入失败',
                message: '配置格式错误，请检查后重试',
                type: 'error',
                duration: 2000
            });
        }
    }).catch(() => {});
}

const submitForm = async (formEl: FormInstance | undefined) => {
    if (!loginInterceptor()) {
        return;
    }
    if (!formEl) return
    if (!userStore.user.phone || !userStore.user.email) {
        ElMessage({
            message: "请填写手机号或邮箱",
            type: 'error',
            duration: 2000
        })
    }
    let valid = await formEl.validate((valid, fields) => {
        return valid;
    })

    if (!valid) {
        return;
    }

    // 无论是否在线，都先更新本地镜像和全局最新镜像
    const mirrorKey = serverStore.getMirrorKey('user_config')
    const globalMirrorKey = serverStore.getGlobalMirrorKey('user_config')
    TampermonkeyApi.GmSetValue(mirrorKey, userStore.user)
    TampermonkeyApi.GmSetValue(globalMirrorKey, userStore.user)

    await axios.post("/api/user/save/preference", {
        ...userStore.user,
        aiSeatStatus: userStore.user.aiSeatStatus ? 1 : 0
    })
        .then(resp => {
            ElMessage({
                message: "偏好设置已同步到服务器",
                type: 'success',
                duration: 2000
            })
        })
        .catch(err => {
            ElNotification({
                title: '保存至本地',
                message: '由于服务器离线，配置仅在本地生效。连接恢复后请重新同步。',
                type: 'warning',
                duration: 4000
            });
        })
}

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    userStore.user.email = "";
    userStore.user.preference = {} as PreferenceConfig;
}

// 图片简历功能
const firstFile = ref<File | null>(null);
let jobDetail: any = platform.getFistJobDetail()
const uploadData = {
    securityId: jobDetail?.securityId,
    // securityId: BossOption.bossUserInfoMap?.values()?.next()?.value.securityId,
    source: 'chat_file',
};
const beforeUpload = (file: File) => {
    firstFile.value = file;
    return true;
};

const handleUploadSuccess = async (response: any) => {
    userStore.user.preference.cI = response.zpData.url + "===" + response.zpData.tinyUrl;
    ElMessage({
        message: "图片简历上传成功；点击下方保存偏好设置可持久保存",
        type: 'success',
        duration: 3000
    })
};

const isGenerating = ref(false);

function debounceImmediate(func: Function, wait: number) {
    let timeout: number | null = null;
    return (...args: any[]) => {
        if (!timeout) {
            func(...args);
        }
        if (timeout !== null) {
            clearTimeout(timeout);
            ElNotification({
                message: '请勿频繁点击，请等待',
                type: 'warning',
                duration: 1000
            });
        }
        timeout = window.setTimeout(() => {
            timeout = null;
        }, wait);
    };
}

const generateGreet = debounceImmediate(async () => {
    if (isGenerating.value) return;
    isGenerating.value = true;

    try {
        const response = await axios.post('/api/job/ai/assistant/generate/greeting', {}, {timeout: 30000});
        userStore.user.preference.cg = response.data.data;
        ElNotification({
            message: '招呼语生成成功，请点击下方保存偏好设置',
            type: 'success',
            duration: 2000
        });
    } catch (error) {
        ElNotification({
            title: 'AI问候语生成失败',
            message: error as string,
            type: 'error',
            duration: 3000
        });
    } finally {
        isGenerating.value = false;
    }
}, 15000);


/**
 * 偏好设置默认值处理
 */
const preferenceDefaultValueHandler = () => {
    // ai坐席延迟回复
    if (!userStore.user.preference.dr) {
        userStore.user.preference.dr = 0;
    }
}

preferenceDefaultValueHandler()

</script>

<style scoped>

.input-opt > :first-child {
    width: 100px;
}

.form-item-upload > :first-child {
    margin-left: 0;
}

.el-input-number--small {
    line-height: 22px;
    width: 80px;
}

.time-interval{
    margin-top: 10px;
    margin-right: 1px;
    margin-left: 1px;
}


</style>
