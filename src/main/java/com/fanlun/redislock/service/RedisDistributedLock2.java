package com.fanlun.redislock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
public class RedisDistributedLock2 {
    /**
     * 用来表示 setnx 的参数
     */
    private static final String SET_IF_NOT_EXIST = "NX";
    /**
     * EX = seconds（秒）; PX = milliseconds（毫秒）
     */
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    /**
     * 释放锁成功返回值
     */
    private static final Long RELEASE_SUCCESS = 1L;
    /**
     * 加锁成功返回值
     */
    private static final String LOCK_SUCCESS = "OK";
    /**
     * 超时时间 10s，单位是由 {@code SET_WITH_EXPIRE_TIME }
     */
    public static final int TIMEOUT = 10;

    /**
     * 常量前缀
     */
    private static final String REDIS_LOCK_KEY_PREFIX = "redis_lock_key_prefix";

    /**
     * 常量连接符
     */
    private static final String REDIS_LOCK_PLUS = "@";

    /**
     * 可用 key前缀
     */
    public static final String REDIS_LOCK_KEY = REDIS_LOCK_KEY_PREFIX + REDIS_LOCK_PLUS;

    @Autowired
    RedisService redisService;


    /**
     * 生成分布式锁密钥
     *
     * @param key
     * @return
     */
    public String generateLockKey(String key) {
        if (key == null || key.equals("")) {
            return "";
        }
        return REDIS_LOCK_KEY + UUID.randomUUID().toString();
    }

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey    锁
     * @param requestId  请求标识
     * @param expireTime 超期时间
     * @return 是否获取成功  true：成功获取锁；false：未获取锁资格
     */
    public boolean tryGetDistributedLock(String lockKey, String requestId, int expireTime) {

        String result = redisService.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);

        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁
     * @param requestId 请求标识
     * @return 是否释放成功  true：手动解锁成功；false：手动解锁失败
     */
    public boolean releaseDistributedLock(String lockKey, String requestId) {

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = redisService.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        log.info("解锁失败，解锁用户：{}， 锁值为：{}", requestId, lockKey);
        return false;
    }
}
