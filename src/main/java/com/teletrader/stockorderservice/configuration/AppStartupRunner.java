package com.teletrader.stockorderservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements ApplicationRunner {
    @Value("${server.port}")
    private int serverPort;

    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String swaggerPath;

    @Override
    public void run(ApplicationArguments args) {
        String swaggerUrl = "http://localhost:" + serverPort + swaggerPath;

        System.out.println("\n=====================================================================");
        System.out.println("🚀 Swagger UI is available at: " + swaggerUrl);
        System.out.println("=====================================================================\n");
    }
}

