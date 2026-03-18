package com.maple.ai.job.hunting.controller;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.model.common.Response;
import com.maple.ai.job.hunting.model.vo.UserTrialVO;
import com.maple.ai.job.hunting.service.biz.UserTrialService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户试用控制器
 *
 * @author gaoping
 * @since 2025/03/23
 */
@RestController
@RequestMapping("/api/user/trial")
public class UserTrialController {

    @Resource
    private UserTrialService userTrialService;


    /**
     * 获取用户ai坐席试用列表
     *
     * @return {@link Response }<{@link List }<{@link UserTrialVO }>>
     */
    @GetMapping("/aiSeat/list")
    public Response<List<UserTrialVO>> getUserAISeatTrialList() {
        Long userId = HeaderContext.getHeader().getUserId();
        return Response.success(userTrialService.getUserAISeatTrialList(userId));
    }
}
