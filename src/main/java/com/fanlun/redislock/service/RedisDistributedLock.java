package com.fanlun.redislock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
@Slf4j
public class RedisDistributedLock {
    @Autowired
    private RedisService redisService;

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
    private static final String REDIS_LOCK_KEY_PREFIX = "redis";

    /**
     * 常量连接符
     */
    private static final String REDIS_LOCK_PLUS = "@lock_key";

    /**
     * 可用 key前缀
     */
    public static final String REDIS_LOCK_KEY = REDIS_LOCK_KEY_PREFIX + REDIS_LOCK_PLUS;

    /**
     * 锁等待，防止线程饥饿
     */
    long acquireTimeout = 1 * 1000;

    //获取锁
    public String acquire() {
        try {
            long end = System.currentTimeMillis() + acquireTimeout;
            String requireToken = UUID.randomUUID().toString();
            while (System.currentTimeMillis() < end) {
                String result = redisService.set(REDIS_LOCK_KEY, requireToken, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, TIMEOUT);
                if (LOCK_SUCCESS.equals(result)) {
                    return requireToken;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            log.error("acquire lock due to error", e);
        }
        return null;
    }

    public boolean release(String requireToken) {
        if(requireToken == null){
            return false;
        }
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = new Object();
        try {
            result = redisService.eval(script,
                    Collections.singletonList(REDIS_LOCK_KEY), Collections.singletonList(requireToken));
            if (RELEASE_SUCCESS.equals(result)) {
                log.info("release lock success, requestToken:{}", requireToken);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return false;
    }


}
