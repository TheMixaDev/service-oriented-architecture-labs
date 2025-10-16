package com.aeeph.navigatorservice.service;

import com.aeeph.navigatorservice.exception.BadRequestException;
import com.aeeph.navigatorservice.exception.ConflictException;
import com.aeeph.navigatorservice.exception.ResourceNotFoundException;
import com.aeeph.navigatorservice.exception.ServiceUnavailableException;
import com.aeeph.navigatorservice.model.Priority;
import com.aeeph.navigatorservice.model.Route;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class NavigatorServiceImpl implements NavigatorService {

  private final RestTemplate restTemplate;
  private final String routesServiceUrl;

  public NavigatorServiceImpl(
      RestTemplate restTemplate
//      ,@Value("${routes.service.url}") String routesServiceUrl
  ) {
    this.restTemplate = restTemplate;
//    this.routesServiceUrl = routesServiceUrl;
    this.routesServiceUrl = "https://127.0.0.1:27297/api/v1/routes";
  }

  @Override
  public Optional<Route> findOptimalRoute(long fromId, long toId, boolean shortest) {
    Route startNodeRoute = getRouteByIdWithErrorHandling(fromId);
    Route endNodeRoute = getRouteByIdWithErrorHandling(toId);

    String startLocationName = startNodeRoute.getName();
    String endLocationName = endNodeRoute.getName();

    int distance =
        shortest
            ? startNodeRoute.getCoordinates().distanceLinear(endNodeRoute.getCoordinates())
            : startNodeRoute.getCoordinates().distanceTo(endNodeRoute.getCoordinates());

    Route syntheticRoute = new Route();
    syntheticRoute.setFrom(startNodeRoute.getCoordinates().toLocation(startLocationName));
    syntheticRoute.setTo(endNodeRoute.getCoordinates().toLocation(endLocationName));
    syntheticRoute.setDistance(distance);
    syntheticRoute.setName("Маршрут " + startLocationName + "-" + endLocationName);
    syntheticRoute.setPriority(Priority.HIGH);

    return Optional.of(syntheticRoute);
  }

  @Override
  public Route createRouteByIds(long fromId, long toId, int distance) {
    if (distance <= 1) {
      throw new BadRequestException("Параметр distance должен быть больше 1");
    }

    Route fromRoute = getRouteByIdWithErrorHandling(fromId);
    Route toRoute = getRouteByIdWithErrorHandling(toId);

    if (fromId == toId) {
      throw new ConflictException("Маршрут между этими точками уже существует");
    }

    Route newRoute = new Route();
    newRoute.setName(
        "Route from " + fromRoute.getFrom().getName() + " to " + toRoute.getTo().getName());
    newRoute.setFrom(fromRoute.getFrom());
    newRoute.setTo(toRoute.getTo());
    newRoute.setCoordinates(fromRoute.getCoordinates());
    newRoute.setDistance(distance);
    newRoute.setPriority(Priority.HIGH);

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_XML);
      HttpEntity<Route> request = new HttpEntity<>(newRoute, headers);
      return restTemplate.postForObject(routesServiceUrl, request, Route.class);
    } catch (RestClientException e) {
      throw new ServiceUnavailableException("Внешний сервис недоступен");
    }
  }

  private Route getRouteByIdWithErrorHandling(long id) {
    try {
      Route route = restTemplate.getForObject(routesServiceUrl + "/" + id, Route.class);
      if (route == null) {
        throw new ResourceNotFoundException("Маршрут с ID " + id + " не найден");
      }
      return route;
    } catch (HttpClientErrorException.NotFound e) {
      throw new ResourceNotFoundException("Маршрут с ID " + id + " не найден");
    } catch (RestClientException e) {
      throw new ServiceUnavailableException("Внешний сервис недоступен");
    }
  }
}
