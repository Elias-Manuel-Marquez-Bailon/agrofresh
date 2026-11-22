package com.example.backend.lote.controller;

import com.example.backend.kernel.ApiResponse;
import com.example.backend.lote.dtos.LoteDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lotes")
@AllArgsConstructor
public class LoteController {

    private final LoteService loteService;

    /** Público: últimos lotes cosechados de un producto. */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse> origenDelProducto(@PathVariable Long productoId) {
        return loteService.origenDelProducto(productoId);
    }

    /** Ejemplo: /api/lotes/mios?productoId=5 (productoId opcional) */
    @GetMapping("/mios")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> misLotes(@RequestParam(required = false) Long productoId, Authentication auth) {
        return loteService.misLotes(auth.getName(), productoId);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> crear(@Valid @RequestBody LoteDto dto, Authentication auth) {
        return loteService.crear(auth.getName(), dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> actualizar(@PathVariable Long id, @Valid @RequestBody LoteDto dto,
                                                  Authentication auth) {
        return loteService.actualizar(auth.getName(), id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> eliminar(@PathVariable Long id, Authentication auth) {
        return loteService.eliminar(auth.getName(), id);
    }
}
