package com.bharathandukuri.compilr.protection.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "compilr.protection")
public class ProtectionProperties {

    private RateLimitProperties rateLimit = new RateLimitProperties();
    private QueueProperties queue = new QueueProperties();

    @Getter
    @Setter
    public static class RateLimitProperties {
        private boolean enabled = true;
        private int maxRequests = 15;
        private int windowSeconds = 60;
    }

    @Getter
    @Setter
    public static class QueueProperties {
        private int defaultProcessingCapacity = 3;
        private int defaultQueueCapacity = 10;
        private long maxQueueWaitTimeoutMs = 10000L;
        private int globalMaxProcessing = 12;
        private int globalMaxQueued = 50;

        /**
         * Optional per-environment capacity overrides keyed by environment ID (e.g. "java-21", "python-3.12")
         */
        private Map<String, EnvironmentCapacity> environments = new HashMap<>();
    }

    @Getter
    @Setter
    public static class EnvironmentCapacity {
        private Integer processingCapacity;
        private Integer queueCapacity;
    }
}
