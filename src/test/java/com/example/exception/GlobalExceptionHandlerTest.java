package com.example.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;
    
    @Mock
    private WebRequest webRequest;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        when(webRequest.getDescription(false)).thenReturn("uri=/api/bulbs/1");
    }
    
    @Test
    @DisplayName("Verifies handleResourceNotFoundException returns a not found response")
    void handleResourceNotFoundException_shouldReturnNotFoundResponse() {
        // Arrange
        String resourceName = "LightBulb";
        String fieldName = "id";
        Long fieldValue = 1L;
        ResourceNotFoundException ex = new ResourceNotFoundException(resourceName, fieldName, fieldValue);
        
        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResourceNotFound(ex, webRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        
        assertEquals(HttpStatus.NOT_FOUND.value(), body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertTrue(((String)body.get("message")).contains(resourceName));
        assertTrue(((String)body.get("message")).contains(fieldName));
        assertTrue(((String)body.get("message")).contains(fieldValue.toString()));
        assertNotNull(body.get("timestamp"));
        assertNotNull(body.get("path"));
    }
    
    @Test
    @DisplayName("Verifies handleMethodArgumentNotValid returns a bad request response with validation errors")
    void handleMethodArgumentNotValid_shouldReturnBadRequestWithValidationErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "default message");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
        
        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(ex, webRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Validation failed", body.get("message"));
        assertNotNull(body.get("validationErrors"));
        assertNotNull(body.get("timestamp"));
        assertNotNull(body.get("path"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("validationErrors");
        assertFalse(errors.isEmpty());
        assertEquals("default message", errors.get("fieldName"));
    }
    
    @Test
    @DisplayName("Verifies handleIllegalArgumentException returns a bad request response")
    void handleIllegalArgumentException_shouldReturnBadRequest() {
        // Arrange
        String errorMessage = "Invalid argument";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);
        
        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(ex, webRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals(errorMessage, body.get("message"));
        assertNotNull(body.get("timestamp"));
        assertNotNull(body.get("path"));
    }

    @Test
    @DisplayName("Verifies handleMethodArgumentTypeMismatch returns a bad request response")
    void handleMethodArgumentTypeMismatch_shouldReturnBadRequest() {
        // Arrange
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
            "abc", Long.class, "id", null, null);
        
        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMethodArgumentTypeMismatch(ex, webRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertTrue(((String) body.get("message")).contains("Invalid value 'abc' for parameter 'id'"));
        assertNotNull(body.get("timestamp"));
        assertEquals("/api/bulbs/1", body.get("path"));
    }
    
    @Test
    @DisplayName("Verifies handleAllExceptions returns an internal server error response")
    void handleAllExceptions_shouldReturnInternalServerError() {
        // Arrange
        RuntimeException ex = new RuntimeException("Unexpected error");
        
        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGlobalException(ex, webRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertTrue(((String)body.get("message")).contains("An unexpected error occurred"));
        assertNotNull(body.get("timestamp"));
        assertNotNull(body.get("path"));
    }
    

    @Test
    @DisplayName("Verifies handleMethodArgumentTypeMismatch returns a bad request response")
    void handleMethodArgumentTypeMismatch_shouldFallbackToGeneralHandler() {
        // Arrange
        String errorMessage = "Failed to convert value of type 'String' to required type 'Long'"
            + " for parameter 'id'; For input string: \"abc\"";
        
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getMessage()).thenReturn(errorMessage);
        
        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGlobalException(ex, webRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertTrue(((String) body.get("message")).contains("An unexpected error occurred"));
        assertNotNull(body.get("timestamp"));
        assertNotNull(body.get("path"));
    }
}