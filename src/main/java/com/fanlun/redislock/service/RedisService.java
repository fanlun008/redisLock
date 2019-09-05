package com.fanlun.redislock.service;

import java.util.List;

public interface RedisService {

    String set(String key, String value, String nxxx, String expx, int time);
    Object eval(String script, List<String> keys, List<String> args);
}
