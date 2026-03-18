package com.maple.ai.job.hunting.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.maple.ai.job.hunting.common.Header;
import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.model.vo.UserInfoVO;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * @author 杨锋
 * @since 2022/6/24 17:05
 * desc:
 */

@Component
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    /**
     * insert表时进行的字段填充
     *
     * @param metaObject MetaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Header header = HeaderContext.getHeader();
        Long userId = Optional.ofNullable(header.getUserInfoVO()).map(UserInfoVO::getId).orElse(1L);
        strictInsertFill(metaObject, "createdDate", Date::new, Date.class);
        strictInsertFill(metaObject, "createdId", Long.class, userId);
        strictInsertFill(metaObject, "updatedId", Long.class, userId);
        strictInsertFill(metaObject, "updatedDate", Date::new, Date.class);
        strictInsertFill(metaObject, "isActive", () -> Boolean.TRUE, Boolean.class);
        strictInsertFill(metaObject, "downloadCount", () -> 0, Integer.class);
    }

    /**
     * update表时进行的字段填充
     *
     * @param metaObject MetaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Header header = HeaderContext.getHeader();
        Long userId = Optional.ofNullable(header.getUserInfoVO()).map(UserInfoVO::getId).orElse(1L);

        strictUpdateFill(metaObject, "updatedDate", Date::new, Date.class);
        strictInsertFill(metaObject, "updatedId", Long.class, userId);
    }

    /**
     * 注册MybatisPlus插件
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 防止全表更新和删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

}
