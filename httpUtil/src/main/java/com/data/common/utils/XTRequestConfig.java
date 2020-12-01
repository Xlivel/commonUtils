package com.data.common.utils;

import org.apache.http.client.config.RequestConfig;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class XTRequestConfig {
    private static XTRequestConfig instance = new XTRequestConfig();

    public static XTRequestConfig getInstance() {
        if (instance == null) instance = new XTRequestConfig();
        return instance;
    }

    private LruCache<Integer, RequestConfig> cache = new LruCache<Integer, RequestConfig>(10);
    private Lock lock = new ReentrantLock();

    public RequestConfig getRequestConfig(int timeout) {
        RequestConfig requestConfig = cache.get(timeout);
        if (requestConfig == null) {
            lock.lock();
            try {
                requestConfig = cache.get(timeout);
                if (requestConfig == null) {
                    requestConfig = RequestConfig.custom()
                            // 从连接池中获取到连接的最长时间
                            .setConnectionRequestTimeout(2000)
                            // 等待与服务器建立连接的时间
                            .setConnectTimeout(5000)
                            // 数据传输的最长时间
                            .setSocketTimeout(timeout).build();
                    cache.put(timeout, requestConfig);
                }
            } finally {
                lock.unlock();
            }
        }
        return requestConfig;
    }
}
