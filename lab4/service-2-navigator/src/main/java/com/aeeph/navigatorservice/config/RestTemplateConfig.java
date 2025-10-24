package com.aeeph.navigatorservice.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    RestTemplate restTemplate;
    try {
      TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;
      SSLContext sslContext =
          SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

      SSLConnectionSocketFactory csf =
          new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
      CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

      HttpComponentsClientHttpRequestFactory requestFactory =
          new HttpComponentsClientHttpRequestFactory();
      requestFactory.setHttpClient(httpClient);

      restTemplate = builder.requestFactory(() -> requestFactory).build();
    } catch (Exception ex) {
      restTemplate = builder.build();
    }

    XmlMapper xmlMapper = new XmlMapper();
    MappingJackson2HttpMessageConverter xmlConverter =
        new MappingJackson2HttpMessageConverter(xmlMapper);
    xmlConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_XML));

    List<HttpMessageConverter<?>> messageConverters =
        new ArrayList<>(restTemplate.getMessageConverters());
    messageConverters.add(0, xmlConverter);

    restTemplate.setMessageConverters(messageConverters);

    return restTemplate;
  }
}
