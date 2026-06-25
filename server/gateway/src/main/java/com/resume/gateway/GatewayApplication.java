package com.resume.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class GatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(GatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public ApplicationRunner startupRunner(Environment env) {
        return args -> {
            String port = env.getProperty("server.port", "8080");
            String appName = env.getProperty("spring.application.name", "resume-gateway");
            log.info("");
            log.info("================================================================");
            log.info("  ✅ {} 启动成功！", appName);
            log.info("  网关端口: {}", port);
            log.info("  网关地址: http://localhost:{}", port);
            log.info("================================================================");
            log.info("");
        };
    }
}
