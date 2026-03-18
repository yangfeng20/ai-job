<template>
    <div class="container">
        <el-card class="box-card">
            <template #header>
                <div class="card-header">
                    <h3>邀请码</h3>
                    <div style="display: flex">
                        <span style="margin-right: 15px; font-size: 14px; color: #909399">
                            当前可兑换邀请人数: {{ activeTrialsCount }}
                        </span>
                        <h3>产品兑换</h3>
                    </div>
                </div>
            </template>

            <el-row>
                <el-col :span="12">
                    <div class="grid-content ep-bg-purple">
                        <el-text class="info-item">您的邀请码: {{ userStore.user.inviteCode }}</el-text>

                        <el-form :inline="true" class="bind-form">
                            <el-form-item label="绑定邀请码">
                                <el-input
                                    v-model="bindInviteCode"
                                    placeholder="请输入邀请码"
                                    :clearable="bindInviteCodeStatus"
                                    :disabled="bindInviteCodeStatus"
                                />
                            </el-form-item>
                            <el-form-item>
                                <el-button type="primary" @click="bindInvite" v-if="!bindInviteCodeStatus">
                                    立即绑定
                                </el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </el-col>
                <el-col :span="12">

                    <div class="grid-content ep-bg-purple-light">

                        <el-button-group class="exchange-group" style="justify-content: flex-end;">
                            <el-popconfirm class="box-item" title="即将兑换，将归档1位邀请用户" placement="top"
                                           @confirm="exchangeProduct(-1)">
                                <template #reference>
                                    <el-button
                                        type="primary" :disabled="activeTrialsCount < 1">
                                        兑换50次AI坐席 (邀请1人)
                                    </el-button>
                                </template>
                            </el-popconfirm>

                            <el-popconfirm class="box-item" title="即将兑换，将归档2位邀请用户" placement="top"
                                           @confirm="exchangeProduct(1)">
                                <template #reference>
                                    <el-button
                                        type="primary" :disabled="activeTrialsCount < 2">
                                        兑换1天尝鲜版 (邀请2人)
                                    </el-button>
                                </template>
                            </el-popconfirm>

                            <el-popconfirm class="box-item" title="即将兑换，将归档4位邀请用户" placement="top"
                                           @confirm="exchangeProduct(2)">
                                <template #reference>
                                    <el-button
                                        type="primary" :disabled="activeTrialsCount < 4">
                                        兑换7天基础版 (邀请4人)
                                    </el-button>
                                </template>
                            </el-popconfirm>
                        </el-button-group>
                    </div>
                </el-col>
            </el-row>
        </el-card>

        <el-card class="box-card">
            <template #header>
                <div class="card-header">
                    <h3>产品试用</h3>
                    <h3>邀请列表</h3>
                </div>
            </template>

            <el-row>
                <el-col :span="12">
                    <div class="grid-content ep-bg-purple">
                        <el-table :data="trialList" style="width: 100%">
                            <el-table-column prop="productType" label="产品类型" width="120">
                                <template #default="{ row }">
                                    <el-tag type="danger">AI坐席</el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column prop="trialCount" label="剩余额度" width="120"/>
                            <el-table-column prop="desc" label="描述" width="140"/>
                        </el-table>
                    </div>
                </el-col>
                <el-col :span="12">
                    <div class="grid-content ep-bg-purple-light">
                        <el-table :data="inviteList" style="width: 100%;">
                            <el-table-column prop="beInviteeUsername" label="用户名" width="120"/>
                            <el-table-column label="状态" width="120">
                                <template #default="{ row }">
                                    <el-tag :type="row.status === 1 ? 'success' : 'info'">
                                        {{ row.status === 1 ? '正常' : '归档' }}
                                    </el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column prop="createdDate" label="绑定时间" width="160"/>
                        </el-table>
                    </div>
                </el-col>
            </el-row>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import {AxiosInstance} from "axios";
import {inject, onMounted, Ref, ref} from "vue";
import {Tools} from "../../platform/utils";
import logger from '../../logging';
import {UserStore} from "../../stores";
import {ElNotification} from "element-plus";

const axios = inject('$axios') as AxiosInstance;

const userStore = UserStore();

let bindInviteCode = ref(userStore.user.bindInviteCode);
let bindInviteCodeStatus = ref(!!userStore.user.bindInviteCode);

const trialList: Ref<any[]> = ref([]);
const inviteList: Ref<any[]> = ref([]);

const activeTrialsCount = ref(0);

onMounted(async () => {
    await fetchTrialList();
    await fetchInviteList();
});

const fetchTrialList = async () => {
    try {
        const response = await axios.get('/api/user/trial/aiSeat/list');
        trialList.value = response.data.data;
    } catch (error) {
        logger.error('Failed to fetch trial list', error);
    }
};

const fetchInviteList = async () => {
    try {
        const response = await axios.post('/api/user/invites/list');
        inviteList.value = response.data.data;
        activeTrialsCount.value = inviteList.value.filter(trial => trial.status === 1).length;
    } catch (error) {
        logger.error('Failed to fetch invite list', error);
    }
};

const bindInvite = async () => {
    try {
        let param = `?inviteCode=${bindInviteCode.value}&name=${Tools.window._PAGE.name}`
        await axios.post('/api/user/invites/bind/code' + param);
        userStore.user.bindInviteCode = bindInviteCode.value;
        ElNotification({
            message: "绑定邀请码成功",
            type: 'success',
            duration: 2000
        });
        bindInviteCodeStatus.value = true;
        // 不刷新用户邀请列表，因为当前用户是被邀请者
    } catch (error) {
        logger.error('Failed to bind invite code', error);
        ElNotification({
            message: error as string,
            type: 'error',
            duration: 2000
        });
    }
};

const exchangeProduct = async (productId: number) => {
    try {
        let param = `?product=${productId}`
        await axios.post('/api/user/invites/exchange/products' + param);
        ElNotification({
            message: "产品兑换成功;请前往AI助手页面打开",
            type: 'success',
            duration: 3000
        });
        if (productId === -1) {
            // 刷新用户试用列表
            await fetchTrialList();
        }
        // 刷新未归档用户数量
        await fetchInviteList()
    } catch (error) {
        ElNotification({
            message: error as string,
            type: 'error',
            duration: 2000
        });
    }
};
</script>

<style scoped>
.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0;
}

.box-card {
    margin-bottom: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.bind-form {
    margin-top: 20px;
}

.info-item {
    font-size: 16px;
    margin-bottom: 10px;
    display: block;
}

.exchange-group {
    display: flex;
    gap: 10px;
}

/* 响应式布局 */
@media (max-width: 768px) {
    .container {
        padding: 10px;
    }

    .exchange-group {
        flex-direction: column;
    }

    .el-form--inline .el-form-item {
        margin-right: 0;
        margin-bottom: 10px;
    }
}
</style>