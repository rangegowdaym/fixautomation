package com.fixautomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FixAutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixAutomationApplication.class, args);
    }
}
