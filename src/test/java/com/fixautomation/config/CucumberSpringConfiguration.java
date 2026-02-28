package com.fixautomation.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = com.fixautomation.FixAutomationApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CucumberSpringConfiguration {
}
