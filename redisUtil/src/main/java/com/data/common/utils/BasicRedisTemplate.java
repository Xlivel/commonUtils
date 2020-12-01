package com.data.common.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class BasicRedisTemplate {

    @Autowired
	protected JedisPool jedisPool;

    protected void closeRedis(Jedis jedis) {
        if (jedis != null) {
            try {
            	jedis.close();
            } catch (Exception e) {
                if (jedis != null) {
                    try {
                        jedis.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }
    }

    /**
     * 删除key, 如果key存在返回true, 否则返回false。
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public boolean del(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.del(key) == 1 ? true : false;
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * true if the key exists, otherwise false
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public Boolean exists(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(key);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * set key expired time
     *
     * @param key
     * @param seconds
     * @return
     * @since qlchat 1.0
     */
    public Boolean expire(String key, int seconds) {
        Jedis jedis = null;
        if (seconds == 0) {
            return true;
        }
        try {
            jedis = jedisPool.getResource();
            return jedis.expire(key, seconds) == 1 ? true : false;
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * 
     * 把object转换为json byte array
     *
     * @param o
     * @return
     */
    protected byte[] toJsonByteArray(Object o) {
        String json = JSON.toJSONString(o) != null ? JSON.toJSONString(o) : "";
        return json.getBytes();
    }

    /**
     * 
     * 把json byte array转换为T类型object
     *
     * @param b
     * @param clazz
     * @return
     */
    protected <T> T fromJsonByteArray(byte[] b, Class<T> clazz) {
        if (b == null || b.length == 0) {
            return null;
        }
        return JSON.parseObject(new String(b), clazz);
    }

    public Long ttl(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.ttl(key);
        } finally {
            closeRedis(jedis);
        }
    }
}