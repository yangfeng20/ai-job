<template>
    <el-dialog v-model="productStore.showProduct" :show-close="false" width="800">
        <template #header="{ close }">
            <div class="my-header">
                <el-text size="large" style="font-size: 20px" type="info">产品列表</el-text>
                <el-button type="warning" @click="close">
                    <el-icon class="el-icon--left">
                        <CircleCloseFilled/>
                    </el-icon>
                    关闭
                </el-button>
            </div>

            <!--已购买产品-->
            <div v-show="buyProductList.length>0">
                <br>
                <h3>我的产品列表</h3>
                <br>
                <el-table v-show="buyProductList.length>0" :data="buyProductList" stripe style="width: 100%">
                    <el-table-column prop="productName" label="产品" width="180">
                        <template v-slot="{ row }">
                            <span :style="{ textDecoration: isExpired(row) ? 'line-through' : 'none' }">
                                {{ row.productName }}
                            </span>
                        </template>
                    </el-table-column>

                    <!-- 状态列 -->
                    <el-table-column label="状态" width="100">
                        <template v-slot="{ row }">
                            <span :style="{ color: isExpired(row) ? 'red' : 'green' }">
                                {{ isExpired(row) ? '过期' : '正常' }}
                            </span>
                        </template>
                    </el-table-column>

                    <el-table-column prop="powerList" label="能力" width="180">
                        <template v-slot="{ row }">
                            <div v-for="power in row.powerList" :key="power">
                                <el-tag effect="dark" :type="randomStyle()" size="small">{{ power }}</el-tag>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="periodOfValidityStartTime" label="有效期开始时间"/>
                    <el-table-column prop="periodOfValidityEndTime" label="有效期结束时间"/>
                </el-table>
                <br>
            </div>

            <!--            搜索展示不做条件限制-->
            <!--            <div v-show="!showOtherProduct" type="info">-->
            <div type="info" style="margin-top: 10px">
                <el-button type="danger" :icon="Shop" @click="showOrderGroup" :loading="productGroupLoading">
                    更多产品
                </el-button>
                <el-input :suffix-icon="Wallet" v-model="promotionCode" style="margin-left: 10px;width: 240px"
                          placeholder="请输入您的优惠码"/>
                <el-link :icon="PriceTag" type="primary" style="margin-left: 30px;" target="_blank"
                         href="https://www.bilibili.com/video/BV1HKAyebESp">点击获取优惠码(评论区)
                </el-link>

            </div>

            <!--订单组二维码-->
            <div v-if="showOtherProduct">
                <br>
                <p>
                    <el-text class="mx-1" type="danger">定价说明：</el-text>
                    使用R1深度思考大模型时：首先，R1的价格更贵，深度思考的内容也会被记录token消耗。token消耗量巨大。同时由于boss的会话聊天机制，需要携带消息上下文调用。这也就意味着对话轮数越多，token消耗越多。按乘方的趋势增长。
                </p>
                <br>
                <div v-for="order in orderGroup" :key="order" style="display: flex" class="block"
                     :style="'width: '+1/orderGroup.length">
                    <!--订单标题-->
                    <div style="padding-top: 10px;min-width: 8%;">
                        <p class="demonstration">
                            <el-text size="large" type="primary">{{ order.title }}</el-text>
                        </p>
                        <p class="demonstration">
                            <el-text size="large" type="success">{{ order.validDays }}天</el-text>
                        </p>
                        <p class="demonstration">
                            <el-text size="large" type="danger">￥ {{ order.totalAmount }}</el-text>
                        </p>
                    </div>

                    <!--图片二维码-->
                    <el-image style="width: 100px; height: 100px" :src="'data:image/png;base64,'+order.qrCodeBase64"
                              fit="fill">
                        <template #error>
                            <div class="image-slot">加载订单二维码失败；请稍后刷新重试</div>
                        </template>
                    </el-image>

                    <div style="width: 80%">
                        <!--产品能力标签-->
                        <div>
                            提供能力:
                            <el-tag style="margin: 10px;" v-for="tag in order.tags" :key="tag" :type="randomStyle()"
                                    size="large" effect="light">{{ tag }}
                            </el-tag>
                        </div>
                        <!--产品推广描述-->
                        <div>
                            <span class="demonstration">{{ order.desc }}</span>
                        </div>
                    </div>
                </div>
            </div>

        </template>
    </el-dialog>
</template>

<script setup lang="ts">

import {PriceTag, Shop, Wallet, CircleCloseFilled} from "../icons";
import {inject, Ref, ref, watch} from "vue";
import {AxiosInstance} from "axios";
import {ElMessage, loginInterceptor} from "../../utils/tools";
import {SSEClient} from "../../utils/sse";
import logger from "../../logging";
import {ProductStore} from "../../stores";
import {Pair} from "../../types";

