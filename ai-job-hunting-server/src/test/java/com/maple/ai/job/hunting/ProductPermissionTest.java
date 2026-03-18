package com.maple.ai.job.hunting;

import com.maple.ai.job.hunting.config.ProductPermissionConfig;
import com.maple.ai.job.hunting.emums.ProductTypeEnum;
import com.maple.ai.job.hunting.utils.PermissionConfigUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author maple
 * Created Date: 2024/12/19
 * Description: 产品权限配置测试类
 */
@SpringBootTest
public class ProductPermissionTest {

    private ProductPermissionConfig productPermissionConfig;

    @BeforeEach
    public void setUp() {
        productPermissionConfig = new ProductPermissionConfig();
        productPermissionConfig.initDefaultPermissions();
    }

    @Test
    public void testSimplePermissionConfig() {
        // 测试简单权限配置
        List<List<Integer>> permissionGroups = productPermissionConfig.getPermissionGroups("/api/job/seeker/cloned/ask");
        assertNotNull(permissionGroups);
        assertEquals(1, permissionGroups.size());
        assertEquals(1, permissionGroups.get(0).size());
        assertEquals(ProductTypeEnum.AI_SEAT.getCode(), permissionGroups.get(0).get(0));
    }

    @Test
    public void testAndLogicPermissionConfig() {
        // 测试与逻辑权限配置
        List<List<Integer>> permissionGroups = productPermissionConfig.getPermissionGroups("/api/user/ai/config/debug");
        assertNotNull(permissionGroups);
        assertEquals(1, permissionGroups.size());
        assertEquals(2, permissionGroups.get(0).size());
        assertTrue(permissionGroups.get(0).contains(ProductTypeEnum.CUSTOM_API.getCode()));
        assertTrue(permissionGroups.get(0).contains(ProductTypeEnum.MODEL_FINE_TUNING.getCode()));
    }

    @Test
    public void testOrLogicPermissionConfig() {
        // 测试或逻辑权限配置
        productPermissionConfig.addOrLogicPermissionConfig("/api/test/or/logic", 
            PermissionConfigUtil.createOrLogicConfig(
                PermissionConfigUtil.createPermissionGroup(ProductTypeEnum.AI_SEAT, ProductTypeEnum.AI_ASSISTANT),
                PermissionConfigUtil.createPermissionGroup(ProductTypeEnum.CUSTOM_API)
            )
        );

        List<List<Integer>> permissionGroups = productPermissionConfig.getPermissionGroups("/api/test/or/logic");
        assertNotNull(permissionGroups);
        assertEquals(2, permissionGroups.size());
        
        // 第一个权限组：AI坐席 + AI助理
        assertEquals(2, permissionGroups.get(0).size());
        assertTrue(permissionGroups.get(0).contains(ProductTypeEnum.AI_SEAT.getCode()));
        assertTrue(permissionGroups.get(0).contains(ProductTypeEnum.AI_ASSISTANT.getCode()));
        
        // 第二个权限组：自定义API
        assertEquals(1, permissionGroups.get(1).size());
        assertEquals(ProductTypeEnum.CUSTOM_API.getCode(), permissionGroups.get(1).get(0));
    }

    @Test
    public void testWildcardPermissionConfig() {
        // 测试通配符权限配置
        productPermissionConfig.addOrLogicPermissionConfig("/api/admin/**", 
            PermissionConfigUtil.createOrLogicConfig(
                PermissionConfigUtil.createPermissionGroup(ProductTypeEnum.AI_SEAT),
                PermissionConfigUtil.createPermissionGroup(ProductTypeEnum.AI_ASSISTANT)
            )
        );

        // 测试通配符匹配
        assertTrue(productPermissionConfig.needsPermissionCheck("/api/admin/users"));
        assertTrue(productPermissionConfig.needsPermissionCheck("/api/admin/settings"));
        assertFalse(productPermissionConfig.needsPermissionCheck("/api/other/endpoint"));

        List<List<Integer>> permissionGroups = productPermissionConfig.resolvePermissionGroups("/api/admin/users");
        assertNotNull(permissionGroups);
        assertEquals(2, permissionGroups.size());
    }

    @Test
    public void testPermissionConfigUtil() {
        // 测试权限配置工具类
        List<Integer> permissionGroup = PermissionConfigUtil.createPermissionGroup(
            ProductTypeEnum.AI_SEAT, ProductTypeEnum.AI_ASSISTANT, ProductTypeEnum.BATCH_PUSH);
        assertEquals(3, permissionGroup.size());
        assertTrue(permissionGroup.contains(ProductTypeEnum.AI_SEAT.getCode()));
        assertTrue(permissionGroup.contains(ProductTypeEnum.AI_ASSISTANT.getCode()));
        assertTrue(permissionGroup.contains(ProductTypeEnum.BATCH_PUSH.getCode()));

        List<List<Integer>> orLogicConfig = PermissionConfigUtil.createOrLogicConfig(
            PermissionConfigUtil.createPermissionGroup(ProductTypeEnum.AI_SEAT, ProductTypeEnum.AI_ASSISTANT),
            PermissionConfigUtil.createPermissionGroup(ProductTypeEnum.CUSTOM_API)
        );
        assertEquals(2, orLogicConfig.size());
    }

    @Test
    public void testComplexPermissionLogic() {
        // 测试复杂权限逻辑：(权限1 AND 权限2) OR (权限3 AND 权限4) OR 权限7
        productPermissionConfig.addOrLogicPermissionConfig("/api/complex/endpoint", List.of(
            List.of(ProductTypeEnum.AI_SEAT.getCode(), ProductTypeEnum.AI_ASSISTANT.getCode()),
            List.of(ProductTypeEnum.BATCH_PUSH.getCode(), ProductTypeEnum.SEND_CUSTOM_MSG.getCode()),
            List.of(ProductTypeEnum.CUSTOM_API.getCode())
        ));

        List<List<Integer>> permissionGroups = productPermissionConfig.getPermissionGroups("/api/complex/endpoint");
        assertNotNull(permissionGroups);
        assertEquals(3, permissionGroups.size());
        
        // 第一个权限组：AI坐席 + AI助理
        assertEquals(2, permissionGroups.get(0).size());
        
        // 第二个权限组：批量投递 + 自定义招呼语
        assertEquals(2, permissionGroups.get(1).size());
        
        // 第三个权限组：自定义API
        assertEquals(1, permissionGroups.get(2).size());
    }

    @Test
    public void testNoPermissionRequired() {
        // 测试不需要权限的URL
        assertFalse(productPermissionConfig.needsPermissionCheck("/api/public/endpoint"));
        assertNull(productPermissionConfig.resolvePermissionGroups("/api/public/endpoint"));
    }
}

