package com.example.backend.categoria.controller;

import com.example.backend.categoria.dtos.CategoriaDto;
import com.example.backend.kernel.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
@AllArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /** Público: lo usa el catálogo para el filtro. */
    @GetMapping
    public ResponseEntity<ApiResponse> listar() {
        return categoriaService.listar();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> crear(@Valid @RequestBody CategoriaDto dto) {
        return categoriaService.crear(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDto dto) {
        return categoriaService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> eliminar(@PathVariable Long id) {
        return categoriaService.eliminar(id);
    }
}
