package com.aeeph.routeservice.repository;

import com.aeeph.routeservice.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {
    List<Route> findByNameStartingWith(String prefix);

    @Query("SELECT DISTINCT r.distance FROM Route r ORDER BY r.distance ASC")
    List<Integer> findUniqueDistances();

    List<Route> findTopByOrderByFromLocationDesc();
}
