package com.bestfriend.danjjak.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
        "com.bestfriend.danjjak.health.controller",
        "com.bestfriend.danjjak.account.controller",
        "com.bestfriend.danjjak.common.error"
})
public class WebConfig {
}
