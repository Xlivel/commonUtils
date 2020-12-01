package com.data.common.utils;


import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class LruCache<K, V> {

    private final ConcurrentLinkedHashMap<K, V> cache;

    public LruCache(final int maxEntries) {
        cache = new ConcurrentLinkedHashMap.Builder<K, V>()
                .maximumWeightedCapacity(maxEntries + 1)
                .concurrencyLevel(32)
                .build();
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public V get(K key) {
        return cache.get(key);
    }

    public V remove(K key) {
        return cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}
