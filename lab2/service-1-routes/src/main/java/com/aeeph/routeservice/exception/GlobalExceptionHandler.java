package com.aeeph.routeservice.exception;

import com.aeeph.routeservice.model.ErrorResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity<>(errorResponse, headers, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = "Некорректные данные в теле запроса.";
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String fieldPath = ife.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .collect(Collectors.joining("."));

            String expectedType = ife.getTargetType().getSimpleName().toLowerCase();
            if (ife.getTargetType().isEnum()) {
                message = "Некорректные данные в теле запроса. Допустимые значения поля priority: LOW, MEDIUM, HIGH.";
            } else if (expectedType.equals("double") || expectedType.equals("integer")) {
                message = String.format("Некорректные данные в теле запроса. Тип поля %s должен быть %s.", fieldPath, expectedType);
            } else {
                message = String.format("Некорректные данные в теле запроса для поля %s.", fieldPath);
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), message);
        headers.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity<>(errorResponse, headers, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    String fieldPath = error.getField()
                            .replace("fromLocation", "from")
                            .replace("toLocation", "to");

                    String simpleFieldName;
                    int lastDot = error.getField().lastIndexOf('.');
                    if (lastDot > -1) {
                        simpleFieldName = error.getField().substring(lastDot + 1);
                    } else {
                        simpleFieldName = error.getField();
                    }
                    
                    return error.getDefaultMessage().replace(" " + simpleFieldName, " " + fieldPath);
                })
                .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), message);
        headers.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity<>(errorResponse, headers, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), message);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String param = ex.getName();
        Class<?> required = ex.getRequiredType();
        String typeName = "";
        if (required != null) {
            if (Number.class.isAssignableFrom(required) || required == Long.class || required == Integer.class) {
                typeName = "integer";
            } else if (required == Double.class || required == Float.class) {
                typeName = "double";
            } else if (required == Boolean.class) {
                typeName = "boolean";
            } else {
                typeName = required.getSimpleName();
            }
        }
        String message = String.format("Некорректные данные в URL запросе. Тип поля %s должен быть %s.", param, typeName);
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        logger.error("An unexpected error occurred: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Внутренняя ошибка сервера");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
