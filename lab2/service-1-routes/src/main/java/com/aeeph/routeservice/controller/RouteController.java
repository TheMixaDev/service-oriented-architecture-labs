package com.aeeph.routeservice.controller;

import com.aeeph.routeservice.exception.ResourceNotFoundException;
import com.aeeph.routeservice.model.Route;
import com.aeeph.routeservice.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/routes", produces = "application/xml")
public class RouteController {

    private final RouteService routeService;

    @Autowired
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<Route>> getRoutes(
            @RequestParam(required = false) String[] sort,
            @RequestParam(required = false) Map<String, String> filter,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null) {
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split("_");
                orders.add(new Sort.Order(_sort.length > 1 && _sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, _sort[0]));
            }
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(orders));
        Page<Route> routePage = routeService.getAllRoutes(filter, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(routePage.getTotalElements()));

        return ResponseEntity.ok().headers(headers).body(routePage.getContent());
    }

    @PostMapping(consumes = "application/xml")
    public ResponseEntity<Route> createRoute(@RequestBody Route route) {
        Route createdRoute = routeService.createRoute(route);
        return ResponseEntity.status(201).body(createdRoute);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable Long id) {
        Route route = routeService.getRouteById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        return ResponseEntity.ok(route);
    }

    @PutMapping(value = "/{id}", consumes = "application/xml")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @RequestBody Route routeDetails) {
        Route updatedRoute = routeService.updateRoute(id, routeDetails);
        return ResponseEntity.ok(updatedRoute);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/max-by-from")
    public ResponseEntity<Route> getMaxByFrom() {
        Route route = routeService.getMaxByFrom()
                .orElseThrow(() -> new ResourceNotFoundException("No routes found"));
        return ResponseEntity.ok(route);
    }

    @GetMapping("/name-starts-with/{substring}")
    public ResponseEntity<List<Route>> getRoutesByNamePrefix(@PathVariable String substring) {
        List<Route> routes = routeService.findRoutesByNameStartingWith(substring);
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/distances/unique")
    public ResponseEntity<List<Integer>> getUniqueDistances() {
        List<Integer> distances = routeService.getUniqueDistances();
        return ResponseEntity.ok(distances);
    }
}
