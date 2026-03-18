package com.maple.ai.job.hunting.controller;

import com.alipay.api.AlipayApiException;
import com.maple.ai.job.hunting.model.OrderGenerateResult;
import com.maple.ai.job.hunting.model.common.Response;
import com.maple.ai.job.hunting.model.param.GenerateOrderGroupParam;
import com.maple.ai.job.hunting.model.vo.GenerateOrderVO;
import com.maple.ai.job.hunting.service.biz.ALiPayService;
import com.maple.ai.job.hunting.service.biz.SseService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author ThirdGoddess
 * @version 1.0.0
 * @since 2022/12/22 14:37
 * desc AliPay当面付
 */
@RestController
@RequestMapping("/api/pay")
public class ALiPayController {

    @Resource
    private ALiPayService aLiPayService;

    @Resource
    private SseService sseService;


    /**
     * 获取二维码
     * 获取的是用户要扫码支付的二维码
     * 创建订单，带入自己的业务逻辑
     */
    @RequestMapping(value = "/getQr", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getQr() {
        OrderGenerateResult generateResult = aLiPayService.generateOrder(100, "测试订单");
        return generateResult.getQrCodeBytes();
    }

    @RequestMapping("generate/order/group")
    public Response<List<GenerateOrderVO>> generateOrderGroup(@RequestBody GenerateOrderGroupParam generateOrderGroupParam) {
        return Response.success(aLiPayService.generateOrderGroup(generateOrderGroupParam));
    }

    /**
     * 支付完成后支付宝会请求这个回调
     */
    @PostMapping("/paySuccessCallBack")
    public String paySuccessCallBack(HttpServletRequest request) {
        Map<String, String> paramsMap = getParamsMap(request);
        String resp = aLiPayService.paySuccessCallBack(paramsMap);
        String[] split = resp.split(":");
        if (split.length < 2) {
            return resp;
        }

        Long userId = Long.valueOf(split[0]);
        String result = split[1];
        if ("success".equals(result)) {
            sseService.notifyClient(userId, "支付成功");
            return result;
        }
        sseService.notifyClient(userId, "支付失败");
        return result;
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
    @GetMapping("/searchOrder")
    public Response<String> searchOrder(String outTradeNo, String tradeNo) throws AlipayApiException {
        if (StringUtils.isBlank(outTradeNo) && StringUtils.isBlank(tradeNo)) {
            return Response.success("无订单号", "无订单号");
        }
        return Response.success(aLiPayService.searchOrderByAli(outTradeNo, tradeNo).getString("trade_status"));
    }

    @GetMapping("/cancelOrder")
    public Response<String> cancelOrder(String outTradeNo) {
        aLiPayService.cancelOrder(outTradeNo);
        return Response.success("success");
    }


    @NotNull
    public static Map<String, String> getParamsMap(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return params;
    }

}