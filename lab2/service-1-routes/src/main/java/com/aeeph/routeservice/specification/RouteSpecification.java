package com.aeeph.routeservice.specification;

import com.aeeph.routeservice.exception.BadRequestException;
import com.aeeph.routeservice.model.Priority;
import com.aeeph.routeservice.model.Route;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class RouteSpecification {

  public static Specification<Route> getRoutesByFilters(
      Map<String, String> filters, Map<String, String> operations) {
    return (Root<Route> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filters == null || filters.isEmpty()) {
        return criteriaBuilder.conjunction();
      }

      filters.forEach(
          (key, value) -> {
            if (value != null && !value.isEmpty() && !key.endsWith("_op")) {
              String operator = getOperator(key, operations);
              Predicate predicate = buildPredicate(root, criteriaBuilder, key, value, operator);
              if (predicate != null) {
                predicates.add(predicate);
              }
            }
          });

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  private static String getOperator(String fieldName, Map<String, String> operations) {
    if (operations == null || operations.isEmpty()) {
      return "==";
    }
    return operations.getOrDefault(fieldName + "_op", "==");
  }

  private static Predicate buildPredicate(
      Root<Route> root, CriteriaBuilder cb, String key, String value, String operator) {
    try {
      if ("creationDate".equals(key)) {
        return buildDatePredicate(root, cb, key, value, operator);
      }

      if ("priority".equals(key)) {
        return buildEnumPredicate(root, cb, key, value, operator);
      }

      if (key.contains(".")) {
        return buildNestedPredicate(root, cb, key, value, operator);
      }

      return buildSimplePredicate(root, cb, key, value, operator);

    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      return null;
    }
  }

  private static Predicate buildDatePredicate(
      Root<Route> root, CriteriaBuilder cb, String key, String value, String operator) {
    try {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      format.setTimeZone(TimeZone.getTimeZone("UTC"));
      Date dateValue = format.parse(value);
      Path<Date> path = root.get(key);

      switch (operator) {
        case "==":
          return cb.equal(path, dateValue);
        case "!=":
          return cb.notEqual(path, dateValue);
        case ">":
          return cb.greaterThan(path, dateValue);
        case ">=":
          return cb.greaterThanOrEqualTo(path, dateValue);
        case "<":
          return cb.lessThan(path, dateValue);
        case "<=":
          return cb.lessThanOrEqualTo(path, dateValue);
        default:
          return cb.equal(path, dateValue);
      }
    } catch (ParseException e) {
      throw new BadRequestException(
          String.format(
              "Некорректные данные в URL запросе. Тип поля %s должен быть %s.", key, "date-time"));
    }
  }

  private static Predicate buildEnumPredicate(
      Root<Route> root, CriteriaBuilder cb, String key, String value, String operator) {
    try {
      Priority priorityValue = Priority.valueOf(value.toUpperCase());
      Path<Priority> path = root.get(key);

      switch (operator) {
        case "==":
          return cb.equal(path, priorityValue);
        case "!=":
          return cb.notEqual(path, priorityValue);
        case ">":
          {
            Predicate or = cb.disjunction();
            for (Priority p : Priority.values()) {
              if (p.ordinal() > priorityValue.ordinal()) {
                or = cb.or(or, cb.equal(path, p));
              }
            }
            return or;
          }
        case ">=":
          {
            Predicate or = cb.disjunction();
            for (Priority p : Priority.values()) {
              if (p.ordinal() >= priorityValue.ordinal()) {
                or = cb.or(or, cb.equal(path, p));
              }
            }
            return or;
          }
        case "<":
          {
            Predicate or = cb.disjunction();
            for (Priority p : Priority.values()) {
              if (p.ordinal() < priorityValue.ordinal()) {
                or = cb.or(or, cb.equal(path, p));
              }
            }
            return or;
          }
        case "<=":
          {
            Predicate or = cb.disjunction();
            for (Priority p : Priority.values()) {
              if (p.ordinal() <= priorityValue.ordinal()) {
                or = cb.or(or, cb.equal(path, p));
              }
            }
            return or;
          }
        default:
          return cb.equal(path, priorityValue);
      }
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(
          "Некорректные данные в URL запросе. Допустимые значения поля priority: LOW, MEDIUM,"
              + " HIGH.");
    }
  }

  private static Predicate buildNestedPredicate(
      Root<Route> root, CriteriaBuilder cb, String key, String value, String operator) {
    String[] keys = key.split("\\.", 2);
    String objectName = keys[0];
    String fieldName = keys[1];

    if ("from".equals(objectName)) {
      objectName = "fromLocation";
    } else if ("to".equals(objectName)) {
      objectName = "toLocation";
    }

    Path<?> path = root.get(objectName).get(fieldName);

    Class<?> javaType = path.getJavaType();

    if (String.class.equals(javaType)) {
      return buildStringPredicate(cb, path.as(String.class), value, operator);
    } else if (Number.class.isAssignableFrom(javaType)
        || javaType.equals(Double.class)
        || javaType.equals(double.class)
        || javaType.equals(Integer.class)
        || javaType.equals(int.class)
        || javaType.equals(Long.class)
        || javaType.equals(long.class)) {
      String expectedType =
          (javaType.equals(Double.class) || javaType.equals(double.class)) ? "double" : "integer";
      return buildNumericPredicate(key, expectedType, cb, path, value, operator);
    }

    return null;
  }

  private static Predicate buildSimplePredicate(
      Root<Route> root, CriteriaBuilder cb, String key, String value, String operator) {
    Path<?> path = root.get(key);
    Class<?> javaType = path.getJavaType();

    if (String.class.equals(javaType)) {
      return buildStringPredicate(cb, path.as(String.class), value, operator);
    } else if (Number.class.isAssignableFrom(javaType)
        || javaType.equals(Integer.class)
        || javaType.equals(int.class)
        || javaType.equals(Long.class)
        || javaType.equals(long.class)
        || javaType.equals(Double.class)
        || javaType.equals(double.class)) {
      String expectedType =
          (javaType.equals(Double.class) || javaType.equals(double.class)) ? "double" : "integer";
      return buildNumericPredicate(key, expectedType, cb, path, value, operator);
    }

    return null;
  }

  private static Predicate buildStringPredicate(
      CriteriaBuilder cb, Expression<String> path, String value, String operator) {
    switch (operator) {
      case "==":
        return cb.equal(path, value);
      case "!=":
        return cb.notEqual(path, value);
      case "~":
        return cb.like(path, value + "%");
      default:
        return cb.equal(path, value);
    }
  }

  @SuppressWarnings("unchecked")
  private static Predicate buildNumericPredicate(
      String key,
      String expectedType,
      CriteriaBuilder cb,
      Path<?> path,
      String value,
      String operator) {
    try {
      Class<?> javaType = path.getJavaType();

      if (javaType.equals(Double.class) || javaType.equals(double.class)) {
        Double numValue = Double.parseDouble(value);
        Path<Double> numPath = (Path<Double>) path;
        switch (operator) {
          case "==":
            return cb.equal(numPath, numValue);
          case "!=":
            return cb.notEqual(numPath, numValue);
          case ">":
            return cb.greaterThan(numPath, numValue);
          case ">=":
            return cb.greaterThanOrEqualTo(numPath, numValue);
          case "<":
            return cb.lessThan(numPath, numValue);
          case "<=":
            return cb.lessThanOrEqualTo(numPath, numValue);
          default:
            return cb.equal(numPath, numValue);
        }
      } else if (javaType.equals(Integer.class) || javaType.equals(int.class)) {
        Integer numValue = Integer.parseInt(value);
        Path<Integer> numPath = (Path<Integer>) path;
        switch (operator) {
          case "==":
            return cb.equal(numPath, numValue);
          case "!=":
            return cb.notEqual(numPath, numValue);
          case ">":
            return cb.greaterThan(numPath, numValue);
          case ">=":
            return cb.greaterThanOrEqualTo(numPath, numValue);
          case "<":
            return cb.lessThan(numPath, numValue);
          case "<=":
            return cb.lessThanOrEqualTo(numPath, numValue);
          default:
            return cb.equal(numPath, numValue);
        }
      } else if (javaType.equals(Long.class) || javaType.equals(long.class)) {
        Long numValue = Long.parseLong(value);
        Path<Long> numPath = (Path<Long>) path;
        switch (operator) {
          case "==":
            return cb.equal(numPath, numValue);
          case "!=":
            return cb.notEqual(numPath, numValue);
          case ">":
            return cb.greaterThan(numPath, numValue);
          case ">=":
            return cb.greaterThanOrEqualTo(numPath, numValue);
          case "<":
            return cb.lessThan(numPath, numValue);
          case "<=":
            return cb.lessThanOrEqualTo(numPath, numValue);
          default:
            return cb.equal(numPath, numValue);
        }
      }
    } catch (NumberFormatException e) {
      throw new BadRequestException(
          String.format(
              "Некорректные данные в URL запросе. Тип поля %s должен быть %s.", key, expectedType));
    }
    return null;
  }
}
