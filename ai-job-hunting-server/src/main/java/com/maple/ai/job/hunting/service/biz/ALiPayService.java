package com.maple.ai.job.hunting.service.biz;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.config.AppPayConfig;
import com.maple.ai.job.hunting.emums.BizCodeEnum;
import com.maple.ai.job.hunting.emums.OrderStatusEnum;
import com.maple.ai.job.hunting.emums.ProductEnum;
import com.maple.ai.job.hunting.entity.OrderDO;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.mapper.OrderMapper;
import com.maple.ai.job.hunting.mapper.UserInfoMapper;
import com.maple.ai.job.hunting.mapper.UserProductMapper;
import com.maple.ai.job.hunting.model.OrderGenerateResult;
import com.maple.ai.job.hunting.model.bo.UserInfoDO;
import com.maple.ai.job.hunting.model.bo.UserProductDO;
import com.maple.ai.job.hunting.model.entity.GenerateOrderGroupConfigEntity;
import com.maple.ai.job.hunting.model.param.GenerateOrderGroupParam;
import com.maple.ai.job.hunting.model.vo.GenerateOrderVO;
import com.maple.ai.job.hunting.utils.QrCodeUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author maple
 * Created Date: 2024/5/8 21:46
 * Description:
 */

@Slf4j
@Service
public class ALiPayService {

    @Resource
    private AppPayConfig appPayConfig;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private UserProductMapper userProductMapper;
    @Resource
    private UserInfoMapper userInfoMapper;

