package com.maple.ai.job.hunting.config;

import com.maple.smart.config.core.listener.ConfigListener;
import com.maple.smart.config.core.model.ConfigEntity;
import com.maple.smart.config.core.repository.ConfigRepository;
import com.maple.smart.config.core.subscription.ConfigSubscription;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * 智能配置监听器
 *
 * @author gaoping
 * @since 2025/02/27
 */


public class SmartConfigListener implements ConfigListener {

    @Override
    public void onChange(Collection<ConfigEntity> changeConfigEntityList) {

    }

    @Override
    public void setConfigSubscription(ConfigSubscription configSubscription) {

    }
}
