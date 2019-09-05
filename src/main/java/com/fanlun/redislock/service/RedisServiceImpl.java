package com.fanlun.redislock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private JedisPool jedisPool;

    /**
     * 处理jedis请求
     * @param f 处理逻辑，通过lambda行为参数化
     * @return 处理结果
     */
    private Object excuteByJedis(Function<Jedis, Object> f) {
        try (Jedis jedis = jedisPool.getResource()) {
            return f.apply(jedis);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, int time) {
        return (String) this.excuteByJedis(j -> j.set(key, value, nxxx, expx, time));
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        return this.excuteByJedis(j -> j.eval(script, keys, args));
    }
}
