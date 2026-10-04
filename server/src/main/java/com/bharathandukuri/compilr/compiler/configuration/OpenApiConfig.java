package com.bharathandukuri.compilr.compiler.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI compilrOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Compilr - Online Compiler Engine API")
                        .description("High-performance, secure online code execution and compilation API powered by multi-tenant Linux sandbox containers.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Bharath")
                                .url("https://github.com/bharathandukuri/compilr"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://spring.io")));
    }
}
