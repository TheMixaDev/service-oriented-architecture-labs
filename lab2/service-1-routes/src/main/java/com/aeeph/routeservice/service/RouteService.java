package com.aeeph.routeservice.service;

import com.aeeph.routeservice.model.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RouteService {
    Page<Route> getAllRoutes(Map<String, String> filters, Map<String, String> operations, Pageable pageable);
    Route createRoute(Route route);
    Optional<Route> getRouteById(Long id);
    Route updateRoute(Long id, Route routeDetails);
    void deleteRoute(Long id);
    Optional<Route> getMaxByFrom();
    List<Route> findRoutesByNameStartingWith(String prefix);
    List<Integer> getUniqueDistances();
}
