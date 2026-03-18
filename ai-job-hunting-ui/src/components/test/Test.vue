<template>
    <h1>Test模块</h1>
    <br>
    <el-input
        v-model="msg"
        style="max-width: 600px"
        placeholder="发送消息内容">
        <template #append style="width: 600px">
            <el-button type="primary" style="width: 600px" @click="handlerClick">发送消息和简历</el-button>
        </template>
    </el-input>


    <br/>

    <!-- 上传图片并通过ws发送-->
    <el-upload
        action="https://www.zhipin.com/wapi/zpupload/image/uploadSingle"
        :before-upload="beforeUpload"
        :on-success="handleUploadSuccess"
        :show-file-list="false"
        :data="uploadData"
        :headers='{"Zp_token": Tools.getCookieValue("bst")}'>
        <el-button type="primary">选择图片并发送</el-button>
    </el-upload>
    <el-button type="primary" @click="scrollDown">向下滑动</el-button>

    <br/>
    <el-button plain @click="open"> AI坐席回复通知弹框</el-button>

</template>

<script setup lang="ts">
import {BossOption} from "../../platform/bossPlatform";
import {inject, ref} from "vue";
import axios from "../../axios";
import {
    scrollElementToBottom,
    Tools
} from "../../platform/utils";
import {AbsPlatform} from "../../platform/platform";
import {ElNotification} from "element-plus";
import logger from "../../logging";

const platform = inject('$platform') as AbsPlatform;
let bossOption: BossOption = new BossOption()

let msg = ref<string>("");

const scrollDown = () => {
    // 等待click
    scrollElementToBottom(document.querySelector(".job-list-container"))
};

let template = `
<style>
.chat-container {
    max-width: 1000px;
    margin: 20px auto;
    font-family: Arial, sans-serif;
}

.message {
    display: flex;
    margin: 10px 0;
    align-items: start;
}

.message-user {
    justify-content: flex-start;
    margin-left: -70px;
}

.message-assistant {
    justify-content: flex-end;
    margin-right: -46px;
}

.avatar-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin: 0 15px;
    width: 65px;
}

.avatar {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    object-fit: cover;
    margin-bottom: 5px;
}

.user-info {
    font-size: 10px;
    font-weight: bold;
    color: #666;
    text-align: center;
    line-height: 1.3;
    max-width: 65px;
    word-break: break-word;
}

.content {
    max-width: 80%;
    padding: 10px 8px;
    border-radius: 12px;
    line-height: 1.5;
    font-size: 10px;
}

.user-content {
    background: #f1f0f0;
    color: #333;
    margin-left: -20px;
}

.assistant-content {
    background: #96d1d1;
    color: #333;
    margin-right: -20px;
}
</style>

<div class="chat-container">
    <!-- 用户提问 -->
    <div class="message message-user">
        <div class="avatar-container">
            <img class="avatar" src="{{user_avatar}}" alt="提问者头像">
            <span class="user-info">{{user_name}}</span>
        </div>
        <div class="content user-content">
            {{user_question}}
        </div>
    </div>

    <!-- 助理回答 -->
    <div class="message message-assistant">
        <div class="content assistant-content">
            {{assistant_answer}}
        </div>
        <div class="avatar-container">
            <img class="avatar" src="{{assistant_avatar}}" alt="回答者头像">
            <span class="user-info">{{assistant_name}}</span>
        </div>
    </div>
</div>

<!-- 变量替换示例：
{{user_avatar}} → "https://example.com/user.jpg"
{{user_name}} → "张三"
{{user_question}} → "如何快速掌握响应式布局设计？需要具体的学习路线建议"
{{assistant_avatar}} → "https://example.com/bot.png"
{{assistant_name}} → "AI助手"
{{assistant_answer}} → "建议分三步学习：1. 掌握媒体查询... 2. 学习弹性盒子布局... 3. 实践栅格系统..."
-->
`;

template = template.replace(/{{user_avatar}}/g, '');
template = template.replace(/{{assistant_avatar}}/g, '');

template = template.replace(/{{user_question}}/g, '如何学习前端开发？');
template = template.replace(/{{assistant_answer}}/g, '建议从HTML/CSS开始，然后学习Java...？你觉得呢');

template = template.replace(/{{user_name}}/g, '张三里');
template = template.replace(/{{assistant_name}}/g, 'AI助手');

const debugBossId = ref(111111)

const open = () => {
    ElNotification({
        type: 'success',
        title: 'AI坐席回复',
        showClose: false,
        duration: 0,
        dangerouslyUseHTMLString: true,
        message: template,
    })
}

const handlerClick = () => {
    if (msg.value === "") {
        return;
    }
    bossOption.sendMsg(debugBossId.value, msg.value, undefined)
    bossOption.sendResumeFile(debugBossId.value)
}

const firstFile = ref<File | null>(null);
let jobDetail: any = platform.getFistJobDetail()
const uploadData = {
    securityId: jobDetail?.securityId,
    source: 'chat_file',
};
const beforeUpload = (file: File) => {
    firstFile.value = file;
    return true;
};

const handleUploadSuccess = async (response: any) => {
    console.log('上传图片到boss成功', response);

    bossOption.sendMsg(debugBossId.value, "", {
        originImage: response.zpData.url,
        tinyImage: response.zpData.tinyUrl,
    })
    console.log('发送图片成功');
};

</script>

<style scoped>

</style>
