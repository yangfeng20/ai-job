package com.maple.ai.job.hunting.controller;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.model.common.Response;
import com.maple.ai.job.hunting.model.param.JobSeekerClonedParam;
import com.maple.ai.job.hunting.model.vo.JobSeekerClonedResultVO;
import com.maple.ai.job.hunting.service.biz.JobSeekerClonedService;
import com.maple.ai.job.hunting.service.biz.MsgSessionService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 求职者分身
 *
 * @author maple
 * Created Date: 2024/4/23 14:01
 * Description:
 */

@Validated
@RestController
@RequestMapping("/api/job/seeker/cloned")
public class JobSeekerClonedController {

    @Resource
    private JobSeekerClonedService jobSeekerClonedService;

    @Resource
    private MsgSessionService msgSessionService;


    @PostMapping("/ask")
    public Response<JobSeekerClonedResultVO> ask(@Valid @RequestBody JobSeekerClonedParam jobSeekerClonedParam) {
        return Response.success(jobSeekerClonedService.ask(jobSeekerClonedParam));
    }

    @PostMapping("/change/session/status")
    public Response<Boolean> changeSessionStatus(@RequestParam("jobKey") String jobKey, @RequestParam("stop") Boolean stop) {
        return Response.success(msgSessionService.setMsgSessionStop(jobKey, stop));
    }

    @PostMapping("/change/session/user/stop")
    public Response<Boolean> stopUserAllSession() {
        Long userId = HeaderContext.getHeader().getUserId();
        return Response.success(msgSessionService.stopUserAllSession(userId));
    }

    @PostMapping("/change/session/admin/status")
    public Response<Boolean> adminChangeSessionStatus(@RequestParam("jobKey") String jobKey, @RequestParam("stop") Boolean stop) {
        return Response.success(msgSessionService.setMsgSessionStop(jobKey, stop));
    }
}
