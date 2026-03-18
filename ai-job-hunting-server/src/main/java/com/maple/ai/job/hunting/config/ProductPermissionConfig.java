package com.maple.ai.job.hunting.config;

import com.maple.ai.job.hunting.emums.ProductTypeEnum;
import com.maple.ai.job.hunting.utils.PermissionConfigUtil;
import com.maple.smart.config.core.annotation.JsonValue;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author maple
 * Created Date: 2024/12/19
 * Description: 产品权限配置类
 * 支持权限组逻辑：[[权限1,权限2],[权限3,权限4]] 表示需要(权限1 AND 权限2) OR (权限3 AND 权限4)
 */
@Data
@Component
@ConfigurationProperties(prefix = "product.permission")
public class ProductPermissionConfig {

    /**
     * URL权限映射配置
     * key: URL路径
     * value: 权限组列表，每个组内的权限是AND关系，组之间是OR关系
     */
    @JsonValue("${product.url.map:{}}")
    private Map<String, List<List<Integer>>> urlPermissionMap;

    /**
     * 初始化默认权限配置
     */
    public void initDefaultPermissions() {
        if (urlPermissionMap == null) {
            urlPermissionMap = new java.util.HashMap<>();
        }

        addOrLogicPermissionConfig("/api/job/seeker/cloned/ask", PermissionConfigUtil.createOrLogicConfig(
                PermissionConfigUtil.createPermissionGroup(ProductTypeEnum.AI_SEAT),
                PermissionConfigUtil.createPermissionGroup(ProductTypeEnum.CUSTOM_API)
        ));

        urlPermissionMap.put("/api/job/ai/assistant/generate/greeting",
                List.of(List.of(ProductTypeEnum.AI_CUSTOM_MSG.getCode())));

        urlPermissionMap.put("/api/user/ai/config/save",
                List.of(List.of(ProductTypeEnum.CUSTOM_API.getCode())));

        urlPermissionMap.put("/api/job/filter/one",
                List.of(List.of(ProductTypeEnum.AI_FILTER.getCode())));

        urlPermissionMap.put("/api/user/ai/config/debug",
                List.of(List.of(ProductTypeEnum.CUSTOM_API.getCode(), ProductTypeEnum.MODEL_FINE_TUNING.getCode())));
    }

    /**
     * 添加权限配置
     *
     * @param url              URL路径
     * @param permissionGroups 权限组列表
     */
    public void addPermissionConfig(String url, List<List<Integer>> permissionGroups) {
        if (urlPermissionMap == null) {
            urlPermissionMap = new java.util.HashMap<>();
        }
        urlPermissionMap.put(url, permissionGroups);
    }

    /**
     * 添加简单权限配置（单个权限组）
     *
     * @param url         URL路径
     * @param permissions 权限列表
     */
    public void addSimplePermissionConfig(String url, List<Integer> permissions) {
        addPermissionConfig(url, List.of(permissions));
    }

    /**
     * 添加或逻辑权限配置
     *
     * @param url              URL路径
     * @param permissionGroups 权限组列表，每个组内的权限是AND关系，组之间是OR关系
     */
    public void addOrLogicPermissionConfig(String url, List<List<Integer>> permissionGroups) {
        addPermissionConfig(url, permissionGroups);
    }

    /**
     * 获取URL对应的权限组
     *
     * @param url URL路径
     * @return 权限组列表
     */
    public List<List<Integer>> getPermissionGroups(String url) {
        if (urlPermissionMap == null) {
            return null;
        }
        return urlPermissionMap.get(url);
    }

    /**
     * 检查URL是否需要权限验证
     *
     * @param url URL路径
     * @return 是否需要权限验证
     */
    public boolean needsPermissionCheck(String url) {
        if (urlPermissionMap == null) {
            return false;
        }

        // 精确匹配
        if (urlPermissionMap.containsKey(url)) {
            return true;
        }

        // 通配符前缀匹配
        for (String key : urlPermissionMap.keySet()) {
            if (key.endsWith("/**")) {
                String prefix = key.substring(0, key.length() - 3);
                if (url.startsWith(prefix)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 根据URL获取权限组（支持通配符匹配）
     *
     * @param url URL路径
     * @return 权限组列表
     */
    public List<List<Integer>> resolvePermissionGroups(String url) {
        if (urlPermissionMap == null) {
            return null;
        }

        // 精确匹配
        if (urlPermissionMap.containsKey(url)) {
            return urlPermissionMap.get(url);
        }

        // 通配符前缀匹配
        for (Map.Entry<String, List<List<Integer>>> entry : urlPermissionMap.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("/**")) {
                String prefix = key.substring(0, key.length() - 3);
                if (url.startsWith(prefix)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }
}

