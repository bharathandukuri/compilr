package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.CompilerOptionsDto;
import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.service.CompilerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
public abstract class BaseEnvironmentTest {

    public static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:8-alpine"))
                    .withExposedPorts(6379);

    static {
        if (!REDIS_CONTAINER.isRunning()) {
            REDIS_CONTAINER.start();
        }
    }

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
    }

    @Autowired
    protected CompilerService compilerService;

    protected ExecuteResponse execute(String language, String code) {
        return execute(language, code, "", 10000L, 524288L);
    }

    protected ExecuteResponse execute(String language, String code, String stdin) {
        return execute(language, code, stdin, 10000L, 524288L);
    }

    protected ExecuteResponse execute(String language, String code, String stdin, Long timeLimitMs, Long memoryLimitKb) {
        ExecuteRequest request = new ExecuteRequest(
                language,
                code,
                stdin,
                new CompilerOptionsDto(timeLimitMs, memoryLimitKb)
        );
        return compilerService.execute(request);
    }
}
