package com.aeeph.routeservice.service;

import com.aeeph.routeservice.exception.ResourceNotFoundException;
import com.aeeph.routeservice.model.Route;
import com.aeeph.routeservice.repository.RouteRepository;
import com.aeeph.routeservice.specification.RouteSpecification;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class RouteServiceImpl implements RouteService {

  private final RouteRepository routeRepository;

  public RouteServiceImpl(RouteRepository routeRepository) {
    this.routeRepository = routeRepository;
  }

  @Override
  public Page<Route> getAllRoutes(
      Map<String, String> filters, Map<String, String> operations, Pageable pageable) {
    Specification<Route> spec = Specification.where(null);
    if (filters != null && !filters.isEmpty()) {
      spec = RouteSpecification.getRoutesByFilters(filters, operations);
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
    Route route =
        routeRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Маршрут с указанным ID не найден"));

    route.setName(routeDetails.getName());
    route.setCoordinates(routeDetails.getCoordinates());
    route.setFromLocation(routeDetails.getFromLocation());
    route.setToLocation(routeDetails.getToLocation());
    route.setDistance(routeDetails.getDistance());
    route.setPriority(routeDetails.getPriority());

    return routeRepository.save(route);
  }

  @Override
  public void deleteRoute(Long id) {
    if (!routeRepository.existsById(id)) {
      throw new ResourceNotFoundException("Маршрут с указанным ID не найден");
    }
    routeRepository.deleteById(id);
  }

  @Override
  public Optional<Route> getMaxByFrom() {
    List<Route> list = routeRepository.findTopByFromMax();
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
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
