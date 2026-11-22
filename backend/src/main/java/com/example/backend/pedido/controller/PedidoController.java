package com.example.backend.pedido.controller;

import com.example.backend.kernel.ApiResponse;
import com.example.backend.pedido.dtos.CambiarEstadoDto;
import com.example.backend.pedido.dtos.CrearPedidoDto;
import com.example.backend.pedido.model.EstadoPedido;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@AllArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    /** Confirma el carrito. Devuelve la lista de pedidos creados (uno por productor). */
    @PostMapping
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<ApiResponse> crear(@Valid @RequestBody CrearPedidoDto dto, Authentication auth) {
        return pedidoService.crear(auth.getName(), dto);
    }

    /** Ejemplo: /api/pedidos/mis-pedidos?estado=PENDIENTE&pagina=0&tamanio=10 */
    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<ApiResponse> misPedidos(@RequestParam(required = false) EstadoPedido estado,
                                                  @RequestParam(defaultValue = "0") int pagina,
                                                  @RequestParam(defaultValue = "10") int tamanio,
                                                  Authentication auth) {
        return pedidoService.misPedidos(auth.getName(), estado, pagina, tamanio);
    }

    @GetMapping("/recibidos")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> recibidos(@RequestParam(required = false) EstadoPedido estado,
                                                 @RequestParam(defaultValue = "0") int pagina,
                                                 @RequestParam(defaultValue = "10") int tamanio,
                                                 Authentication auth) {
        return pedidoService.recibidos(auth.getName(), estado, pagina, tamanio);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> todos(@RequestParam(required = false) EstadoPedido estado,
                                             @RequestParam(defaultValue = "0") int pagina,
                                             @RequestParam(defaultValue = "10") int tamanio) {
        return pedidoService.todos(estado, pagina, tamanio);
    }

    /** Lo ven el comprador, el productor del pedido o el ADMIN. */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse> detalle(@PathVariable Long id, Authentication auth) {
        return pedidoService.detalle(auth.getName(), id);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('COMPRADOR','PRODUCTOR')")
    public ResponseEntity<ApiResponse> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoDto dto,
                                                     Authentication auth) {
        return pedidoService.cambiarEstado(auth.getName(), id, dto.getEstado(), dto.getLotes());
    }

    @GetMapping("/{id:\\d+}/trazabilidad")
    public ResponseEntity<ApiResponse> trazabilidad(@PathVariable Long id, Authentication auth) {
        return pedidoService.trazabilidad(auth.getName(), id);
    }
}
