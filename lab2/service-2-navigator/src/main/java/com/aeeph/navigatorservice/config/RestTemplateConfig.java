package com.aeeph.navigatorservice.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        
        XmlMapper xmlMapper = new XmlMapper();
        MappingJackson2HttpMessageConverter xmlConverter = new MappingJackson2HttpMessageConverter(xmlMapper);
        xmlConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_XML));
        
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>(restTemplate.getMessageConverters());
        messageConverters.add(0, xmlConverter);
        
        restTemplate.setMessageConverters(messageConverters);
        
        return restTemplate;
    }
}
