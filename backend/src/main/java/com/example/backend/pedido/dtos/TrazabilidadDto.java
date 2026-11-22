package com.example.backend.pedido.dtos;

import com.example.backend.lote.dtos.LoteResponseDto;
import com.example.backend.pedido.model.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Recorrido de cada producto del pedido: siembra -> cosecha (lote) -> pedido -> entrega. */
public record TrazabilidadDto(Long pedidoId, EstadoPedido estado, String nombreFinca, String ubicacion,
                              LocalDateTime fechaPedido, LocalDateTime fechaEntrega, List<Producto> productos) {

    /** lote es null si el productor no indicó de qué lote salió. */
    public record Producto(Long productoId, String nombreProducto, BigDecimal cantidad, String unidad,
                           LoteResponseDto lote) {
    }
}
