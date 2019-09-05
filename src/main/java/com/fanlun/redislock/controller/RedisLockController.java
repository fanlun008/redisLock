package com.fanlun.redislock.controller;

import com.fanlun.redislock.dao.AmountDao;
import com.fanlun.redislock.entity.Amount;
import com.fanlun.redislock.service.RedisDistributedLock;
import com.fanlun.redislock.service.RedisDistributedLock2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class RedisLockController {

    @Autowired
    private RedisDistributedLock2 redisDistributedLock;
    @Autowired
    AmountDao amountDao;

    @GetMapping("/redistest")
    public List<Amount> distributeLock() {

        List<Amount> amounts = amountDao.selectList(null);
        return amounts;
    }

//    @GetMapping("/testNoRedisLock")
//    public List<Amount> testNoRedisLock() {
//        String lock_str = null;
//
//        try {
//            lock_str = redisDistributedLock.acquire();
//            Amount amount = amountDao.selectById(1L);
//            amount.setCount(amount.getCount() - 1);
//            amountDao.updateById(amount);
//        } finally {
//            redisDistributedLock.release(lock_str);
//        }
//        return amountDao.selectList(null);
//
//    }

    @GetMapping("/testNoRedisLock")
    public List<Amount> testNoRedisLock2() {

        final String lockKey = "redis@lockKey";

        String requestId = UUID.randomUUID().toString();

        while (true){
            boolean isLock = redisDistributedLock.tryGetDistributedLock(lockKey, requestId, 10);
            if (isLock) {
                Amount amount = amountDao.selectById(1L);
                amount.setCount(amount.getCount() - 1);
                amountDao.updateById(amount);
                redisDistributedLock.releaseDistributedLock(lockKey, requestId);
                return amountDao.selectList(null);
            }
        }

    }

}
