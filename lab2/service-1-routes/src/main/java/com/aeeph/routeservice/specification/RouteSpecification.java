package com.aeeph.routeservice.specification;

import com.aeeph.routeservice.model.Route;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteSpecification {

    public static Specification<Route> getRoutesByFilters(Map<String, String> filters) {
        return (Root<Route> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            filters.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    try {
                        String[] keys = key.split("\\.");
                        if (keys.length == 2) {
                            // аля "coordinates.x"
                            predicates.add(criteriaBuilder.equal(root.get(keys[0]).get(keys[1]), value));
                        } else {
                             // аля "name"
                            predicates.add(criteriaBuilder.equal(root.get(key), value));
                        }
                    } catch (IllegalArgumentException e) {
                    }
                }
            });

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
