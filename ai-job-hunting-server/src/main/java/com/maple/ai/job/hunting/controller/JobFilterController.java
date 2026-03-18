package com.maple.ai.job.hunting.controller;

import com.maple.ai.job.hunting.model.common.Response;
import com.maple.ai.job.hunting.model.param.AIFilterParam;
import com.maple.ai.job.hunting.model.vo.AiFilterResultVO;
import com.maple.ai.job.hunting.service.biz.JobFilterService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author maple
 * Created Date: 2024/4/23 13:59
 * Description:
 */

@RestController
@RequestMapping("api/job/filter")
public class JobFilterController {

    @Resource
    private JobFilterService jobFilterService;

    /**
     * 单个过滤
     */
    @PostMapping("/one")
    public Response<AiFilterResultVO> filter(@RequestBody AIFilterParam param) {
        return Response.success(jobFilterService.filter(param));
    }

    /**
     * 批量过滤
     */
    @PostMapping("/list")
    public void batchFilter() {

    }
}
