package com.bharathandukuri.compilr.redis.service;

import java.time.Duration;

public interface RedisService {

    void set(String key, Object value);

    void set(String key, Object value, Duration timeout);

    Object get(String key);

    <T> T get(String key, Class<T> targetClass);

    boolean hasKey(String key);

    boolean delete(String key);

    boolean expire(String key, Duration timeout);

    Long getExpire(String key);

    boolean ping();
}
