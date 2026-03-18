package com.maple.ai.job.hunting.controller;

import com.maple.ai.job.hunting.emums.BizCodeEnum;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.model.common.Response;
import com.maple.ai.job.hunting.model.vo.UserInfoVO;
import com.maple.ai.job.hunting.service.biz.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @author maple
 * Created Date: 2024/5/9 16:21
 * Description:
 */

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;


    /**
     * 导入简历
     *
     * @param file     简历文件
     * @param uniqueId boss平台的用户id
     * @param resumeId boss平台的简历id
     * @return {@link Response }<{@link UserInfoVO }>
     * @throws Exception 例外
     */
    @PostMapping("/import/resume")
    public Response<UserInfoVO> importResume(MultipartFile file, String uniqueId, String resumeId) throws Exception {
        InputStream resumeFileStream = file.getInputStream();
        if (uniqueId == null) {
            throw new ApplicationException(BizCodeEnum.PARAM_ERROR);
        }

        return Response.success(userService.importResume(resumeFileStream, uniqueId, resumeId));
    }


    /**
     * 静默登录
     *
     * @param uniqueId uniqueId boss平台id
     * @return {@link String}
     */
    @PostMapping("/silently/login")
    public Response<String> loginSilently(@RequestParam String uniqueId) {
        return Response.success(userService.loginSilently(uniqueId));
    }

    @PostMapping("/userinfo")
    public Response<UserInfoVO> getUserInfo() {
        return Response.success(userService.getUserInfo());
    }


    @PostMapping("/save/preference")
    public Response<?> savePreference(@RequestBody UserInfoVO userInfoVO) {
        userService.savePreference(userInfoVO);
        return Response.success();
    }
}
