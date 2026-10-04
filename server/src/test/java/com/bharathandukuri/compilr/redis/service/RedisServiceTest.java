package com.bharathandukuri.compilr.redis.service;

import com.bharathandukuri.compilr.redis.service.impl.RedisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RedisServiceImpl redisService;

    @BeforeEach
    void setUp() {
        redisService = new RedisServiceImpl(redisTemplate, objectMapper);
    }

    @Test
    @DisplayName("Set and get value successfully")
    void setAndGet() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test-key")).thenReturn("test-value");

        redisService.set("test-key", "test-value");
        verify(valueOperations).set("test-key", "test-value");

        Object retrieved = redisService.get("test-key");
        assertThat(retrieved).isEqualTo("test-value");
    }

    @Test
    @DisplayName("Set with duration TTL successfully")
    void setWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Duration ttl = Duration.ofSeconds(60);

        redisService.set("ttl-key", "ttl-val", ttl);
        verify(valueOperations).set("ttl-key", "ttl-val", ttl);
    }

    @Test
    @DisplayName("Get with target class conversion")
    void getWithTargetClass() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("str-key")).thenReturn("hello");

        String result = redisService.get("str-key", String.class);
        assertThat(result).isEqualTo("hello");
    }

    @Test
    @DisplayName("hasKey returns true when key exists")
    void hasKey() {
        when(redisTemplate.hasKey("key-1")).thenReturn(true);
        boolean exists = redisService.hasKey("key-1");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("delete returns true when key is removed")
    void deleteKey() {
        when(redisTemplate.delete("key-2")).thenReturn(true);
        boolean deleted = redisService.delete("key-2");
        assertThat(deleted).isTrue();
    }
}
