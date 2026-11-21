package com.example.backend.kernel;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Respuesta estándar de toda la API: { message, data, error, status }.
 */
@Data
@NoArgsConstructor
public class ApiResponse {
    private String message;
    private Object data;
    private boolean error;
    private HttpStatus status;

    public ApiResponse(String message, Object data, boolean error, HttpStatus status) {
        this.message = message;
        this.data = data;
        this.error = error;
        this.status = status;
    }

    public ApiResponse(String message, boolean error, HttpStatus status) {
        this.message = message;
        this.error = error;
        this.status = status;
    }
}
