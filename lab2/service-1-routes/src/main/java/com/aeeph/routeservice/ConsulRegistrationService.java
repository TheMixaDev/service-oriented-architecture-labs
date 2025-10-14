package com.aeeph.routeservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "spring.cloud.consul.discovery.enabled", havingValue = "true")
public class ConsulRegistrationService {

    @Autowired
    private ConsulServiceRegistry serviceRegistry;

    @Autowired
    private ConsulAutoRegistration registration;

    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        try {
            System.out.println("Manually registering service with Consul...");
            serviceRegistry.register(registration);
            System.out.println("Service registered successfully!");
        } catch (Exception e) {
            System.err.println("Failed to register service: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

