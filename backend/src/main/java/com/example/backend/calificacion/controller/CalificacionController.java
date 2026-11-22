package com.example.backend.calificacion.controller;

import com.example.backend.calificacion.dtos.CalificacionDto;
import com.example.backend.kernel.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calificaciones")
@AllArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;

    @PostMapping
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<ApiResponse> calificar(@Valid @RequestBody CalificacionDto dto, Authentication auth) {
        return calificacionService.calificar(auth.getName(), dto);
    }

    /** Público. Ejemplo: /api/calificaciones/productor/3?pagina=0&tamanio=5 */
    @GetMapping("/productor/{productorId}")
    public ResponseEntity<ApiResponse> deProductor(@PathVariable Long productorId,
                                                   @RequestParam(defaultValue = "0") int pagina,
                                                   @RequestParam(defaultValue = "5") int tamanio) {
        return calificacionService.deProductor(productorId, pagina, tamanio);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> mias(@RequestParam(defaultValue = "0") int pagina,
                                            @RequestParam(defaultValue = "5") int tamanio,
                                            Authentication auth) {
        return calificacionService.mias(auth.getName(), pagina, tamanio);
    }
}
