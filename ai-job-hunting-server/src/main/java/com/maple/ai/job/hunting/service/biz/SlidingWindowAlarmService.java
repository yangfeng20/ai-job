package com.maple.ai.job.hunting.service.biz;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author maple
 * Created Date: 2024/5/28 10:00
 * Description: 滑动窗口告警服务，用于处理在特定时间窗口内的事件频率告警。
 * 该服务是线程安全的。
 */
@Service
public class SlidingWindowAlarmService {

    /**
     * 存储每个告警键的时间戳记录
     */
    private final ConcurrentMap<String, List<Long>> records = new ConcurrentHashMap<>();

    /**
     * 为每个告警键提供专用的锁对象
     */
    private final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();

    /**
     * 记录当前事件并返回当前时间窗口内的事件总数。
     *
     * @param key           告警的唯一标识符
     * @param windowMinutes 时间窗口，单位为分钟
     * @return 当前时间窗口内的事件总数
     */
    public int recordAndGetCount(String key, int windowMinutes) {
        // 为每个key获取一个专用的锁，确保对该key下列表操作的线程安全
        Object lock = locks.computeIfAbsent(key, k -> new Object());

        synchronized (lock) {
            // 从记录中获取当前key的时间戳列表，如果不存在则创建一个新的
            List<Long> timestamps = records.getOrDefault(key, new ArrayList<>());

            // 记录当前事件的时间戳
            long now = System.currentTimeMillis();
            timestamps.add(now);

            // 计算时间窗口的截止时间
            long cutoff = now - TimeUnit.MINUTES.toMillis(windowMinutes);

            // 移除时间窗口之外的旧时间戳
            timestamps.removeIf(ts -> ts < cutoff);

            // 将更新后的列表放回记录中
            records.put(key, timestamps);

            // 返回窗口内事件的数量
            return timestamps.size();
        }
    }
}
