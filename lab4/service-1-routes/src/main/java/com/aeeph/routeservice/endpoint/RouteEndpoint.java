package com.aeeph.routeservice.endpoint;

import com.aeeph.routeservice.exception.BadRequestException;
import com.aeeph.routeservice.exception.ResourceNotFoundException;
import com.aeeph.routeservice.exception.ValidationFormatException;
import com.aeeph.routeservice.model.Route;
import com.aeeph.routeservice.service.RouteService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

@Endpoint
public class RouteEndpoint {

  private static final String NAMESPACE_URI = "http://aeeph.com/routeservice";

  private final RouteService routeService;
  private final Validator validator;

  @Autowired
  public RouteEndpoint(RouteService routeService, Validator validator) {
    this.routeService = routeService;
    this.validator = validator;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetRoutesRequest")
  @ResponsePayload
  public Element getRoutes(@RequestPayload Element request) throws Exception {
    String sort = getElementTextContent(request, "sort");
    int page = getElementIntValue(request, "page", 1);
    int pageSize = getElementIntValue(request, "pageSize", 10);

    Map<String, String> filters = extractMapFromElement(request, "filters", "filter");
    Map<String, String> operations = extractMapFromElement(request, "operations", "operation");
    operations = mapSoapOperationsToInternal(operations);

    List<Sort.Order> orders = new ArrayList<>();
    if (sort != null && !sort.isEmpty()) {
      String[] sortFields = sort.split(",");
      for (String sortOrder : sortFields) {
        String[] _sort = sortOrder.trim().split("_");
        if (_sort.length > 0) {
          String fieldName = _sort[0];
          Sort.Direction direction = Sort.Direction.ASC;
          if (_sort.length > 1 && _sort[1].equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
          }
          orders.add(new Sort.Order(direction, fieldName));
        }
      }
    }

    Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(orders));
    Page<Route> routePage = routeService.getAllRoutes(filters, operations, pageable);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    Element response = doc.createElementNS(NAMESPACE_URI, "GetRoutesResponse");
    Element totalCount = doc.createElementNS(NAMESPACE_URI, "totalCount");
    totalCount.setTextContent(String.valueOf(routePage.getTotalElements()));
    response.appendChild(totalCount);

    Element routes = doc.createElementNS(NAMESPACE_URI, "routes");
    for (Route route : routePage.getContent()) {
      routes.appendChild(routeToElement(doc, route));
    }
    response.appendChild(routes);

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateRouteRequest")
  @ResponsePayload
  public Element createRoute(@RequestPayload Element request) throws Exception {
    Route route = elementToRoute(request);
    validateRoute(route);
    Route createdRoute = routeService.createRoute(route);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    Element response = doc.createElementNS(NAMESPACE_URI, "CreateRouteResponse");
    response.appendChild(routeToElement(doc, createdRoute));

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetRouteByIdRequest")
  @ResponsePayload
  public Element getRouteById(@RequestPayload Element request) throws Exception {
    long id = parseId(request, "id");
    Route route =
        routeService
            .getRouteById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Маршрут с указанным ID не найден"));

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    Element response = doc.createElementNS(NAMESPACE_URI, "GetRouteByIdResponse");
    response.appendChild(routeToElement(doc, route));

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "UpdateRouteRequest")
  @ResponsePayload
  public Element updateRoute(@RequestPayload Element request) throws Exception {
    long id = parseId(request, "id");
    Route routeDetails = elementToRoute(request);
    validateRoute(routeDetails);
    Route updatedRoute = routeService.updateRoute(id, routeDetails);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    Element response = doc.createElementNS(NAMESPACE_URI, "UpdateRouteResponse");
    response.appendChild(routeToElement(doc, updatedRoute));

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteRouteRequest")
  @ResponsePayload
  public Element deleteRoute(@RequestPayload Element request) throws Exception {
    long id = parseId(request, "id");
    routeService.deleteRoute(id);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    Element response = doc.createElementNS(NAMESPACE_URI, "DeleteRouteResponse");
    Element success = doc.createElementNS(NAMESPACE_URI, "success");
    success.setTextContent("true");
    response.appendChild(success);

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetMaxByFromRequest")
  @ResponsePayload
  public Element getMaxByFrom(@RequestPayload Element request) throws Exception {
    Route route =
        routeService
            .getMaxByFrom()
            .orElseThrow(() -> new ResourceNotFoundException("Маршрут с указанным ID не найден"));

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    Element response = doc.createElementNS(NAMESPACE_URI, "GetMaxByFromResponse");
    response.appendChild(routeToElement(doc, route));

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetRoutesByNamePrefixRequest")
  @ResponsePayload
  public Element getRoutesByNamePrefix(@RequestPayload Element request) throws Exception {
    String substring = getElementTextContent(request, "substring");
    List<Route> routes = routeService.findRoutesByNameStartingWith(substring);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    Element response = doc.createElementNS(NAMESPACE_URI, "GetRoutesByNamePrefixResponse");
    Element routesElement = doc.createElementNS(NAMESPACE_URI, "routes");
    for (Route route : routes) {
      routesElement.appendChild(routeToElement(doc, route));
    }
    response.appendChild(routesElement);

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetUniqueDistancesRequest")
  @ResponsePayload
  public Element getUniqueDistances(@RequestPayload Element request) throws Exception {
    List<Integer> distances = routeService.getUniqueDistances();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    Element response = doc.createElementNS(NAMESPACE_URI, "GetUniqueDistancesResponse");
    Element distancesElement = doc.createElementNS(NAMESPACE_URI, "distances");
    for (Integer distance : distances) {
      Element distanceElement = doc.createElementNS(NAMESPACE_URI, "distance");
      distanceElement.setTextContent(String.valueOf(distance));
      distancesElement.appendChild(distanceElement);
    }
    response.appendChild(distancesElement);

    return response;
  }

  private void validateRoute(Route route) {
    Set<ConstraintViolation<Route>> violations = validator.validate(route);
    if (!violations.isEmpty()) {
      String message =
          violations.stream()
              .map(
                  violation -> {
                    String originalPath = violation.getPropertyPath().toString();
                    String fieldPath =
                        originalPath.replace("fromLocation", "from").replace("toLocation", "to");

                    String simpleFieldName;
                    int lastDot = originalPath.lastIndexOf('.');
                    if (lastDot > -1) {
                      simpleFieldName = originalPath.substring(lastDot + 1);
                    } else {
                      simpleFieldName = originalPath;
                    }

                    String errorMessage = violation.getMessage();
                    if (errorMessage.contains("поле " + simpleFieldName)) {
                      errorMessage = errorMessage.replace("поле " + simpleFieldName, "поле " + fieldPath);
                    } else if (errorMessage.contains(" " + simpleFieldName)) {
                      errorMessage = errorMessage.replace(" " + simpleFieldName, " " + fieldPath);
                    }
                    return errorMessage;
                  })
              .collect(Collectors.joining(", "));
      throw new ConstraintViolationException(message, violations);
    }
  }

  private String getElementTextContent(Element parent, String tagName) {
    NodeList nodeList = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent();
    }
    return null;
  }

  private String getDirectChildTextContent(Element parent, String tagName) {
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      org.w3c.dom.Node node = children.item(i);
      if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
        Element element = (Element) node;
        if (NAMESPACE_URI.equals(element.getNamespaceURI())
            && tagName.equals(element.getLocalName())) {
          return element.getTextContent();
        }
      }
    }
    return null;
  }

  private long parseId(Element request, String fieldName) {
    String idStr = getDirectChildTextContent(request, fieldName);
    if (idStr == null || idStr.trim().isEmpty()) {
      throw new ValidationFormatException(
          "Некорректные данные в запросе: Не заполнено поле " + fieldName);
    }
    try {
      return Long.parseLong(idStr.trim());
    } catch (NumberFormatException e) {
      throw new ValidationFormatException(
          "Некорректные данные в запросе: Тип поля " + fieldName + " должен быть integer.");
    }
  }

  private int getElementIntValue(Element parent, String tagName, int defaultValue) {
    String value = getElementTextContent(parent, tagName);
    if (value != null && !value.isEmpty()) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  private Map<String, String> extractMapFromElement(
      Element parent, String containerName, String itemName) {
    Map<String, String> map = new HashMap<>();
    NodeList containerList = parent.getElementsByTagNameNS(NAMESPACE_URI, containerName);
    if (containerList.getLength() > 0) {
      Element container = (Element) containerList.item(0);
      NodeList items = container.getElementsByTagNameNS(NAMESPACE_URI, itemName);
      for (int i = 0; i < items.getLength(); i++) {
        Element item = (Element) items.item(i);
        String key = item.getAttribute("key");
        String value = item.getTextContent();
        if (key != null && !key.isEmpty()) {
          map.put(key, value);
        }
      }
    }
    return map;
  }

  private Map<String, String> mapSoapOperationsToInternal(Map<String, String> operations) {
    if (operations == null || operations.isEmpty()) {
      return operations;
    }
    Map<String, String> mappedOperations = new HashMap<>();
    for (Map.Entry<String, String> entry : operations.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (value != null && !value.trim().isEmpty()) {
        String mappedValue = mapSoapOperationToInternal(value.trim().toLowerCase());
        String mappedKey = key + "_op";
        mappedOperations.put(mappedKey, mappedValue);
      }
    }
    return mappedOperations;
  }

  private String mapSoapOperationToInternal(String soapOperation) {
    if (soapOperation == null || soapOperation.isEmpty()) {
      return "==";
    }
    switch (soapOperation.toLowerCase()) {
      case "eq":
        return "==";
      case "ne":
        return "!=";
      case "gt":
        return ">";
      case "ge":
        return ">=";
      case "lt":
        return "<";
      case "le":
        return "<=";
      case "~":
        return "~";
      default:
        return soapOperation;
    }
  }

  private Route elementToRoute(Element element) {
    Route route = new Route();

    String name = getDirectChildTextContent(element, "name");
    if (name != null) {
      route.setName(name.trim());
    }

    NodeList coordsList = element.getElementsByTagNameNS(NAMESPACE_URI, "coordinates");
    if (coordsList.getLength() > 0) {
      Element coordsElement = (Element) coordsList.item(0);
      com.aeeph.routeservice.model.Coordinates coords =
          new com.aeeph.routeservice.model.Coordinates();
      String x = getElementTextContent(coordsElement, "x");
      if (x != null && !x.isEmpty()) {
        try {
          coords.setX(new java.math.BigDecimal(x));
        } catch (NumberFormatException e) {
          throw new ValidationFormatException(
              "Некорректные данные в теле запроса. Тип поля coordinates.x должен быть decimal.");
        }
      }
      String y = getElementTextContent(coordsElement, "y");
      if (y != null && !y.isEmpty()) {
        try {
          coords.setY(Integer.parseInt(y));
        } catch (NumberFormatException e) {
          throw new ValidationFormatException(
              "Некорректные данные в теле запроса. Тип поля coordinates.y должен быть integer.");
        }
      }
      route.setCoordinates(coords);
    }

    NodeList fromList = element.getElementsByTagNameNS(NAMESPACE_URI, "from");
    if (fromList.getLength() > 0) {
      Element fromElement = (Element) fromList.item(0);
      route.setFromLocation(parseLocation(fromElement));
    }

    NodeList toList = element.getElementsByTagNameNS(NAMESPACE_URI, "to");
    if (toList.getLength() > 0) {
      Element toElement = (Element) toList.item(0);
      route.setToLocation(parseLocation(toElement));
    }

    String distance = getDirectChildTextContent(element, "distance");
    if (distance != null && !distance.isEmpty()) {
      try {
        route.setDistance(Integer.parseInt(distance));
      } catch (NumberFormatException e) {
        throw new ValidationFormatException(
            "Некорректные данные в теле запроса. Тип поля distance должен быть integer.");
      }
    }

    String priority = getDirectChildTextContent(element, "priority");
    if (priority != null && !priority.isEmpty()) {
      try {
        route.setPriority(com.aeeph.routeservice.model.Priority.valueOf(priority));
      } catch (IllegalArgumentException e) {
        throw new ValidationFormatException(
            "Некорректные данные в теле запроса. Допустимые значения поля priority: LOW, MEDIUM, HIGH.");
      }
    }

    return route;
  }

  private com.aeeph.routeservice.model.Location parseLocation(Element locationElement) {
    com.aeeph.routeservice.model.Location location =
        new com.aeeph.routeservice.model.Location();
    String x = getElementTextContent(locationElement, "x");
    if (x != null && !x.isEmpty()) {
      try {
        location.setX(Double.parseDouble(x));
      } catch (NumberFormatException e) {
        throw new ValidationFormatException(
            "Некорректные данные в теле запроса. Тип поля x должен быть double.");
      }
    }
    String y = getElementTextContent(locationElement, "y");
    if (y != null && !y.isEmpty()) {
      try {
        location.setY(Double.parseDouble(y));
      } catch (NumberFormatException e) {
        throw new ValidationFormatException(
            "Некорректные данные в теле запроса. Тип поля y должен быть double.");
      }
    }
    String name = getElementTextContent(locationElement, "name");
    if (name != null) {
      location.setName(name.trim());
    }
    return location;
  }

  private Element routeToElement(Document doc, Route route) {
    Element routeElement = doc.createElementNS(NAMESPACE_URI, "route");

    Element id = doc.createElementNS(NAMESPACE_URI, "id");
    id.setTextContent(String.valueOf(route.getId()));
    routeElement.appendChild(id);

    Element name = doc.createElementNS(NAMESPACE_URI, "name");
    name.setTextContent(route.getName());
    routeElement.appendChild(name);

    if (route.getCoordinates() != null) {
      Element coords = doc.createElementNS(NAMESPACE_URI, "coordinates");
      if (route.getCoordinates().getX() != null) {
        Element x = doc.createElementNS(NAMESPACE_URI, "x");
        x.setTextContent(route.getCoordinates().getX().toString());
        coords.appendChild(x);
      }
      if (route.getCoordinates().getY() != null) {
        Element y = doc.createElementNS(NAMESPACE_URI, "y");
        y.setTextContent(String.valueOf(route.getCoordinates().getY()));
        coords.appendChild(y);
      }
      routeElement.appendChild(coords);
    }

    if (route.getCreationDate() != null) {
      Element creationDate = doc.createElementNS(NAMESPACE_URI, "creationDate");
      creationDate.setTextContent(
          new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
              .format(route.getCreationDate()));
      routeElement.appendChild(creationDate);
    }

    if (route.getFromLocation() != null) {
      routeElement.appendChild(locationToElement(doc, "from", route.getFromLocation()));
    }

    if (route.getToLocation() != null) {
      routeElement.appendChild(locationToElement(doc, "to", route.getToLocation()));
    }

    if (route.getDistance() != null) {
      Element distance = doc.createElementNS(NAMESPACE_URI, "distance");
      distance.setTextContent(String.valueOf(route.getDistance()));
      routeElement.appendChild(distance);
    }

    if (route.getPriority() != null) {
      Element priority = doc.createElementNS(NAMESPACE_URI, "priority");
      priority.setTextContent(route.getPriority().toString());
      routeElement.appendChild(priority);
    }

    return routeElement;
  }

  private Element locationToElement(
      Document doc, String elementName, com.aeeph.routeservice.model.Location location) {
    Element locationElement = doc.createElementNS(NAMESPACE_URI, elementName);

    if (location.getX() != null) {
      Element x = doc.createElementNS(NAMESPACE_URI, "x");
      x.setTextContent(String.valueOf(location.getX()));
      locationElement.appendChild(x);
    }

    Element y = doc.createElementNS(NAMESPACE_URI, "y");
    y.setTextContent(String.valueOf(location.getY()));
    locationElement.appendChild(y);

    if (location.getName() != null) {
      Element name = doc.createElementNS(NAMESPACE_URI, "name");
      name.setTextContent(location.getName());
      locationElement.appendChild(name);
    }

    return locationElement;
  }
}

