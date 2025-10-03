package com.aeeph.navigatorservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    // Forward requests to root to the SPA entry point
    registry.addViewController("/").setViewName("forward:/index.html");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Serve static resources from classpath:/public
    registry
        .addResourceHandler("/index.html", "/app.js", "/style.css")
        .addResourceLocations("classpath:/public/");
  }
}


