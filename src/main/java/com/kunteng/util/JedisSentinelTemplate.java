package com.kunteng.util;

import org.springframework.data.redis.core.TimeoutUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis实现
 *
 * @author hlqian
 */
public class JedisSentinelTemplate {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JedisSentinelTemplate.class);
    private Pool<Jedis> jedisPool;

    public Pool<Jedis> getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }

    public <T> T execute(JedisAction<T> jedisAction) throws JedisException {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            return jedisAction.action(jedis);
        } catch (JedisConnectionException e) {
            logger.error("connection error", e);
            broken = true;
            throw e;
        } finally {
            closeResource(jedis, broken);
        }
    }

    public void execute(JedisActionNoResult jedisAction) throws JedisException {
        Jedis jedis = null;
        boolean broken = false;
        try {
            jedis = jedisPool.getResource();
            jedisAction.action(jedis);
        } catch (JedisConnectionException e) {
            logger.error("connection error", e);
            broken = true;
            throw e;
        } finally {
            closeResource(jedis, broken);
        }
    }

    protected void closeResource(Jedis jedis, boolean connectionBroken) {
        if (jedis != null) {
            try {
                if (connectionBroken) {
                    jedisPool.returnBrokenResource(jedis);
                } else {
                    jedisPool.returnResource(jedis);
                }
            } catch (Exception e) {
                logger.error("Error happen when return jedis to pool, try to close it directly.", e);
                closeJedis(jedis);
            }
        }
    }

    public static void closeJedis(Jedis jedis) {
        if ((jedis != null) && jedis.isConnected()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception e) {
                }
                jedis.disconnect();
            } catch (Exception e) {
            }
        }
    }

    public String get(final String key) {
        return execute(new JedisAction<String>() {

            
            public String action(Jedis jedis) {
                return jedis.get(key);
            }
        });
    }

    public byte[] get(final byte[] key) {
        return execute(new JedisAction<byte[]>() {

            
            public byte[] action(Jedis jedis) {
                return jedis.get(key);
            }
        });
    }


    public void set(final String key, final String value) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.set(key, value);
            }
        });
    }

    public void set(final byte[] key, final byte[] value) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.set(key, value);
            }
        });
    }

    public void expire(final String key, final int milliseconds) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.expire(key, milliseconds);
            }
        });
    }


    public void setSadd(final String key, final String... value) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.sadd(key, value);
            }
        });
    }


    public void setex(final String key, final String value, final int seconds) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.setex(key, seconds, value);
            }
        });
    }

    public void set2Bean(final String key, final Object object) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.set(key.getBytes(), SerializeUtil.serialize(object));
            }
        });
    }

    public void set2Beanex(final String key, final int seconds, final Object object) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.setex(key.getBytes(), seconds, SerializeUtil.serialize(object));
            }
        });
    }

    public Object get2Bean(final String key) {
        return execute(new JedisAction<Object>() {

            
            public Object action(Jedis jedis) {
                byte[] value = jedis.get(key.getBytes());
                return SerializeUtil.unserialize(value);
            }
        });
    }


    public void setex(final String key, final String value, final long timeout, final TimeUnit unit) {
        Long seconds = TimeoutUtils.toSeconds(timeout, unit);
        setex(key, value, seconds.intValue());
    }

    /**
     * 如果key还不存在则进行设置，返回true，否则返回false.
     */
    public Boolean setnx(final String key, final String value) {
        return execute(new JedisAction<Boolean>() {

            
            public Boolean action(Jedis jedis) {
                return jedis.setnx(key, value) == 1 ? true : false;
            }
        });
    }

    public Boolean del(final String... keys) {
        return execute(new JedisAction<Boolean>() {

            
            public Boolean action(Jedis jedis) {
                return jedis.del(keys) == 1 ? true : false;
            }
        });
    }

    public Boolean del(final byte[] keys) {
        return execute(new JedisAction<Boolean>() {

            
            public Boolean action(Jedis jedis) {
                return jedis.del(keys) == 1 ? true : false;
            }
        });
    }

    public String hget(final String key, final String field) {
        return execute(new JedisAction<String>() {
            
            public String action(Jedis jedis) {
                return jedis.hget(key, field);
            }
        });
    }

    public byte[] hget(final byte[] key, final byte[] field) {
        return execute(new JedisAction<byte[]>() {
            
            public byte[] action(Jedis jedis) {
                return jedis.hget(key, field);
            }
        });
    }

    public void hset(final byte[] key, final byte[] field, final byte[] value) {
        execute(new JedisActionNoResult() {

            
            public void action(Jedis jedis) {
                jedis.hset(key, field, value);
            }
        });
    }

    public void hset(final String key, final String field, final String value) {
        execute(new JedisActionNoResult() {

            
            public void action(Jedis jedis) {
                jedis.hset(key, field, value);
            }
        });
    }

    public void hmset(final String key, final Map<String, String> map) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.hmset(key, map);
            }
        });
    }

    public Map<String, String> hgetAll(final String key) {
        return execute(new JedisAction<Map<String, String>>() {
            
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(key);
            }
        });
    }

    public void lpush(final String key, final String... values) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.lpush(key, values);
            }
        });
    }

    public void rpush(final String key, final String... values) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.rpush(key, values);
            }
        });
    }

    public String rpop(final String key) {
        return execute(new JedisAction<String>() {
            
            public String action(Jedis jedis) {
                return jedis.rpop(key);
            }
        });
    }

    public Long llen(final String key) {
        return execute(new JedisAction<Long>() {
            
            public Long action(Jedis jedis) {
                return jedis.llen(key);
            }
        });
    }

    public String ltrim(final String key, final Long start, final Long end) {
        return execute(new JedisAction<String>() {
            
            public String action(Jedis jedis) {
                return jedis.ltrim(key, start, end);
            }
        });
    }

    public Boolean zadd(final String key, final String member, final double score) {
        return execute(new JedisAction<Boolean>() {
            
            public Boolean action(Jedis jedis) {
                return jedis.zadd(key, score, member) == 1 ? true : false;
            }
        });
    }

    public Boolean zrem(final String key, final String member) {
        return execute(new JedisAction<Boolean>() {
            
            public Boolean action(Jedis jedis) {
                return jedis.zrem(key, member) == 1 ? true : false;
            }
        });
    }

    public Boolean zrem(final String key, final String... member) {
        return execute(new JedisAction<Boolean>() {
            
            public Boolean action(Jedis jedis) {
                return jedis.zrem(key, member) == 1 ? true : false;
            }
        });
    }

    public Long zremrangeByRank(final String key, final Long start, final Long end) {
        return execute(new JedisAction<Long>() {
            
            public Long action(Jedis jedis) {
                return jedis.zremrangeByRank(key, start, end);
            }
        });
    }

    public Long zcard(final String key) {
        return execute(new JedisAction<Long>() {
            
            public Long action(Jedis jedis) {
                return jedis.zcard(key);
            }
        });
    }

    public Boolean sismember(final String key, final String value) {
        return execute(new JedisAction<Boolean>() {

            
            public Boolean action(Jedis jedis) {
                return jedis.sismember(key, value);
            }
        });
    }

    public void srem(final String key, final String... values) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.srem(key, values);
            }
        });
    }

    public Boolean exists(final String key) {
        return execute(new JedisAction<Boolean>() {

            
            public Boolean action(Jedis jedis) {
                return jedis.exists(key);
            }
        });
    }

    public Set<String> smembers(final String key) {
        return execute(new JedisAction<Set<String>>() {
            
            public Set<String> action(Jedis jedis) {
                return jedis.smembers(key);
            }
        });
    }

    public Set<String> keys(final String key) {
        return execute(new JedisAction<Set<String>>() {
            
            public Set<String> action(Jedis jedis) {
                return jedis.keys(key);
            }
        });
    }

    public Long ttl(final String key) {
        return execute(new JedisAction<Long>() {
            
            public Long action(Jedis jedis) {
                return jedis.ttl(key);
            }
        });
    }

    public void hdel(final String key, final String field) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.hdel(key, field);
            }
        });
    }

    public void hdel(final byte[] key, final byte[] field) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.hdel(key, field);
            }
        });
    }

    public Set<byte[]> hkeys(final byte[] key) {
        return execute(new JedisAction<Set<byte[]>>() {
            
            public Set<byte[]> action(Jedis jedis) {
                return jedis.hkeys(key);
            }
        });
    }

    public Collection<byte[]> hvals(final byte[] key) {
        return execute(new JedisAction<Collection<byte[]>>() {
            
            public Collection<byte[]> action(Jedis jedis) {
                return jedis.hvals(key);
            }
        });
    }


    public Long hlen(final byte[] key) {
        return execute(new JedisAction<Long>() {
            
            public Long action(Jedis jedis) {
                return jedis.hlen(key);
            }
        });
    }

    public void hdel(final byte[] key) {
        execute(new JedisActionNoResult() {
            
            public void action(Jedis jedis) {
                jedis.hdel(key);
            }
        });
    }

    public Set<String> zrange(final String key, final Long start, final Long end) {
        return execute(new JedisAction<Set<String>>() {
            
            public Set<String> action(Jedis jedis) {
                return jedis.zrange(key, start, end);
            }
        });
    }


    public interface JedisAction<T> {
        T action(Jedis jedis);
    }

    public interface JedisActionNoResult {
        void action(Jedis jedis);
    }
}