const productStore = ProductStore()

const axios = inject('$axios') as AxiosInstance
const productGroupLoading = ref(false)

// 已经购买产品
const buyProductList = ref([])

// 显示其他产品
const showOtherProduct = ref(true)
const orderGroup: Ref = ref([])
const payStatus = ref(false)
const promotionCode = ref('')
const lastPromotionCode = ref('')

let pairList: Pair<any, any> [] = []

// --------------------------------------------------函数定义-------------------------------------------------------------

const isExpired = (row: any): boolean => {
    const currentTime = new Date();
    const endTime = new Date(row.periodOfValidityEndTime);
    return currentTime > endTime;
}


const randomStyle = (): string => {
    const tagStyleArr = ['primary', 'warning', 'success', 'danger']
    let number = Math.floor(Math.random() * 4);
    return tagStyleArr[number];
}


// --------------------------------------------------函数定义-------------------------------------------------------------


const queryBuyProductList = async () => {
    // 已购买产品集合
    let productResp = await axios.post("/api/product/user/product/list")
    buyProductList.value = productResp.data.data
}

const openProductDialog = async () => {

    // if (buyProductList.value.length <= 0) {
    // }
    await queryBuyProductList()

    if (buyProductList.value.length > 0) {
        // 显示产品集合
        return;
    }
    showOtherProduct.value = false
    // 没有产品，直接调用接口生成订单组
    await showOrderGroup()
}

const showOrderGroup = async () => {
    if (!loginInterceptor()) {
        return;
    }
    productGroupLoading.value = true
    let promotionCodeVar = promotionCode.value.trim()
    promotionCode.value = ''
    setTimeout(() => {
        showOtherProduct.value = true;
    }, 100)
    // 如果之前生成过订单，或者和上次的优惠码一致时，则不再生成订单
    if (orderGroup.value.length < 1 || promotionCodeVar !== lastPromotionCode.value) {
        // 生成订单组
        let orderGroupResp: any;
        try {
            orderGroupResp = await axios.post("/api/pay/generate/order/group", {promotionCode: promotionCodeVar});
        } catch (e) {
            productGroupLoading.value = false
            return;
        }
        if (orderGroupResp.data.code != 200) {
            ElMessage({
                message: orderGroupResp.data.message,
                type: 'warning',
                duration: 3000
            })
            setTimeout(() => {
                showOtherProduct.value = false;
            }, 100)
            productGroupLoading.value = false
            return;
        }
        orderGroup.value = orderGroupResp.data.data
        lastPromotionCode.value = promotionCodeVar
        productGroupLoading.value = false

        waitUsePay()
    }
    productGroupLoading.value = false
}

const waitUsePay = () => {
    // 建立sse连接，用于服务端通知前端订单支付成功
    const sseClient = new SSEClient(axios.defaults.baseURL + 'api/sse/connect');
    sseClient.addOnMsgCallback((event: any) => {
        let data = event.data;
        if (data === "支付成功") {
            // 支付成功，清除之前的付款二维码
            payStatus.value = true
            orderGroup.value = []
            queryBuyProductList()
            showOtherProduct.value = false
            // firstAiSeatStatus.value = 0;
            productStore.setShowProduct(false)
        }
    })
    sseClient.start();

    // 半分钟之后主动查询订单状态
    let count = 0;
    let interval = setInterval(() => {
        if (payStatus.value) {
            // sse通知订单已经支付成功，取消轮询查询订单
            clearInterval(interval)
        }
        orderGroup.value.forEach((orderItem: any) => {
            axios.get("/api/pay/searchOrder?outTradeNo=" + orderItem.orderId).then(resp => {
                if (resp.data.data === "TRADE_SUCCESS") {
                    payStatus.value = true
                    orderGroup.value = []
                    clearInterval(interval)
                }
                if (resp.data.data === "WAIT_BUYER_PAY") {
                    logger.debug("等待支付")
                }

                count++
                if (count >= 10) {
                    logger.warn("订单超时未支付")
                    clearInterval(interval)
                }
            })
        })
    }, 30000);

    let pair: Pair<any, any> = {
        key: sseClient,
        value: interval
    }
    pairList.push(pair)
}


watch(
    () => productStore.showProduct,
    (newVal) => {
        if (newVal) {
            openProductDialog()
        } else {
            pairList.forEach((pair: Pair<any, any>) => {
                if (pair && pair.key instanceof SSEClient) {
                    (pair.key as SSEClient).close()
                    clearInterval(pair.value)
                }
            })
        }
    }
);


</script>

<style scoped>
.my-header {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    gap: 16px;
}
</style>