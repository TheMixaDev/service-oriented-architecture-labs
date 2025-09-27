package com.aeeph.routeservice.service;

import com.aeeph.routeservice.model.Route;
import com.aeeph.routeservice.repository.RouteRepository;
import com.aeeph.routeservice.exception.ResourceNotFoundException;
import com.aeeph.routeservice.specification.RouteSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    @Autowired
    public RouteServiceImpl(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public Page<Route> getAllRoutes(Map<String, String> filters, Pageable pageable) {
        Specification<Route> spec = Specification.where(null);
        if (filters != null && !filters.isEmpty()) {
            spec = RouteSpecification.getRoutesByFilters(filters);
        }
        return routeRepository.findAll(spec, pageable);
    }

    @Override
    public Route createRoute(Route route) {
        return routeRepository.save(route);
    }

    @Override
    public Optional<Route> getRouteById(Long id) {
        return routeRepository.findById(id);
    }

    @Override
    public Route updateRoute(Long id, Route routeDetails) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));

        route.setName(routeDetails.getName());
        route.setCoordinates(routeDetails.getCoordinates());
        route.setFrom(routeDetails.getFrom());
        route.setTo(routeDetails.getTo());
        route.setDistance(routeDetails.getDistance());

        return routeRepository.save(route);
    }

    @Override
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }

    @Override
    public Optional<Route> getMaxByFrom() {
        return routeRepository.findTopByOrderByFromLocationDesc().stream().findFirst();
    }

    @Override
    public List<Route> findRoutesByNameStartingWith(String prefix) {
        return routeRepository.findByNameStartingWith(prefix);
    }

    @Override
    public List<Integer> getUniqueDistances() {
        return routeRepository.findUniqueDistances();
    }
}
