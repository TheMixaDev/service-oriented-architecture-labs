package com.aeeph.routeservice.repository;

import com.aeeph.routeservice.model.Route;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository
    extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {
  List<Route> findByNameStartingWith(String prefix);

  @Query("SELECT DISTINCT r.distance FROM Route r ORDER BY r.distance ASC")
  List<Integer> findUniqueDistances();

  @Query(
      "SELECT r FROM Route r ORDER BY GREATEST(COALESCE(r.fromLocation.x,0),"
          + " COALESCE(r.fromLocation.y,0)) DESC, r.id ASC")
  List<Route> findTopByFromMax();
}
