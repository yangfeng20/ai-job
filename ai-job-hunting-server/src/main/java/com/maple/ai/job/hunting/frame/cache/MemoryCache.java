package com.maple.ai.job.hunting.frame.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 杨锋
 * @date 2022/7/20 16:52
 * desc:
 */

@SuppressWarnings("unused")
public interface MemoryCache<K, V> {


    /**
     * 获取redis数据
     *
     * @param key key
     * @return 数据实体
     */
    V get(K key);

    /**
     * 批量获取redis数据
     *
     * @param keyList keys
     * @return 数据实体
     */
    Map<K, V> mGet(List<K> keyList);


    /**
     * 设置数据到redis
     *
     * @param key   key
     * @param value 数据实体
     * @return 成功？
     */
    boolean set(K key, V value);

    /**
     * 设置数据到redis
     *
     * @param kvMap 数据实体
     * @return 成功？
     */
    boolean mSet(Map<K, V> kvMap);

    /**
     * 设置数据到redis
     *
     * @param kvMap    数据实体
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @return 成功？
     */
    boolean mSet(Map<K, V> kvMap, int expire, TimeUnit timeUnit);


    /**
     * 删除redis数据
     *
     * @param key key
     * @return 成功？
     */
    boolean delete(K key);

    /**
     * 删除redis数据
     *
     * @param keys keys
     * @return 成功？
     */
    boolean mDelete(List<K> keys);

    /**
     * 指定key是否存在
     *
     * @param key key
     * @return boolean
     */
    boolean contains(K key);


    /**
     * toMap
     *
     * @return {@link Map }<{@link K }, {@link V }>
     */
    Map<K, V> toMap();

}
