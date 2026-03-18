package com.maple.ai.job.hunting.frame.cache;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollectionUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author maple
 * Created Date: 2023/12/26 11:21
 * Description:
 */

public class AbsMemoryCache<K extends Serializable, V> implements MemoryCache<K, V> {

    private final boolean resetExpireTime;

    private final TimedCache<K, V> memoryCacheMap;

    public AbsMemoryCache(int expire, TimeUnit timeUnit, boolean resetExpireTime) {
        this.resetExpireTime = resetExpireTime;
        this.memoryCacheMap = CacheUtil.newTimedCache(timeUnit.toMillis(expire));
        // 如果本身过期时间较短，那么就不应该太频繁的清理
        long clearIntervalTimeMs = timeUnit.toMillis(expire) * 2;

        if (clearIntervalTimeMs < 3600 * 1000) {
            // 小于一个小时按照一个小时清理一次
            this.memoryCacheMap.schedulePrune(TimeUnit.HOURS.toMillis(1));
        } else {
            this.memoryCacheMap.schedulePrune(clearIntervalTimeMs);
        }
    }

    @Override
    public V get(K key) {
        return memoryCacheMap.get(key, resetExpireTime);
    }

    @Override
    public Map<K, V> mGet(List<K> keyList) {
        if (CollectionUtil.isEmpty(keyList)) {
            return Collections.emptyMap();
        }
        return keyList.stream()
                .collect(Collectors.toMap(Function.identity(), key -> memoryCacheMap.get(key, resetExpireTime)));
    }

    @Override
    public boolean set(K key, V value) {
        memoryCacheMap.put(key, value);
        return true;
    }

    @Override
    public boolean mSet(Map<K, V> kvMap) {
        for (Map.Entry<K, V> kvEntry : kvMap.entrySet()) {
            K k = kvEntry.getKey();
            V v = kvEntry.getValue();
            if (v == null && k == null) {
                return false;
            }
            memoryCacheMap.put(k, v);
        }
        return true;
    }

    @Override
    public boolean mSet(Map<K, V> kvMap, int expire, TimeUnit timeUnit) {
        for (Map.Entry<K, V> kvEntry : kvMap.entrySet()) {
            K k = kvEntry.getKey();
            V v = kvEntry.getValue();
            if (v == null && k == null) {
                return false;
            }
            memoryCacheMap.put(k, v, timeUnit.toSeconds(expire));
        }
        return true;
    }

    @Override
    public boolean delete(K key) {
        memoryCacheMap.remove(key);
        return true;
    }

    @Override
    public boolean mDelete(List<K> keys) {
        for (K key : keys) {
            memoryCacheMap.remove(key);
        }
        return true;
    }

    @Override
    public boolean contains(K key) {
        return memoryCacheMap.containsKey(key);
    }

    @Override
    public Map<K, V> toMap() {
        HashMap<K, V> map = new HashMap<>();
        // 转换为map
        memoryCacheMap.cacheObjIterator().forEachRemaining(cacheObj -> {
            map.put(cacheObj.getKey(), cacheObj.getValue());
        });
        return map;
    }
}
