package com.aeeph.routeservice.specification;

import com.aeeph.routeservice.model.Route;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class RouteSpecification {

    public static Specification<Route> getRoutesByFilters(Map<String, String> filters) {
        return (Root<Route> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            filters.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    try {
                        if ("creationDate".equals(key)) {
                            try {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                                predicates.add(criteriaBuilder.equal(root.get(key), format.parse(value)));
                            } catch (ParseException e) {
                            }
                        } else if (key.contains(".")) {
                            String[] keys = key.split("\\.", 2);
                            String objectName = keys[0];
                            String fieldName = keys[1];

                            if ("from".equals(objectName)) {
                                objectName = "fromLocation";
                            } else if ("to".equals(objectName)) {
                                objectName = "toLocation";
                            }
                            
                            predicates.add(criteriaBuilder.equal(root.get(objectName).get(fieldName), value));
                        } else {
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
