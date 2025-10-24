package com.aeeph.routeservice.exception;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.w3c.dom.Element;

import java.util.stream.Collectors;

public class SoapExceptionHandler extends SoapFaultMappingExceptionResolver {

  private static final Logger logger = LoggerFactory.getLogger(SoapExceptionHandler.class);

  private static final String NAMESPACE_URI = "http://aeeph.com/routeservice";

  @Override
  protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
    logger.error("Exception occurred: ", ex);

    if (ex instanceof ResourceNotFoundException) {
      addErrorDetail(fault, 404, ex.getMessage());
    } else if (ex instanceof BadRequestException) {
      addErrorDetail(fault, 400, ex.getMessage());
    } else if (ex instanceof ConstraintViolationException) {
      ConstraintViolationException cve = (ConstraintViolationException) ex;
      String message =
          cve.getConstraintViolations().stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining(", "));
      addErrorDetail(fault, 422, message);
    } else if (ex instanceof IllegalArgumentException) {
      String message = "Некорректные данные в запросе: " + ex.getMessage();
      addErrorDetail(fault, 400, message);
    } else if (ex instanceof NumberFormatException) {
      addErrorDetail(fault, 400, "Некорректный формат числа в запросе");
    } else {
      addErrorDetail(fault, 500, "Внутренняя ошибка сервера");
    }
  }

  private void addErrorDetail(SoapFault fault, int code, String message) {
    SoapFaultDetail detail = fault.addFaultDetail();
    
    SoapFaultDetailElement codeElement =
        detail.addFaultDetailElement(new QName(NAMESPACE_URI, "status"));
    Result codeResult = codeElement.getResult();
    if (codeResult instanceof DOMResult) {
      Element codeEl = (Element) ((DOMResult) codeResult).getNode();
      if (codeEl != null) {
        codeEl.setTextContent(String.valueOf(code));
      }
    }

    SoapFaultDetailElement messageElement =
        detail.addFaultDetailElement(new QName(NAMESPACE_URI, "message"));
    Result messageResult = messageElement.getResult();
    if (messageResult instanceof DOMResult) {
      Element messageEl = (Element) ((DOMResult) messageResult).getNode();
      if (messageEl != null) {
        messageEl.setTextContent(message != null ? message : "");
      }
    }
  }
}

