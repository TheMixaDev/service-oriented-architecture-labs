package com.aeeph.routeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RouteServiceApplication extends SpringBootServletInitializer {

  public RouteServiceApplication() {
    setRegisterErrorPageFilter(false);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(RouteServiceApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(RouteServiceApplication.class, args);
  }
}
