package com.centropsicologico.sistema.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support
        .DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter
        .HttpMessageNotReadableException;
import org.springframework.web.bind
        .MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server
        .ResponseStatusException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(
            ResourceNotFoundException.class
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(
            BusinessRuleException.class
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBusinessRule(
            BusinessRuleException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    /*
     * Errores de anotaciones como:
     * @NotBlank, @Email, @Size, etc.
     */
    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message =
                exception
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(
                                DefaultMessageSourceResolvable
                                        ::getDefaultMessage
                        )
                        .orElse(
                                "Los datos enviados no son válidos"
                        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "El contenido de la solicitud no es válido",
                request
        );
    }

    /*
     * Conserva correctamente los estados utilizados
     * por ResponseStatusException.
     */
    @ExceptionHandler(
            ResponseStatusException.class
    )
    public Map<String, Object> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        int statusCode =
                exception
                        .getStatusCode()
                        .value();

        response.setStatus(statusCode);

        HttpStatus status =
                HttpStatus.resolve(statusCode);

        String reason =
                exception.getReason() != null
                        ? exception.getReason()
                        : "No se pudo completar la solicitud";

        return buildResponse(
                status != null
                        ? status
                        : HttpStatus.INTERNAL_SERVER_ERROR,
                reason,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(
            HttpStatus.INTERNAL_SERVER_ERROR
    )
    public Map<String, Object> handleGeneral(
            Exception exception,
            HttpServletRequest request
    ) {
        /*
         * El detalle completo queda en los logs,
         * pero no se muestra al usuario.
         */
        log.error(
                "Error no controlado en {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. "
                        + "Inténtalo nuevamente más tarde.",
                request
        );
    }

    private Map<String, Object> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                LocalDateTime.now()
        );

        response.put(
                "status",
                status.value()
        );

        response.put(
                "error",
                status.getReasonPhrase()
        );

        response.put(
                "message",
                message
        );

        response.put(
                "path",
                request != null
                        ? request.getRequestURI()
                        : ""
        );

        return response;
    }
}