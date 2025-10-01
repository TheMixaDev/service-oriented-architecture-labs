package com.aeeph.routeservice.controller;

import com.aeeph.routeservice.exception.ResourceNotFoundException;
import com.aeeph.routeservice.model.Route;
import com.aeeph.routeservice.model.RouteList;
import com.aeeph.routeservice.service.RouteService;
import com.aeeph.routeservice.model.DistanceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public ResponseEntity<RouteList> getRoutes(
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

        return ResponseEntity.ok().headers(headers).body(new RouteList(routePage.getContent()));
    }

    @PostMapping(consumes = "application/xml")
    public ResponseEntity<Route> createRoute(@Valid @RequestBody Route route) {
        Route createdRoute = routeService.createRoute(route);
        return ResponseEntity.status(201).body(createdRoute);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable Long id) {
        Route route = routeService.getRouteById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Маршрут с указанным ID не найден"));
        return ResponseEntity.ok(route);
    }

    @PutMapping(value = "/{id}", consumes = "application/xml")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @Valid @RequestBody Route routeDetails) {
        Route updatedRoute = routeService.updateRoute(id, routeDetails);
        return ResponseEntity.ok(updatedRoute);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/max/from")
    public ResponseEntity<Route> getMaxByFrom() {
        Route route = routeService.getMaxByFrom()
                .orElseThrow(() -> new ResourceNotFoundException("Маршрут с указанным ID не найден"));
        return ResponseEntity.ok(route);
    }

    @GetMapping("/name-starts-with/{substring}")
    public ResponseEntity<RouteList> getRoutesByNamePrefix(@PathVariable String substring) {
        List<Route> routes = routeService.findRoutesByNameStartingWith(substring);
        return ResponseEntity.ok(new RouteList(routes));
    }

    @GetMapping("/distances/unique")
    public ResponseEntity<DistanceList> getUniqueDistances() {
        List<Integer> distances = routeService.getUniqueDistances();
        return ResponseEntity.ok(new DistanceList(distances));
    }
}
