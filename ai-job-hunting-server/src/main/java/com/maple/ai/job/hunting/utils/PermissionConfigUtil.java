package com.maple.ai.job.hunting.utils;

import com.maple.ai.job.hunting.emums.ProductTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author maple
 * Created Date: 2024/12/19
 * Description: 权限配置工具类，提供便捷的权限配置方法
 */
@Component
public class PermissionConfigUtil {

    /**
     * 创建单个权限组
     * @param permissions 权限列表
     * @return 权限组
     */
    public static List<Integer> createPermissionGroup(Integer... permissions) {
        return List.of(permissions);
    }

    /**
     * 创建单个权限组（使用枚举）
     * @param productTypes 产品类型枚举
     * @return 权限组
     */
    public static List<Integer> createPermissionGroup(ProductTypeEnum... productTypes) {
        return Stream.of(productTypes)
                .map(ProductTypeEnum::getCode)
                .toList();
    }

    /**
     * 创建或逻辑权限配置
     * @param permissionGroups 权限组列表
     * @return 权限配置
     */
    @SafeVarargs
    public static List<List<Integer>> createOrLogicConfig(List<Integer>... permissionGroups) {
        return List.of(permissionGroups);
    }

    /**
     * 创建或逻辑权限配置（使用枚举）
     * @param permissionGroups 权限组列表
     * @return 权限配置
     */
    public static List<List<Integer>> createOrLogicConfig(List<List<Integer>> permissionGroups) {
        return permissionGroups;
    }

    /**
     * 创建简单权限配置（单个权限组）
     * @param permissions 权限列表
     * @return 权限配置
     */
    public static List<List<Integer>> createSimpleConfig(Integer... permissions) {
        return List.of(createPermissionGroup(permissions));
    }

    /**
     * 创建简单权限配置（单个权限组，使用枚举）
     * @param productTypes 产品类型枚举
     * @return 权限配置
     */
    public static List<List<Integer>> createSimpleConfig(ProductTypeEnum... productTypes) {
        return List.of(createPermissionGroup(productTypes));
    }

    /**
     * 示例：创建需要权限1,2,3 或者权限5,6 的配置
     * @return 权限配置
     */
    public static List<List<Integer>> createExampleOrLogicConfig() {
        return createOrLogicConfig(
            createPermissionGroup(ProductTypeEnum.AI_SEAT, ProductTypeEnum.AI_ASSISTANT, ProductTypeEnum.BATCH_PUSH),
            createPermissionGroup(ProductTypeEnum.IMAGE_RESUME, ProductTypeEnum.AI_CUSTOM_MSG)
        );
    }
}

