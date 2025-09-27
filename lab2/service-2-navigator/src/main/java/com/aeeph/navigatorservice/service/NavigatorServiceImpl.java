package com.aeeph.navigatorservice.service;

import com.aeeph.navigatorservice.model.Route;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class NavigatorServiceImpl implements NavigatorService {

    private final RestTemplate restTemplate;
    private final String routesServiceUrl;

    public NavigatorServiceImpl(RestTemplate restTemplate, @Value("${routes.service.url}") String routesServiceUrl) {
        this.restTemplate = restTemplate;
        this.routesServiceUrl = routesServiceUrl;
    }

    @Override
    public Optional<Route> findOptimalRoute(long fromId, long toId, boolean shortest) {
        Route[] allRoutes = getAllRoutes();
        if (allRoutes == null) return Optional.empty();

        Route startNodeRoute = getRouteById(fromId);
        Route endNodeRoute = getRouteById(toId);
        if (startNodeRoute == null || endNodeRoute == null) return Optional.empty();
        
        String startLocationName = startNodeRoute.getFrom().getName();
        String endLocationName = endNodeRoute.getTo().getName();

        Map<String, List<Edge>> graph = new HashMap<>();
        for (Route route : allRoutes) {
            graph.putIfAbsent(route.getFrom().getName(), new ArrayList<>());
            graph.get(route.getFrom().getName()).add(new Edge(route.getTo().getName(), route.getDistance()));
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));

        for (String location : graph.keySet()) {
            distances.put(location, shortest ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        }
        distances.put(startLocationName, 0);
        pq.add(new Node(startLocationName, 0));

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();
            if (currentNode.name.equals(endLocationName)) break;

            List<Edge> neighbors = graph.getOrDefault(currentNode.name, Collections.emptyList());
            for (Edge edge : neighbors) {
                int newDist = distances.get(currentNode.name) + edge.weight;
                if (shortest && newDist < distances.get(edge.target)) {
                    distances.put(edge.target, newDist);
                    previousNodes.put(edge.target, currentNode.name);
                    pq.add(new Node(edge.target, newDist));
                } else if (!shortest && newDist > distances.get(edge.target)) {
                    distances.put(edge.target, newDist);
                    previousNodes.put(edge.target, currentNode.name);
                    pq.add(new Node(edge.target, newDist));
                }
            }
        }
        
        if (distances.get(endLocationName) != (shortest ? Integer.MAX_VALUE : Integer.MIN_VALUE)) {
            Route syntheticRoute = new Route();
            syntheticRoute.setFrom(startNodeRoute.getFrom());
            syntheticRoute.setTo(endNodeRoute.getTo());
            syntheticRoute.setDistance(distances.get(endLocationName));
            syntheticRoute.setName("Optimal path from " + startLocationName + " to " + endLocationName);
            return Optional.of(syntheticRoute);
        }

        return Optional.empty();
    }

    @Override
    public Route createRouteByIds(long fromId, long toId, int distance) {
        Route fromRoute = getRouteById(fromId);
        Route toRoute = getRouteById(toId);

        if (fromRoute == null || toRoute == null) {
            return null;
        }

        Route newRoute = new Route();
        newRoute.setName("Route from " + fromRoute.getFrom().getName() + " to " + toRoute.getTo().getName());
        newRoute.setFrom(fromRoute.getFrom());
        newRoute.setTo(toRoute.getTo());
        newRoute.setCoordinates(fromRoute.getCoordinates());
        newRoute.setDistance(distance);

        return restTemplate.postForObject(routesServiceUrl, newRoute, Route.class);
    }

    private Route getRouteById(long id) {
        return restTemplate.getForObject(routesServiceUrl + "/" + id, Route.class);
    }

    private Route[] getAllRoutes() {
        return restTemplate.getForObject(routesServiceUrl, Route[].class);
    }

    private static class Edge {
        String target;
        int weight;
        Edge(String target, int weight) { this.target = target; this.weight = weight; }
    }

    private static class Node {
        String name;
        int distance;
        Node(String name, int distance) { this.name = name; this.distance = distance; }
    }
}
