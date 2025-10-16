package com.aeeph.routeservice.controller;

import com.aeeph.routeservice.model.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotFoundController {

  @RequestMapping("/**")
  public ResponseEntity<ErrorResponse> handleNotFound() {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Страница не найдена");
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    
    return new ResponseEntity<>(errorResponse, headers, HttpStatus.NOT_FOUND);
  }
}
