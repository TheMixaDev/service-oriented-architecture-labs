package com.aeeph.routeservice.config;

import com.aeeph.routeservice.exception.SoapExceptionHandler;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.Properties;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, "/ws/*");
  }

  @Bean(name = "routes")
  public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema routesSchema) {
    DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
    wsdl11Definition.setPortTypeName("RoutesPort");
    wsdl11Definition.setLocationUri("/ws");
    wsdl11Definition.setTargetNamespace("http://aeeph.com/routeservice");
    wsdl11Definition.setSchema(routesSchema);
    return wsdl11Definition;
  }

  @Bean
  public XsdSchema routesSchema() {
    return new SimpleXsdSchema(new ClassPathResource("routes.xsd"));
  }

  @Bean
  public SoapFaultMappingExceptionResolver exceptionResolver() {
    SoapExceptionHandler exceptionResolver = new SoapExceptionHandler();

    SoapFaultDefinition faultDefinition = new SoapFaultDefinition();
    faultDefinition.setFaultCode(SoapFaultDefinition.SERVER);
    exceptionResolver.setDefaultFault(faultDefinition);

    Properties errorMappings = new Properties();
    errorMappings.setProperty(Exception.class.getName(), SoapFaultDefinition.SERVER.toString());
    errorMappings.setProperty(
        "com.aeeph.routeservice.exception.ResourceNotFoundException",
        SoapFaultDefinition.CLIENT.toString());
    errorMappings.setProperty(
        "com.aeeph.routeservice.exception.BadRequestException",
        SoapFaultDefinition.CLIENT.toString());
    exceptionResolver.setExceptionMappings(errorMappings);
    exceptionResolver.setOrder(1);
    return exceptionResolver;
  }
}