    private final Snowflake snowflakeGenerate = IdUtil.getSnowflake();

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);


    @PostConstruct
    public void init() {
        // 可能存在支付成功，但是回调异常；最终我方系统中还是未支付，支付宝系统已经支付成功。这种需要手动处理
        log.info("启动时向支付宝取消历史超时订单");
        scheduledExecutorService.scheduleAtFixedRate(this::timeoutCancelOrder, 0, 1, TimeUnit.HOURS);

        // 每天晚上23:55执行订单汇总分析
        scheduleDailyOrderSummary();
    }

    private void scheduleDailyOrderSummary() {
        long initialDelay = computeInitialDelay();
        long period = TimeUnit.DAYS.toMillis(1);
        scheduledExecutorService.scheduleAtFixedRate(this::summarizeDailyOrders, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    private long computeInitialDelay() {
        Calendar nextRun = Calendar.getInstance();
        nextRun.set(Calendar.HOUR_OF_DAY, 23);
        nextRun.set(Calendar.MINUTE, 55);
        nextRun.set(Calendar.SECOND, 0);
        nextRun.set(Calendar.MILLISECOND, 0);

        long currentTime = System.currentTimeMillis();
        long nextRunTime = nextRun.getTimeInMillis();

        if (nextRunTime <= currentTime) {
            nextRun.add(Calendar.DAY_OF_MONTH, 1);
            nextRunTime = nextRun.getTimeInMillis();
        }

        return nextRunTime - currentTime;
    }

    private void summarizeDailyOrders() {
        // 获取当天的订单数据
        LambdaQueryWrapper<OrderDO> condition = new LambdaQueryWrapper<>();
        condition.ge(OrderDO::getCreatedDate, DateUtil.beginOfDay(new Date()));
        condition.lt(OrderDO::getCreatedDate, DateUtil.endOfDay(new Date()));

        List<OrderDO> orderDOList = orderMapper.selectList(condition);
        if (CollectionUtils.isEmpty(orderDOList)) {
            log.info("今天没有订单");
            return;
        }

        long totalOrders = orderDOList.size();
        long paidOrders = orderDOList.stream().filter(order -> OrderStatusEnum.PAID.getCode().equals(order.getStatus())).count();
        double paymentRate = (double) paidOrders / totalOrders * 100;

        log.info("今日订单总数: {}, 已支付订单数: {}, 支付率: {}%", totalOrders, paidOrders, paymentRate);
    }

    public OrderGenerateResult generateOrder(float totalAmount, String subject) {
        AlipayClient alipayClient = getAlipayClient();
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(appPayConfig.getNotifyUrl());

        JSONObject bizContent = new JSONObject();
        long bizOrderId = snowflakeGenerate.nextId();
        // 商家订单号
        bizContent.put("out_trade_no", bizOrderId);
        // 订单金额
        bizContent.put("total_amount", totalAmount);
        // 支付主题
        bizContent.put("subject", subject);
        request.setBizContent(bizContent.toString());

        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                log.error("调用支付宝生成订单qr码失败 msg:{} resp:{}", response.getMsg(), response);
                throw new ApplicationException("生成订单qr码失败 case: " + response.getMsg());
            }

            //生成支付二维码图片data:image/png;base64,
            String qrCode = response.getQrCode();
            BufferedImage image = QrCodeUtil.createImage(qrCode, appPayConfig.getLogoBase64());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", out);
            byte[] b = out.toByteArray();
            out.write(b);
            out.close();

            return new OrderGenerateResult(bizOrderId, qrCode, b);
        } catch (Throwable e) {
            throw new ApplicationException("生成订单失败；请稍后重试");
        }
    }


    public String paySuccessCallBack(Map<String, String> params) {
        if (CollectionUtils.isEmpty(params)) {
            log.error("支付成功回调参数为空");
            return "fail";
        }
        log.debug("支付成功回调参数 map:{}", params);

        try {
            //执行验签，确保结果是支付宝回调的，而不是被恶意调用，一定要做这一步
            boolean signVerified = AlipaySignature.rsaCheckV1(params, appPayConfig.getAlipayPublicKey(), appPayConfig.getCharset(), "RSA2");
            if (!signVerified) {
                //验签失败（很可能接口被非法调用）
                log.error("支付成功接口验签失败");
                return "fail";
            }

            //再次主动查询订单，不要只依赖支付宝回调的结果
            String bizOrderId = params.get("out_trade_no");
            JSONObject aliOrderResult = searchOrderByAli(bizOrderId, params.get("trade_no"));

            OrderDO orderDO = orderMapper.selectById(bizOrderId);
            if (orderDO == null) {
                log.error("订单 {} 不存在", bizOrderId);
                return "fail";
            }
            Long userId = orderDO.getUserId();

            String orderStatus = aliOrderResult.getString("trade_status");
            if ("TRADE_SUCCESS".equals(orderStatus) || "TRADE_FINISHED".equals(orderStatus)) {
                log.info("订单[{}]支付成功", bizOrderId);
                handlerOrderPaySuccess(bizOrderId);
                return userId + ":success";
            }
            log.info("订单[{}]为暂未支付成功 msg:{}", bizOrderId, aliOrderResult.getString("msg"));
            return userId + ":fail";

        } catch (AlipayApiException e) {
            log.error("支付成功接口回调异常", e);
            return "fail";
        }
    }

    private void handlerOrderPaySuccess(String orderId) {

        OrderDO orderDO = orderMapper.selectById(orderId);
        if (orderDO == null) {
            log.error("订单[{}]不存在", orderId);
            return;
        }

        UserProductDO userProductDO = new UserProductDO();
        userProductDO.setUserId(orderDO.getUserId());
        Integer type = orderDO.getType();
        ProductEnum productEnum = ProductEnum.getByCode(type);

        // 查询历史产品，累计产品时间
        List<UserProductDO> userProductDOList = userProductMapper.queryUserValidAllProduct(orderDO.getUserId());
        Date historyProductMaxValidityEndTime = userProductDOList.stream().filter(product -> product.getProductId().equals(type.longValue()))
                .max(Comparator.comparing(UserProductDO::getPeriodOfValidityEndTime))
                .map(UserProductDO::getPeriodOfValidityEndTime).orElse(new Date());

        userProductDO.setOrderId(orderDO.getId());
        userProductDO.setProductType(productEnum.getProductTypes().toString().trim());
        userProductDO.setProductId(type.longValue());
        // 累积同类产品的有效期时间
        userProductDO.setPeriodOfValidityStartTime(historyProductMaxValidityEndTime);
        userProductDO.setPeriodOfValidityEndTime(DateUtil.offsetDay(historyProductMaxValidityEndTime, productEnum.getDaysOfValidity()));

        orderDO.setStatus(OrderStatusEnum.PAID.getCode());
        orderMapper.updateById(orderDO);
        userProductMapper.insert(userProductDO);
        // 更新用户的AI坐席状态(0:未使用，1:使用中,null:未购买)
        userInfoMapper.update(new LambdaUpdateWrapper<UserInfoDO>().set(UserInfoDO::getAiSeatStatus, 0).eq(UserInfoDO::getId, orderDO.getUserId()));
    }

    /**
     * 封装一个订单查询
     * "WAIT_BUYER_PAY":交易创建，等待买家付款；"TRADE_CLOSED":未付款交易超时关闭，或支付完成后全额退款； "TRADE_SUCCESS":交易支付成功；"TRADE_FINISHED":交易结束，不可退款；
     *
     * @param outTradeNo 商户订单号
     * @param tradeNo    支付宝交易号。支付宝交易凭证号
     * @return 订单状态：String
     * @throws AlipayApiException AlipayApiException
     */
    public JSONObject searchOrderByAli(String outTradeNo, String tradeNo) throws AlipayApiException {
        AlipayClient alipayClient = getAlipayClient();

        AlipayTradeQueryModel bizQueryModel = new AlipayTradeQueryModel();
        bizQueryModel.setOutTradeNo(outTradeNo);
        bizQueryModel.setTradeNo(tradeNo);
        AlipayTradeQueryRequest aliRequest = new AlipayTradeQueryRequest();
        aliRequest.setBizModel(bizQueryModel);
        AlipayTradeQueryResponse response = alipayClient.execute(aliRequest);
        JSONObject responseObject = JSONObject.parseObject(response.getBody());
        return responseObject.getJSONObject("alipay_trade_query_response");
    }

    public void cancelOrder(String outTradeNo) {
        AlipayClient alipayClient = getAlipayClient();
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeCancelResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            throw new ApplicationException(e.getMessage());
        }
        if (!response.isSuccess()) {
            throw new ApplicationException("取消订单失败 case: " + response.getMsg());
        }
        String action = response.getAction();
        if (action != null) {
            log.warn("取消订单action:{}", action);
        }

    }

    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(appPayConfig.getGatewayUrl(), appPayConfig.getAppId(),
                appPayConfig.getMerchantPrivateKey(), "json", appPayConfig.getCharset(), appPayConfig.getAlipayPublicKey(), "RSA2");
    }

    public List<GenerateOrderVO> generateOrderGroup(GenerateOrderGroupParam generateOrderGroupParam) {
        Long userId = HeaderContext.getHeader().getUserId();

        GenerateOrderGroupConfigEntity generateOrderGroupConfig = appPayConfig.getGenerateOrderGroupConfig();
        List<GenerateOrderGroupConfigEntity.GenerateOrderConfigEntity> orderGenerateConfigList = generateOrderGroupConfig.getPromotionCodeMap().get(generateOrderGroupParam.getPromotionCode());
        if (StringUtils.isNotBlank(generateOrderGroupParam.getPromotionCode())) {
            // 校验优惠券码
            Optional.ofNullable(orderGenerateConfigList).orElse(Collections.emptyList())
                    .stream().filter(config -> new Date().before(config.getValidityDate()))
                    .findAny().orElseThrow(() -> new ApplicationException(BizCodeEnum.PROMOTION_CODE_EXPIRED));
        }
        if (CollectionUtils.isEmpty(orderGenerateConfigList)) {
            orderGenerateConfigList = generateOrderGroupConfig.getDefaultConfigList();
        }
        if (CollectionUtils.isEmpty(orderGenerateConfigList)) {
            throw new ApplicationException("没有配置生成订单的配置");
        }

        return orderGenerateConfigList.stream().map(generateOrderConfigEntity -> {
            // 调用支付宝生产qr订单二维码
            OrderGenerateResult orderGenerateResult = generateOrder(generateOrderConfigEntity.getTotalAmount(), generateOrderConfigEntity.getSubject());
            // bizOrderId入库
            orderMapper.insert(new OrderDO(orderGenerateResult.getOrderId(), OrderStatusEnum.PRE_GENERATE.getCode(),
                    userId, generateOrderConfigEntity.getProductId(), new BigDecimal(String.valueOf(generateOrderConfigEntity.getTotalAmount()))));

            // 返回二维码结果字节
            ProductEnum productEnum = ProductEnum.getByCode(generateOrderConfigEntity.getProductId());
            return new GenerateOrderVO(orderGenerateResult.getOrderId(), Base64Encoder.encode(orderGenerateResult.getQrCodeBytes()),
                    generateOrderConfigEntity.getTitle(), generateOrderConfigEntity.getAdText(), productEnum.getDaysOfValidity(),
                    productEnum.getProductTypeDescList(), generateOrderConfigEntity.getTotalAmount(), null);
        }).collect(Collectors.toList());
    }


    private void timeoutCancelOrder() {

        LambdaQueryWrapper<OrderDO> condition = new LambdaQueryWrapper<>();
        condition.eq(OrderDO::getStatus, OrderStatusEnum.PRE_GENERATE.getCode());
        // 订单创建时间小于1小时前的时间
        condition.lt(OrderDO::getCreatedDate, DateUtil.offsetHour(new Date(), -1));

        List<OrderDO> orderDOList = orderMapper.selectList(condition);
        if (CollectionUtils.isEmpty(orderDOList)) {
            log.info("无符合条件的历史订单需要取消");
            return;
        }
        orderDOList.forEach(orderDO -> {
            try {
                cancelOrder(orderDO.getId().toString());
                orderMapper.updateOrderStatus(orderDO.getId(), OrderStatusEnum.TIMEOUT_CLOSE);
                log.info("取消历史订单 orderId:{}", orderDO.getId());
            } catch (Exception e) {
                log.error("取消订单失败 orderId:{} case: {}", orderDO.getId(), e.getMessage());
            }
        });
    }
}
