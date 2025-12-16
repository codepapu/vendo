package com.nexus.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class NexusNotificationServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NexusNotificationServiceApplication.class, args);
    }
}
