package com.aeeph.routeservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@ConditionalOnProperty(value = "spring.cloud.consul.discovery.enabled", havingValue = "true", matchIfMissing = true)
public class ConsulConfig {

    @Autowired
    private ConsulProperties consulProperties;

    @Bean
    @ConditionalOnMissingBean
    public ConsulAutoServiceRegistration consulAutoServiceRegistration(
            ConsulServiceRegistry registry,
            AutoServiceRegistrationProperties autoServiceRegistrationProperties,
            ConsulDiscoveryProperties properties,
            ConsulAutoRegistration consulRegistration) {

        return new ConsulAutoServiceRegistration(registry, autoServiceRegistrationProperties,
                properties, consulRegistration);
    }

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        System.out.println("Attempting to register service with Consul...");
    }
}

