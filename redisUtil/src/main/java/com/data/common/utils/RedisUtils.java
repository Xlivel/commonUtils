package com.data.common.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.Map.Entry;

@Repository
public class RedisUtils extends BasicRedisTemplate {

    /**
     * set key-value
     *
     * @param key
     * @param value String
     * @throws
     * @since qlchat 1.0
     */
    public void set(String key, String value) {
        set(key, value, 0);
    }

    /**
     * set key-value
     *
     * @param key
     * @param value String
     * @throws
     * @since qlchat 1.0
     */
    public void set(String key, String value, int ttl) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key, value);
            if (ttl > 0) {
                jedis.expire(key.getBytes(), ttl);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * set key-value For Object(NOT String)
     *
     * @param key
     * @param value Object
     * @throws
     * @since qlchat 1.0
     */
    public void set(String key, Object value) {
        set(key, value, 0);
    }

    /**
     * set key-value For Object(NOT String)
     *
     * @param key
     * @param value Object
     * @param ttl   int
     * @throws
     * @since qlchat 1.0
     */
    public void set(String key, Object value, int ttl) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // jedis.set(key.getBytes(), HessianSerializer.serialize(value));
            jedis.set(key.getBytes(), toJsonByteArray(value));
            if (ttl > 0) {
                jedis.expire(key.getBytes(), ttl);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Double zincrby(String key, Double score, String member){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zincrby(key, score, member);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * set key-value with expired time(s)
     *
     * @param key
     * @param seconds
     * @param value
     * @return
     * @throws
     * @since qlchat 1.0
     */
    public String setex(String key, int seconds, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.setex(key, seconds, value);

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * set key-value For Object(NOT String) with expired time(s)
     *
     * @param key
     * @param seconds
     * @param value
     * @return
     * @throws
     * @since qlchat 1.0
     */
    public String setex(String key, int seconds, Object value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // return jedis.setex(key.getBytes(), seconds,
            // HessianSerializer.serialize(value));
            return jedis.setex(key.getBytes(), seconds, toJsonByteArray(value));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 如果key还不存在则进行设置，返回true，否则返回false.
     *
     * @param key
     * @param value
     * @return
     * @throws
     * @since qlchat 1.0
     */
    public boolean setnx(String key, String value, int ttl) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Long reply = jedis.setnx(key, value);
            if (reply == null) {
                reply = 0L;
            }
            if (ttl > 0 && reply.longValue() == 1) {
                jedis.expire(key, ttl);
            }
            return reply.longValue() == 1;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 如果key还不存在则进行设置，返回true，否则返回false.
     *
     * @param key
     * @param value
     * @return
     * @throws
     * @since qlchat 1.0
     */
    public boolean setnx(String key, String value) {
        return setnx(key, value, 0);
    }

    /**
     * 如果key还不存在则进行设置 For Object，返回true，否则返回false.
     *
     * @param key
     * @param value
     * @return
     * @throws
     * @since qlchat 1.0
     */
    public boolean setnx(String key, Object value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // return jedis.setnx(key.getBytes(),
            // HessianSerializer.serialize(value)) == 1 ? true : false;
            return jedis.setnx(key.getBytes(), toJsonByteArray(value)) == 1 ? true : false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 如果key还不存在则进行设置 For Object，返回true，否则返回false.
     *
     * @param key
     * @param value
     * @return
     * @throws
     * @since qlchat 1.0
     */
    public boolean setnx(String key, Object value, int ttl) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // Long reply = jedis.setnx(key.getBytes(),
            // HessianSerializer.serialize(value));
            Long reply = jedis.setnx(key.getBytes(), toJsonByteArray(value));
            if (ttl > 0) {
                jedis.expire(key.getBytes(), ttl);
            }
            return reply == 1;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 如果key不存在, 返回null.
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.get(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, 如果key不存在, 返回null.
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public <T> T get(String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // return (T)
            // HessianSerializer.deserialize(jedis.get(key.getBytes()));
            return fromJsonByteArray(jedis.get(key.getBytes()), clazz);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 自增 +1
     *
     * @param key
     * @return 返回自增后结果
     * @since qlchat 1.0
     */
    public Long incr(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.incr(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 自增 +1
     *
     * @param key     key
     * @param integer 起始值
     * @return 返回自增后结果
     * @since qlchat 1.0
     */
    public Long incrBy(final String key, long integer) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.incrBy(key, integer);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 自减 -1
     *
     * @param key
     * @return 返回自减后结果
     * @since qlchat 1.0
     */
    public Long decr(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.decr(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Prepend one or multiple values to a list
     *
     * @param key
     * @param values
     * @since qlchat 1.0
     */
    public void lpush(String key, String... values) {
        Jedis jedis = null;
        if (values == null) {
            return;
        }
        try {
            jedis = jedisPool.getResource();
            jedis.lpush(key, values);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object,Prepend one or multiple values to a list
     *
     * @param key
     * @param values
     * @since qlchat 1.0
     */
    public void lpush(String key, Object... values) {
        Jedis jedis = null;
        if (values == null) {
            return;
        }
        try {
            jedis = jedisPool.getResource();
            byte[][] strings = new byte[values.length][];
            for (int j = 0; j < values.length; j++) {
                // strings[j] = HessianSerializer.serialize(values[j]);
                strings[j] = toJsonByteArray(values[j]);
            }
            jedis.lpush(key.getBytes(), strings);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For single Object,Prepend one or multiple values to a list
     *
     * @param key
     * @param value
     * @since qlchat 1.0
     */
    public void lpushForObject(String key, Object value) {
        Jedis jedis = null;
        if (value == null) {
            return;
        }
        try {
            jedis = jedisPool.getResource();
            // jedis.lpush(key.getBytes(), HessianSerializer.serialize(value));
            jedis.lpush(key.getBytes(), toJsonByteArray(value));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For single Object,Prepend one or multiple values to a list
     *
     * @param key
     * @param value
     * @since qlchat 1.0
     */
    public void rpushForObject(String key, Object value) {
        Jedis jedis = null;
        if (value == null) {
            return;
        }
        try {
            jedis = jedisPool.getResource();
            // jedis.rpush(key.getBytes(), HessianSerializer.serialize(value));
            jedis.rpush(key.getBytes(), toJsonByteArray(value));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Append one or multiple values to a list
     *
     * @param key
     * @param values
     * @since qlchat 1.0
     */
    public void rpush(String key, String... values) {
        Jedis jedis = null;
        if (values == null) {
            return;
        }
        try {
            jedis = jedisPool.getResource();
            jedis.rpush(key, values);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Append one or multiple values to a list
     *
     * @param key
     * @param values
     * @since qlchat 1.0
     */
    public void rpush(String key, Object... values) {
        Jedis jedis = null;
        if (values == null) {
            return;
        }
        try {
            jedis = jedisPool.getResource();
            byte[][] strings = new byte[values.length][];
            for (int j = 0; j < values.length; j++) {
                // strings[j] = HessianSerializer.serialize(values[j]);
                strings[j] = toJsonByteArray(values[j]);
            }
            jedis.rpush(key.getBytes(), strings);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Remove and get the last element in a list
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public String rpop(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.rpop(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Remove and get the last element in a list
     *
     * @param key
     * @param clazz
     * @return
     * @since qlchat 1.0
     */
    public <T> T rpop(String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // return (T)
            // HessianSerializer.deserialize(jedis.rpop(key.getBytes()));
            return fromJsonByteArray(jedis.rpop(key.getBytes()), clazz);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Remove and get the first element in a list
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public String lpop(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.lpop(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Remove and get the first element in a list
     *
     * @param key
     * @param clazz
     * @return
     * @since qlchat 1.0
     */
    public <T> T lpop(String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // return (T)
            // HessianSerializer.deserialize(jedis.lpop(key.getBytes()));
            return fromJsonByteArray(jedis.lpop(key.getBytes()), clazz);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the length of a list
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public Long llen(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.llen(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 删除List中的等于value的元素
     * <p>
     * count = 1 :删除第一个； count = 0 :删除所有； count = -1:删除最后一个；
     *
     * @param key
     * @param count
     * @param value
     * @return
     * @since qlchat 1.0
     */
    public Long lrem(String key, long count, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.lrem(key, count, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * FOR Object, 删除List中的等于value的元素
     * <p>
     * count = 1 :删除第一个； count = 0 :删除所有； count = -1:删除最后一个；
     *
     * @param key
     * @param count
     * @param value
     * @return
     * @since qlchat 1.0
     */
    public Long lrem(String key, long count, Object value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // return jedis.lrem(key.getBytes(), count,
            // HessianSerializer.serialize(value));
            return jedis.lrem(key.getBytes(), count, toJsonByteArray(value));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get a range of elements from a list.
     * <P>
     * For example LRANGE foobar 0 2 will return the first three elements of the
     * list.
     * </p>
     * <P>
     * For example LRANGE foobar -1 -2 will return the last two elements of the
     * list.
     * </p>
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @since qlchat 1.0
     */
    public List<String> lrange(String key, long start, long end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.lrange(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Get a range of elements from a list.
     * <P>
     * For example LRANGE foobar 0 2 will return the first three elements of the
     * list.
     * </p>
     * <P>
     * For example LRANGE foobar -1 -2 will return the last two elements of the
     * list.
     * </p>
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @since qlchat 1.0
     */
    public <T> List<T> lrange(String key, long start, long end, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            List<byte[]> list = jedis.lrange(key.getBytes(), start, end);
            if (list != null && list.size() > 0) {
                List<T> results = new ArrayList<T>();
                for (byte[] bytes : list) {
                    results.add(fromJsonByteArray(bytes, clazz));
                }
                return results;
            }
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String ltrim(String key, long start, long end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.ltrim(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Add one or more members to a set
     *
     * @param key
     * @param members
     * @return
     * @since qlchat 1.0
     */
    public Boolean sadd(final String key, final String... members) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.sadd(key, members) >= 1 ? true : false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Add one or more members to a set
     *
     * @param key
     * @param ttl
     * @param members
     * @return
     * @since qlchat 1.0
     */
    public Boolean sadd(final String key, int ttl, final String... members) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Boolean ret = jedis.sadd(key, members) == 1 ? true : false;
            if (ret && ttl > 0) {
                jedis.expire(key, ttl);
            }
            return ret;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Add one or more members to a set
     *
     * @param key
     * @param members
     * @return
     * @since qlchat 1.0
     */
    public Boolean sadd(final String key, final Object... members) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[][] strings = new byte[members.length][];
            for (int j = 0; j < members.length; j++) {
                strings[j] = toJsonByteArray(members[j]);
            }
            return jedis.sadd(key.getBytes(), strings) == 1 ? true : false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Remove one or more members from a set
     *
     * @param key
     * @param members
     * @return
     * @since qlchat 1.0
     */
    public Boolean srem(final String key, final String... members) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.srem(key, members) >= 1 ? true : false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Remove one or more members from a set
     *
     * @param key
     * @param members
     * @return
     * @since qlchat 1.0
     */
    public Boolean srem(final String key, final Object... members) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[][] strings = new byte[members.length][];
            for (int j = 0; j < members.length; j++) {
                strings[j] = toJsonByteArray(members[j]);
            }
            return jedis.srem(key.getBytes(), strings) == 1 ? true : false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get all the members in a set.
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public Set<String> smembers(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.smembers(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Get all the members in a set.
     *
     * @param key
     * @param clazz
     * @return
     * @since qlchat 1.0
     */
    public <T> Set<T> smembers(final String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<byte[]> tempSet = jedis.smembers(key.getBytes());
            if (tempSet != null && tempSet.size() > 0) {
                TreeSet<T> result = new TreeSet<T>();
                for (byte[] value : tempSet) {
                    result.add(fromJsonByteArray(value, clazz));
                }
                return result;
            }
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get all the members is number in a set.
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public Long scard(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.scard(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * true if the meber exists in a set,else false
     *
     * @param key
     * @param member
     * @return
     * @since qlchat 1.0
     */
    public Boolean sismember(final String key, final String member) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Boolean sismember = jedis.sismember(key, member);
            return sismember;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String spop(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.spop(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<String> srandmember(final String key, final int count) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.srandmember(key, count);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Set the string value of a hash field
     *
     * @param key
     * @param field
     * @param value
     * @return
     * @since qlchat 1.0
     */
    public Boolean hset(final String key, final String field, final String value) {
        return hset(key, field, value, 0);
    }

    /**
     * Set the string value of a hash field
     *
     * @param key
     * @param field
     * @param value
     * @param ttl
     * @return
     * @since qlchat 1.0
     */
    public Boolean hset(final String key, final String field, final String value, final int ttl) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Long reply = jedis.hset(key, field, value);
            if (ttl > 0) {
                jedis.expire(key, ttl);
            }
            return reply == 1;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Set the Object value of a hash field
     *
     * @param key
     * @param field
     * @param value
     * @return
     * @since qlchat 1.0
     */
    public Boolean hset(final String key, final String field, final Object value) {
        return hset(key, field, value, 0);
    }

    /**
     * Set the Object value of a hash field
     *
     * @param key
     * @param field
     * @param value
     * @param ttl
     * @return
     * @since qlchat 1.0
     */
    public Boolean hset(final String key, final String field, final Object value, final int ttl) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Long reply = jedis.hset(key.getBytes(), field.getBytes(), toJsonByteArray(value));
            if (ttl > 0) {
                jedis.expire(key, ttl);
            }
            return reply == 1;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the value of a hash field
     *
     * @param key
     * @param field
     * @return
     * @since qlchat 1.0
     */
    public String hget(final String key, final String field) {
        return hget(key, field, 0);
    }

    /**
     * Get the value of a hash field
     *
     * @param key
     * @param field
     * @return
     * @since qlchat 1.0
     */
    public String hget(final String key, final String field, int ttl) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String res = jedis.hget(key, field);
            if (ttl > 0) {
                jedis.expire(key, ttl);
            }
            return res;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the value of a hash field
     *
     * @param key
     * @param field
     * @return
     * @since qlchat 1.0
     */
    public <T> T hget(final String key, final String field, final Class<T> clazz) {
        return hget(key, field, clazz, 0);
    }

    /**
     * Get the value of a hash field
     *
     * @param key
     * @param field
     * @return
     * @since qlchat 1.0
     */
    public <T> T hget(final String key, final String field, final Class<T> clazz, final int ttl) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            T res = fromJsonByteArray(jedis.hget(key.getBytes(), field.getBytes()), clazz);
            if (ttl > 0) {
                expire(key, ttl);
            }
            return res;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Delete one or more hash fields
     *
     * @param key
     * @param fields
     * @return
     * @since qlchat 1.0
     */
    public Boolean hdel(final String key, final String... fields) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hdel(key, fields) == 1 ? true : false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Check if a hash field exists
     *
     * @param key
     * @param field
     * @return
     * @since qlchat 1.0
     */
    public Boolean hexists(final String key, final String field) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hexists(key, field);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get all the fields and values in a hash 当Hash较大时候，慎用！
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public Map<String, String> hgetAll(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hgetAll(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get all the fields and values in a hash 当Hash较大时候，慎用！
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public Map<String, Object> hgetAllObject(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Map<byte[], byte[]> byteMap = jedis.hgetAll(key.getBytes());
            if (byteMap != null && byteMap.size() > 0) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (Entry<byte[], byte[]> e : byteMap.entrySet()) {
                    map.put(new String(e.getKey()), fromJsonByteArray(e.getValue(), Object.class));
                }
                return map;
            }
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get all the fields and values in a hash 当Hash较大时候，慎用！
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public <T> Map<String, T> hgetAllObject(final String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Map<byte[], byte[]> byteMap = jedis.hgetAll(key.getBytes());
            if (byteMap != null && byteMap.size() > 0) {
                Map<String, T> map = new HashMap<String, T>();
                for (Entry<byte[], byte[]> e : byteMap.entrySet()) {
                    map.put(new String(e.getKey()), fromJsonByteArray(e.getValue(), clazz));
                }
                return map;
            }
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the values of all the given hash fields.
     *
     * @param key
     * @param fields
     * @return
     * @since qlchat 1.0
     */
    public List<String> hmget(final String key, final String... fields) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hmget(key, fields);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the value of a mulit fields
     *
     * @param key
     * @param ttl
     * @param fields
     * @return
     * @since Wifi 1.0
     */
    public Map<String, Object> hmgetObject(final String key, final int ttl, final String... fields) {
        Jedis jedis = null;
        try {
            if (null == fields) {
                return null;
            }
            jedis = jedisPool.getResource();
            List<byte[]> byteList = new ArrayList<byte[]>();
            for (String field : fields) {
                byteList.add(field.getBytes());
            }
            List<byte[]> resBytes = jedis.hmget(key.getBytes(), byteList.toArray(new byte[byteList.size()][]));
            Map<String, Object> resMap = null;
            if (null != resBytes) {
                resMap = new HashMap<String, Object>();
                for (int i = 0; i < resBytes.size(); i++) {
                    resMap.put(fields[i], fromJsonByteArray(resBytes.get(i), Object.class));
                }
            }
            if (ttl > 0) {
                expire(key, ttl);
            }
            return resMap;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the value of a mulit fields
     *
     * @param key
     * @param
     * @param fields
     * @return
     * @since Wifi 1.0
     */
    public Map<String, Object> hmgetObject(final String key, final String... fields) {
        return hmgetObject(key, 0, fields);
    }

    /**
     * Set multiple hash fields to multiple values.
     *
     * @param key
     * @param hash
     * @return
     * @since qlchat 1.0
     */
    public String hmset(final String key, final Map<String, String> hash) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hmset(key, hash);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Set multiple hash fields to multiple values.
     *
     * @param key
     * @param hash
     * @return
     * @since qlchat 1.0
     */
    public String hmsetObject(final String key, final Map<String, Object> hash) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Map<byte[], byte[]> byteMap = new HashMap<byte[], byte[]>(hash.size());
            for (Entry<String, Object> e : hash.entrySet()) {
                byteMap.put(e.getKey().getBytes(), toJsonByteArray(e.getValue()));
            }
            return jedis.hmset(key.getBytes(), byteMap);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Increment the integer value of a hash field by the given number.
     *
     * @param key
     * @param field
     * @param value
     * @return
     * @since qlchat 1.0
     */
    public Long hincrBy(final String key, final String field, final long value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hincrBy(key, field, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get all the fields in a hash.
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public Set<String> hkeys(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hkeys(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get all the fields in a hash.
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public <T> Set<T> hkeys(final String key, final Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<byte[]> set = jedis.hkeys(key.getBytes());
            Set<T> objectSet = new HashSet<T>();
            if (set != null && set.size() != 0) {
                for (byte[] b : set) {
                    objectSet.add(fromJsonByteArray(b, clazz));
                }
            }
            return objectSet;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the number of fields in a hash.
     *
     * @param key
     * @return
     * @since qlchat 1.0
     */
    public Long hlen(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hlen(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Long hsetnx(final String key, final String field, final String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hsetnx(key, field, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    /**
     * 加入Sorted set, 如果member在Set里已存在, 只更新score并返回false, 否则返回true.
     *
     * @param key
     * @param member
     * @param score
     * @return
     * @since qlchat 1.0
     */
    public Boolean zadd(String key, double score, String member) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zadd(key, score, member) == 1 ? true : false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 加入Sorted set, 如果member在Set里已存在, 只更新score并返回false, 否则返回true.
     *
     * @param key
     * @param member
     * @param score
     * @param ttl    过期时间，秒
     * @return
     * @since qlchat 1.0
     */
    public Boolean zadd(final String key, final double score, final int ttl, final String member) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Boolean ret = jedis.zadd(key, score, member) == 1 ? true : false;
            if (ret && ttl > 0) {
                jedis.expire(key, ttl);
            }
            return ret;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, 加入Sorted set, 如果member在Set里已存在, 只更新score并返回false, 否则返回true.
     *
     * @param key
     * @param member
     * @param score
     * @return
     * @since qlchat 1.0
     */
    public Boolean zadd(String key, double score, Object member) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // return jedis.zadd(key.getBytes(), score,
            // HessianSerializer.serialize(member)) == 1 ? true : false;
            return jedis.zadd(key.getBytes(), score, toJsonByteArray(member)) == 1 ? true : false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Return a range of members in a sorted set, by index. Ordered from the
     * lowest to the highest score.
     *
     * @param key
     * @param start
     * @param end
     * @return Ordered from the lowest to the highest score.
     * @since qlchat 1.0
     */
    public Set<String> zrange(String key, long start, long end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrange(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Return a range of members in a sorted set, by index.Ordered
     * from the lowest to the highest score.
     *
     * @param key
     * @param start
     * @param end
     * @return Ordered from the lowest to the highest score.
     * @since qlchat 1.0
     */
    public <T> List<T> zrange(String key, long start, long end, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<byte[]> tempSet = jedis.zrange(key.getBytes(), start, end);
            if (tempSet != null && tempSet.size() > 0) {
                for (byte[] value : tempSet) {
                    // result.add((T) HessianSerializer.deserialize(value));
                    result.add(fromJsonByteArray(value, clazz));
                }
                return result;
            }
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Return a range of members in a sorted set, by index. Ordered from the
     * highest to the lowest score.
     *
     * @param key
     * @param start
     * @param end
     * @return Ordered from the highest to the lowest score.
     * @since qlchat 1.0
     */
    public Set<String> zrevrange(String key, long start, long end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrevrange(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Return a range of members in a sorted set, by index. Ordered
     * from the highest to the lowest score.
     *
     * @param key
     * @param start
     * @param end
     * @param clazz
     * @return Ordered from the highest to the lowest score.
     * @since qlchat 1.0
     */
    public <T> List<T> zrevrange(String key, long start, long end, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<byte[]> tempSet = jedis.zrevrange(key.getBytes(), start, end);
            if (tempSet != null && tempSet.size() > 0) {
                List<T> result = new ArrayList<T>();
                for (byte[] value : tempSet) {
                    // result.add((T) HessianSerializer.deserialize(value));
                    result.add(fromJsonByteArray(value, clazz));
                }
                return result;
            }
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     *
     * @param key
     * @param min
     * @param max
     * @return Ordered from the lowest to the highest score.
     * @since qlchat 1.0
     */
    public Set<String> zrangeByScore(final String key, final double min, final double max) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrangeByScore(key, min, max);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Return the all the elements in the sorted set at key with a
     * score between min and max (including elements with score equal to min or
     * max).
     *
     * @param key
     * @param min
     * @param max
     * @return Ordered from the lowest to the highest score.
     * @since qlchat 1.0
     */
    public <T> Set<T> zrangeHashSetByScore(final String key, final double min, final double max, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<byte[]> tempSet = jedis.zrangeByScore(key.getBytes(), min, max);
            if (tempSet != null && tempSet.size() > 0) {
                HashSet<T> result = new HashSet<T>();
                for (byte[] value : tempSet) {
                    // result.add((T) HessianSerializer.deserialize(value));
                    result.add(fromJsonByteArray(value, clazz));
                }
                return result;
            }
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     *
     * @param key
     * @param min
     * @param max
     * @return Ordered from the highest to the lowest score.
     * @since qlchat 1.0
     */
    public Set<String> zrevrangeByScore(final String key, final double min, final double max) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrevrangeByScore(key, max, min);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Return the all the elements in the sorted set at key with a
     * score between min and max (including elements with score equal to min or
     * max).
     *
     * @param key
     * @param min
     * @param max
     * @return Ordered from the lowest to the highest score.
     * @since qlchat 1.0
     */
    public <T> List<T> zrangeByScore(final String key, final double min, final double max, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<byte[]> tempSet = jedis.zrangeByScore(key.getBytes(), min, max);
            if (tempSet != null && tempSet.size() > 0) {
                List<T> result = new ArrayList<T>();
                for (byte[] value : tempSet) {
                    result.add(fromJsonByteArray(value, clazz));
                }
                return result;
            }
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public <T> List<T> zrangeByScore(final String key, final double min, final double max, final int offset,
                                     final int count, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<byte[]> tempSet = jedis.zrangeByScore(key.getBytes(), min, max, offset, count);
            if (tempSet != null && tempSet.size() > 0) {
                List<T> result = new ArrayList<T>();
                for (byte[] value : tempSet) {
                    result.add(fromJsonByteArray(value, clazz));
                }
                return result;
            }
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Return a range of members with scores in a sorted set, by index. Ordered
     * from the lowest to the highest score.
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @since qlchat 1.0
     */
    public Set<Tuple> zrangeWithScores(final String key, final long start, final long end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrangeWithScores(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Return a range of members with scores in a sorted set, by index. Ordered
     * from the highest to the lowest score.
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @since qlchat 1.0
     */
    public Set<Tuple> zrevrangeWithScores(final String key, final long start, final long end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrevrangeWithScores(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max). Ordered
     * from the lowest to the highest score.
     *
     * @param key
     * @param min
     * @param max
     * @return Ordered from the lowest to the highest score.
     * @since qlchat 1.0
     */
    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrangeByScoreWithScores(key, min, max);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max, final int offset,
                                              final int count) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max). Ordered
     * from the highest to the lowest score.
     *
     * @param key
     * @param min
     * @param max
     * @return Ordered from the highest to the lowest score.
     * @since qlchat 1.0
     */
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double min, final double max) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrevrangeByScoreWithScores(key, max, min);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double min, final double max, final int offset,
                                                 final int count) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Remove one or more members from a sorted set
     *
     * @param key
     * @param members
     * @return
     * @since qlchat 1.0
     */
    public Boolean zrem(final String key, final String... members) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrem(key, members) == 1 ? true : false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For Object, Remove one or more members from a sorted set
     *
     * @param key
     * @param members
     * @return
     * @since qlchat 1.0
     */
    public Boolean zrem(final String key, final Object... members) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[][] strings = new byte[members.length][];
            for (int j = 0; j < members.length; j++) {
                // strings[j] = HessianSerializer.serialize(members[j]);
                strings[j] = toJsonByteArray(members[j]);
            }
            return jedis.zrem(key.getBytes(), strings) == 1 ? true : false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the score associated with the given member in a sorted set
     *
     * @param key
     * @param member
     * @return
     * @since qlchat 1.0
     */
    public Double zscore(final String key, final String member) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zscore(key, member);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * For ObjecGet the score associated with the given member in a sorted set
     *
     * @param key
     * @param member
     * @return
     * @since qlchat 1.0
     */
    public Double zscore(final String key, final Object member) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // return jedis.zscore(key.getBytes(),
            // HessianSerializer.serialize(member));
            return jedis.zscore(key.getBytes(), toJsonByteArray(member));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Remove all elements in the sorted set at key with rank between start and
     * end. Start and end are 0-based with rank 0 being the element with the
     * lowest score. Both start and end can be negative numbers, where they
     * indicate offsets starting at the element with the highest rank. For
     * example: -1 is the element with the highest score, -2 the element with
     * the second highest score and so forth.
     * <p>
     * <b>Time complexity:</b> O(log(N))+O(M) with N being the number of
     * elements in the sorted set and M the number of elements removed by the
     * operation
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @since qlchat 1.0
     */
    public Long zremrangeByRank(String key, long start, long end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zremrangeByRank(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the length of a sorted set
     *
     * @param key
     * @param min
     * @param max
     * @return
     * @since qlchat 1.0
     */
    public Long zcount(final String key, final double min, final double max) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zcount(key, min, max);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Get the number of members in a sorted set
     *
     * @param key key
     * @return Long
     */
    public Long zcard(final String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zcard(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public <T> List<T> zrevrangeByScore(String key, double max, double min, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<String> sets = jedis.zrevrangeByScore(key, max, min);
            if (null == sets || sets.size() == 0) {
                return result;
            }
            for (String s : sets) {
                result.add(JSON.parseObject(s, clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (jedis != null) {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return result;
    }

    public <T> List<T> zrevrangeByScore(String key, double max, double min, int offset, int count, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<String> sets = jedis.zrevrangeByScore(key, max, min, offset, count);
            if (null == sets || sets.size() == 0) {
                return result;
            }
            for (String s : sets) {
                result.add(JSON.parseObject(s, clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (jedis != null) {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return result;
    }

    public List<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        List<String> result = new ArrayList<String>();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<String> sets = jedis.zrevrangeByScore(key, max, min, offset, count);
            if (null == sets || sets.size() == 0) {
                return result;
            }
            for (String s : sets) {
                result.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (jedis != null) {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return result;
    }

    public Set<String> zrangeByScore(String key, double max, double min, int offset, int count) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrangeByScore(key, min, max, offset, count);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Long zremrangeByScore(String key, double start, double end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zremrangeByScore(key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @SuppressWarnings("null")
    public Map<String, Object> zrevrankWithScore(String key, String element) {
        Jedis jedis = null;
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Long rankIndex = jedis.zrevrank(key, element);
            Double score = jedis.zscore(key, element);
            if (rankIndex != null && score > 0) {
                map.put("rank", rankIndex + 1);
                map.put("score", score);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return map;
    }

    /**
     * 添加
     *
     * @param key      key
     * @param elements 元素
     */
    public Long pfadd(String key, String... elements) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.pfadd(key, elements);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 统计
     *
     * @param key key
     * @return long
     */
    public long pfcount(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.pfcount(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

}