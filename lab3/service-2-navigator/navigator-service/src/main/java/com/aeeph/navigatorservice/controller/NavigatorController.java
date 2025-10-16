package com.aeeph.navigatorservice.controller;

import com.aeeph.navigatorservice.exception.ResourceNotFoundException;
import com.aeeph.navigatorservice.model.Route;
import com.aeeph.navigatorservice.service.NavigatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/navigator", produces = "application/xml")
public class NavigatorController {

  private final NavigatorService navigatorService;

  public NavigatorController(NavigatorService navigatorService) {
    this.navigatorService = navigatorService;
  }

  @GetMapping("/route/{id-from}/{id-to}/{shortest}")
  public ResponseEntity<Route> findOptimalRoute(
      @PathVariable("id-from") long idFrom,
      @PathVariable("id-to") long idTo,
      @PathVariable boolean shortest) {
    Route route =
        navigatorService
            .findOptimalRoute(idFrom, idTo, shortest)
            .orElseThrow(() -> new ResourceNotFoundException("Маршрут не найден"));
    return ResponseEntity.ok(route);
  }

  @PostMapping("/route/add/{id-from}/{id-to}/{distance}")
  public ResponseEntity<Route> createRouteByIds(
      @PathVariable("id-from") long idFrom,
      @PathVariable("id-to") long idTo,
      @PathVariable int distance) {
    Route newRoute = navigatorService.createRouteByIds(idFrom, idTo, distance);
    return ResponseEntity.status(201).body(newRoute);
  }
}
