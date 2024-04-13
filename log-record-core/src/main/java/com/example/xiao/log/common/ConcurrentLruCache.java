package com.example.xiao.log.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * com.example.xiao.log.common
 *
 * @author xiaozhiwei
 * 2023/3/11
 * 16:49
 */
public class ConcurrentLruCache<K, V> {

    private final ConcurrentHashMap<K, V> cacheMap;
    private final Queue<K> queue;
    private final int maxCacheSize;

    public ConcurrentLruCache(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        cacheMap = new ConcurrentHashMap<>(maxCacheSize);
        queue = new LinkedList<>();
    }

    public synchronized void put(K key, V value) {
        // 如果缓存已满，则从HashMap和队列中删除最早添加的元素
        while (queue.size() >= maxCacheSize) {
            K expiredKey = queue.poll();
            cacheMap.remove(expiredKey);
        }
        // 添加新元素到HashMap和队列中
        cacheMap.put(key, value);
        queue.offer(key);
    }

    public synchronized V get(K key) {
        // 如果在HashMap中找到键，则更新队列中元素的访问时间
        if (cacheMap.containsKey(key)) {
            queue.remove(key);
            queue.offer(key);
            return cacheMap.get(key);
        }
        // 否则返回null
        return null;
    }

}
