package com.maple.ai.job.hunting.controller;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.model.common.Response;
import com.maple.ai.job.hunting.model.vo.UserInvitesVO;
import com.maple.ai.job.hunting.service.biz.ProductService;
import com.maple.ai.job.hunting.service.biz.UserInvitesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author maple
 * Created Date: 2024/6/5 17:14
 * Description:
 */

@RestController
@RequestMapping("/api/user/invites")
public class UserInvitesController {

    @Resource
    private UserInvitesService userInvitesService;

    /**
     * 用户绑定邀请码
     *
     * @param inviteCode 邀请码
     * @param name       邀请人名称
     * @return void
     */
    @PostMapping("/bind/code")
    public Response<Void> userBindInviteCode(@RequestParam String inviteCode, @RequestParam(required = false) String name) {
        Long userId = HeaderContext.getHeader().getUserId();
        userInvitesService.bindInviteCode(inviteCode, name, userId);
        return Response.success();
    }

    /**
     * 用户邀请列表
     *
     * @return java.util.List<com.maple.ai.job.hunting.model.vo.UserInvitesVO>
     */
    @PostMapping("/list")
    public Response<List<UserInvitesVO>> userInviteList() {
        Long userId = HeaderContext.getHeader().getUserId();
        return Response.success(userInvitesService.userInviteList(userId));
    }

    /**
     * 用户兑换产品
     *
     * @param product 产品id
     * @return void
     */
    @PostMapping("/exchange/products")
    public Response<Void> exchangeProduct(@RequestParam Integer product) {
        Long userId = HeaderContext.getHeader().getUserId();
        userInvitesService.exchangeProduct(product, userId);
        return Response.success();
    }
}
