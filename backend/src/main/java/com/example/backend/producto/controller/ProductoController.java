package com.example.backend.producto.controller;

import com.example.backend.kernel.ApiResponse;
import com.example.backend.producto.dtos.ProductoDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/productos")
@AllArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // ---------- Público (catálogo) ----------

    /** Ejemplo: /api/productos?nombre=tomate&categoriaId=2&pagina=0&tamanio=12 */
    @GetMapping
    public ResponseEntity<ApiResponse> catalogo(@RequestParam(required = false) String nombre,
                                                @RequestParam(required = false) Long categoriaId,
                                                @RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "12") int tamanio) {
        return productoService.catalogo(nombre, categoriaId, pagina, tamanio);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse> detalle(@PathVariable Long id) {
        return productoService.detalle(id);
    }

    @GetMapping("/{id:\\d+}/imagen")
    public ResponseEntity<byte[]> imagen(@PathVariable Long id) {
        return productoService.imagen(id);
    }

    // ---------- Productor ----------

    @GetMapping("/mios")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> misProductos(Authentication auth) {
        return productoService.misProductos(auth.getName());
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> crear(@Valid @RequestBody ProductoDto dto, Authentication auth) {
        return productoService.crear(auth.getName(), dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDto dto,
                                                  Authentication auth) {
        return productoService.actualizar(auth.getName(), id, dto);
    }

    /** Activa o desactiva: /api/productos/5/estado?activo=false */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
                                                     Authentication auth) {
        return productoService.cambiarEstado(auth.getName(), id, activo);
    }

    @PutMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> subirImagen(@PathVariable Long id,
                                                   @RequestParam("archivo") MultipartFile archivo,
                                                   Authentication auth) throws IOException {
        return productoService.subirImagen(auth.getName(), id, archivo);
    }

    @DeleteMapping("/{id}/imagen")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> eliminarImagen(@PathVariable Long id, Authentication auth) {
        return productoService.eliminarImagen(auth.getName(), id);
    }
}
