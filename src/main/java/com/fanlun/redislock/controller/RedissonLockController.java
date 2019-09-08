package com.fanlun.redislock.controller;

import com.fanlun.redislock.dao.AmountDao;
import com.fanlun.redislock.entity.Amount;
import com.fanlun.redislock.service.RedissonService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class RedissonLockController {

    @Autowired
    private RedissonService redissonService;
    @Autowired
    AmountDao amountDao;

    @GetMapping("/getRLock")
    public void testGetRLock() {
        String lockKey = "redis@Lock";
        RLock rLock = redissonService.getRLock(lockKey);
        while (true) {
            try {
                boolean isLock = rLock.tryLock(20, TimeUnit.SECONDS);
                if (isLock) {
                    Amount amount = amountDao.selectById(1L);
                    amount.setCount(amount.getCount() - 1);
                    amountDao.updateById(amount);
                    rLock.unlock();
                    return;
                }
            } catch (InterruptedException e) {
                log.error(e.getLocalizedMessage());
                rLock.unlock();
            }

        }
    }
}
