package com.nexus.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NexusAuthServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NexusAuthServiceApplication.class, args);
    }
}
