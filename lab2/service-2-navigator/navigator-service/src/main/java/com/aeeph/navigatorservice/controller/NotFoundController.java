package com.aeeph.navigatorservice.controller;

import com.aeeph.navigatorservice.model.ErrorResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@RestController
public class NotFoundController {

  @RequestMapping("/**")
  public ResponseEntity<?> handleNotFound(HttpServletRequest request) {
    String path = request.getRequestURI();

    if (path.endsWith("/public/index.html")) {
      return serveStaticFile("public/index.html", MediaType.TEXT_HTML);
    } else if (path.endsWith("/public/app.js")) {
      return serveStaticFile("public/app.js", MediaType.valueOf("application/javascript"));
    } else if (path.endsWith("/public/style.css")) {
      return serveStaticFile("public/style.css", MediaType.valueOf("text/css"));
    }
    
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Страница не найдена");
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    
    return new ResponseEntity<>(errorResponse, headers, HttpStatus.NOT_FOUND);
  }
  
  private ResponseEntity<String> serveStaticFile(String resourcePath, MediaType mediaType) {
    try {
      ClassPathResource resource = new ClassPathResource(resourcePath);
      String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
      
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(mediaType);
      
      return new ResponseEntity<>(content, headers, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
