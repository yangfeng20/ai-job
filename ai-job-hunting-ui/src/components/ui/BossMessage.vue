<template>
    <br/>
    <el-button style="margin-left: 10px;" type="success" @click="handlerClick">重启当前会话AI坐席</el-button>
    
    <!-- 批量发送悬浮框（自定义非遮罩，不阻塞背景点击） -->
    <div v-if="batchSendDialogVisible" class="batch-send-float">
        <el-input
            v-model="batchMessageText"
            type="textarea"
            :rows="4"
            placeholder="请输入要发送的消息内容"
        />
        <div class="dialog-footer" style="margin-top: 10px; text-align: right;">
            <el-button @click="onCancel">取消</el-button>
            <el-button type="primary" @click="sendBatchMessage">发送</el-button>
        </div>
    </div>
</template>

<script setup lang="ts">
import {AiPower} from "../../platform/aiPower";
import {ElMessage} from "../../utils/tools";
import {BossOption} from "../../platform/bossPlatform";
import {ref} from "vue";
import {Message} from "../../webSocket/protobuf";
import {Tools} from "../../platform/utils";

// 批量发送相关状态
const batchSendDialogVisible = ref(false)
const batchMessageText = ref('')

// 统一清理批量UI
const cleanupBatchUI = () => {
    const checkboxes = document.querySelectorAll('.batch-checkbox')
    checkboxes.forEach(checkbox => (checkbox as HTMLElement).remove())
    const selectedElements = document.querySelectorAll('.batch-send-item')
    selectedElements.forEach(element => element.classList.remove('batch-send-item'))
}

const onCancel = () => {
    batchSendDialogVisible.value = false
    cleanupBatchUI()
    batchMessageText.value = ''
}

// 检查并创建批量发送按钮
const checkAndCreateBatchSendButton = () => {
    const labelList = document.querySelector('.label-list')
    if (!labelList) return
    
    // 检查是否已存在批量发送按钮
    const existingButton = labelList.querySelector('.batch-send-btn')
    if (existingButton) return
    
    // 创建批量发送按钮
    const batchSendButton = document.createElement('button')
    batchSendButton.className = 'batch-send-btn'
    batchSendButton.innerHTML = '批量发送消息'
    batchSendButton.style.cssText = `
        margin: 10px 0px;
        padding: 8px 8px;
        background-color: #6ead34;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 14px;
    `
    
    // 添加点击事件
    batchSendButton.addEventListener('click', () => {
        addCheckboxesToItems()
        batchSendDialogVisible.value = true
    })
    
    labelList.appendChild(batchSendButton)
}

// 为所有friend-content-warp元素添加勾选框
const addCheckboxesToItems = () => {
    const items = document.querySelectorAll('.friend-content-warp')
    
    items.forEach((item) => {
        // 检查是否已存在勾选框
        if (item.querySelector('.batch-checkbox')) return
        
        // 创建勾选框
        const checkbox = document.createElement('input')
        checkbox.type = 'checkbox'
        checkbox.className = 'batch-checkbox'
        checkbox.style.cssText = `
            margin-right: 8px;
            transform: scale(1.2);
        `
        // 阻止冒泡，避免触发父节点点击
        checkbox.addEventListener('click', (e) => {
            e.stopPropagation()
        })
        
        // 添加勾选事件
        checkbox.addEventListener('change', (e) => {
            const target = e.target as HTMLInputElement
            e.stopPropagation()
            if (target.checked) {
                item.classList.add('batch-send-item')
            } else {
                item.classList.remove('batch-send-item')
            }
        })

        // 将勾选框插入到元素的最前面
        let firstElementChild = item.firstElementChild as any;
        firstElementChild.insertBefore(checkbox, firstElementChild.firstChild)
    })
}

// 发送批量消息
const sendBatchMessage = () => {
    if (!batchMessageText.value.trim()) {
        ElMessage({
            type: 'warning',
            message: '请输入消息内容'
        })
        return
    }
    
    const selectedItems = document.querySelectorAll('.friend-content-warp.batch-send-item')
    
    if (selectedItems.length === 0) {
        ElMessage({
            type: 'warning',
            message: '请至少选择一个联系人'
        })
        return
    }
    
    // 发送消息给每个选中的联系人
    selectedItems.forEach((item) => {
        const vueInstance = (item as any).__vue__
        if (vueInstance && vueInstance.source) {
            const to_uid = vueInstance.source.uid
            const to_name = vueInstance.source.encryptBossId
            
            if (to_uid && to_name) {
                const message = new Message({
                    form_uid: Tools.window._PAGE.uid.toString(),
                    to_uid: to_uid.toString(),
                    to_name: to_name,
                    content: batchMessageText.value,
                    image: undefined,
                })
                message.send()
            }
        }
    })
    
    ElMessage({
        duration: 3000,
        type: 'success',
        message: `已发送消息给 ${selectedItems.length} 个联系人; 刷新页面查看结果`
    })
    
    // 清理状态
    batchMessageText.value = ''
    batchSendDialogVisible.value = false
    cleanupBatchUI()
}

setInterval(()=>{
    checkAndCreateBatchSendButton()
}, 1000)

const handlerClick = () => {

    const element = document.querySelector('.friend-content.selected') as any;
    const encryptJobId = element?.parentElement?.__vue__?.source?.encryptJobId;
    if (!encryptJobId) {
        ElMessage({
            type: 'info',
            message: '请先进入聊天窗口'
        })
        return;
    }

    const jobKey = BossOption.buildJobKey({encryptJobId: encryptJobId} as any);

    AiPower.updateAskStatus(jobKey, false).then(_ => {
        ElMessage({
            type: 'success',
            message: '已重新触发AI坐席'
        })
    })
}

</script>

<style scoped>
.batch-send-btn:hover {
    background-color: #337ecc !important;
}

.batch-checkbox {
    margin-right: 8px;
    transform: scale(1.2);
}

.batch-send-item {
    background-color: #f0f9ff !important;
    border: 2px solid #409eff !important;
}

.batch-send-float {
    position: fixed;
    right: 24px;
    bottom: 24px;
    width: 480px;
    padding: 16px;
    background: #ffffff;
    box-shadow: 0 6px 16px rgba(0,0,0,0.15);
    border-radius: 8px;
    z-index: 9999;
}
</style>
