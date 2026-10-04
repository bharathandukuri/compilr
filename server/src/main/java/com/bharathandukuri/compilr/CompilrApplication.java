package com.bharathandukuri.compilr;

import com.bharathandukuri.compilr.compiler.configuration.CompilerProperties;
import com.bharathandukuri.compilr.execution.config.DockerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({DockerProperties.class, CompilerProperties.class})
public class CompilrApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompilrApplication.class, args);
    }
}
