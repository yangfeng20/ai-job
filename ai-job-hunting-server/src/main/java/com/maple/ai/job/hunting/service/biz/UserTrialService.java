package com.maple.ai.job.hunting.service.biz;

import cn.hutool.core.bean.BeanUtil;
import com.maple.ai.job.hunting.emums.ProductTypeEnum;
import com.maple.ai.job.hunting.mapper.UserTrialMapper;
import com.maple.ai.job.hunting.model.bo.UserTrialDO;
import com.maple.ai.job.hunting.model.vo.UserTrialVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 用户试用服务
 *
 * @author gaoping
 * @since 2025/03/23
 */
@Service
@Slf4j
public class UserTrialService {

    @Resource
    private UserTrialMapper userTrialMapper;

    @Resource
    private UserInvitesService userInvitesService;


    public List<UserTrialVO> getUserAISeatTrialList(Long userId) {
        List<UserTrialDO> userTrialDoList = userTrialMapper.getUserTrial(userId, ProductTypeEnum.AI_SEAT.getCode());
        return userTrialDoList.stream().map(userTrialDO -> BeanUtil.copyProperties(userTrialDO, UserTrialVO.class))
                .peek(userTrialVO -> userTrialVO.setTrialCount(userInvitesService.buildAiSeatInitTrialCount(userTrialVO.getTrialCount())))
                .toList();
    }
}
