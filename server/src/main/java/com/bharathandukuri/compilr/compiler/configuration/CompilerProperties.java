package com.bharathandukuri.compilr.compiler.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "compilr.compiler")
public class CompilerProperties {

    private int maxSourceSizeBytes = 256 * 1024; // 256 KB

    private int maxStdinSizeBytes = 256 * 1024; // 256 KB

    private int maxOutputSizeBytes = 1024 * 1024; // 1 MB

    private long defaultTimeLimitMs = 5000L;

    private long maxTimeLimitMs = 15000L;

    private long minTimeLimitMs = 100L;

    private Long defaultMemoryLimitKb = 262144L; // 256 MB
}
