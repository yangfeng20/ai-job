package com.maple.ai.job.hunting.frame.filter;

import cn.hutool.core.lang.Pair;
import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.config.AppBizConfig;
import com.maple.ai.job.hunting.config.ProductPermissionConfig;
import com.maple.ai.job.hunting.emums.BizCodeEnum;
import com.maple.ai.job.hunting.emums.ProductTypeEnum;
import com.maple.ai.job.hunting.frame.cache.ProductNotAuthorizedCache;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.mapper.UserProductMapper;
import com.maple.ai.job.hunting.mapper.UserTrialMapper;
import com.maple.ai.job.hunting.model.bo.UserTrialDO;
import com.maple.ai.job.hunting.service.biz.UserAIConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author maple
 * Created Date: 2024/5/29 22:09
 * Description: 产品权限过滤器，支持权限组逻辑
 * 权限组格式：[[1,2,3],[5,6]] 表示需要权限1,2,3 或者权限5,6
 */
@Order(4)
@Slf4j
@Component
public class ProductFilter implements Filter {

    @Resource
    private UserProductMapper userProductMapper;

    @Resource
    private UserTrialMapper userTrialMapper;

    @Resource
    private AppBizConfig appBizConfig;

    @Resource
    private ProductPermissionConfig productPermissionConfig;

    @Resource
    private ProductNotAuthorizedCache productNotAuthorizedCache;

    @Resource
    private UserAIConfigService userAIConfigService;

    @PostConstruct
    public void init() {
        log.info("初始化产品uri校验开始");
        // 初始化默认权限配置
        productPermissionConfig.initDefaultPermissions();
        log.info("产品权限配置初始化完成");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();
        List<List<Integer>> permissionGroups = productPermissionConfig.resolvePermissionGroups(uri);
        if (permissionGroups == null || permissionGroups.isEmpty()) {
            // 当前url未在产品权限map中，无需校验
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = HeaderContext.getHeader().getUserId();
        Set<Integer> productPowerList = userProductMapper.queryUserValidAllProductType(userId);

        // 检查是否满足任一权限组
        boolean hasValidPermission = false;
        List<String> missingPermissions = new ArrayList<>();

        for (List<Integer> permissionGroup : permissionGroups) {
            boolean groupSatisfied = true;
            List<String> groupMissingPermissions = new ArrayList<>();

            for (Integer permission : permissionGroup) {
                if (!productPowerList.contains(permission)) {
                    // 特殊权限检查
                    if (isSpecialPermissionSatisfied(permission, productPowerList)) {
                        continue;
                    }

                    if (!canUseTrial(userId, permission)) {
                        groupSatisfied = false;
                        groupMissingPermissions.add(ProductTypeEnum.getByCode(permission).getDesc());
                    }
                }
            }

            if (groupSatisfied) {
                hasValidPermission = true;
                break;
            } else {
                missingPermissions.addAll(groupMissingPermissions);
            }
        }

        if (!hasValidPermission) {
            log.info("userId:{} 访问url:{} 未获得授权且试用次数已用完", userId, uri);
            String missingPermissionDesc = String.join("、", missingPermissions);
            String userIdAndProductTypeKey = userId + "-" + missingPermissionDesc;

            if (productNotAuthorizedCache.contains(userIdAndProductTypeKey)) {
                productNotAuthorizedCache.set(userIdAndProductTypeKey, true);
                throw new ApplicationException("未购买产品【" + missingPermissionDesc + "】且试用次数已用完");
            } else {
                productNotAuthorizedCache.set(userIdAndProductTypeKey, true);
                log.info("userId:{} product:{} 未授权弹出订单", userId, missingPermissionDesc);
                throw new ApplicationException("未购买产品【" + missingPermissionDesc + "】且试用次数已用完",
                        BizCodeEnum.PRODUCT_NOT_AUTHORIZED.getCode());
            }
        }

        // 校验通过
        filterChain.doFilter(request, response);
    }

    /**
     * 检查特殊权限是否满足
     */
    private boolean isSpecialPermissionSatisfied(Integer permission, Set<Integer> productPowerList) {
        // 如果是需要AI坐席且购买了自定义API，则跳过校验
        if (permission.equals(ProductTypeEnum.AI_SEAT.getCode())
                && userAIConfigService.openCustomApi(productPowerList)) {
            return true;
        }
        // 模型微调能力需要购买API扩展包或普通版/Plus自有API
        if (permission.equals(ProductTypeEnum.MODEL_FINE_TUNING.getCode())
                && userAIConfigService.openCustomApi(productPowerList)) {
            return true;
        }
        return false;
    }

    private boolean canUseTrial(Long userId, Integer productType) {
        Pair<Boolean, UserTrialDO> canTrialPair = userTrialMapper.canTrialGet(userId, productType, appBizConfig);
        if (canTrialPair.getKey()) {
            userTrialMapper.incrTrialCount(canTrialPair.getValue());
            return true;
        }
        return false;
    }
}
