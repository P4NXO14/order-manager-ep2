package com.example.ordermanager.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void debeManejarOrdenNoEncontrada() {
        OrderNotFoundException ex = new OrderNotFoundException(99L);

        ResponseEntity<Map<String, Object>> respuesta = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(404, respuesta.getBody().get("status"));
        assertEquals(ex.getMessage(), respuesta.getBody().get("message"));
    }

    @Test
    void debeManejarRecursoNoEncontrado() {
        NoResourceFoundException ex = new NoResourceFoundException(
                HttpMethod.GET, "ruta-inexistente");

        ResponseEntity<Map<String, Object>> respuesta = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(404, respuesta.getBody().get("status"));
        assertEquals("Recurso no encontrado.", respuesta.getBody().get("message"));
    }

    @Test
    void debeManejarEstadoInvalido() {
        InvalidOrderStateException ex = new InvalidOrderStateException(
                "Estado invalido");

        ResponseEntity<Map<String, Object>> respuesta = handler.handleInvalidState(ex);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(409, respuesta.getBody().get("status"));
        assertEquals(ex.getMessage(), respuesta.getBody().get("message"));
    }

    @Test
    void debeManejarSolicitudIncorrecta() {
        IllegalArgumentException ex = new IllegalArgumentException(
                "Datos incorrectos");

        ResponseEntity<Map<String, Object>> respuesta = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(400, respuesta.getBody().get("status"));
        assertEquals(ex.getMessage(), respuesta.getBody().get("message"));
    }

    @Test
    void debeManejarErrorDeValidacion() {
        BeanPropertyBindingResult errores = new BeanPropertyBindingResult(
                new Object(), "order");

        errores.addError(new FieldError(
                "order", "customerName", "Campo obligatorio"));

        MethodArgumentNotValidException ex = mock(
                MethodArgumentNotValidException.class);

        when(ex.getBindingResult()).thenReturn(errores);

        ResponseEntity<Map<String, Object>> respuesta = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(400, respuesta.getBody().get("status"));
        assertEquals(Map.of("customerName", "Campo obligatorio"),
                respuesta.getBody().get("fields"));
    }

    @Test
    void debeManejarErrorInterno() {
        RuntimeException ex = new RuntimeException("Error de prueba");

        ResponseEntity<Map<String, Object>> respuesta = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(500, respuesta.getBody().get("status"));
        assertEquals("Error interno del servidor. Contacte al administrador.",
                respuesta.getBody().get("message"));
    }
}