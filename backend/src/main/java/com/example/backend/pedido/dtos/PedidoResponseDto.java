package com.example.backend.pedido.dtos;

import com.example.backend.pedido.model.DetallePedido;
import com.example.backend.pedido.model.EstadoPedido;
import com.example.backend.pedido.model.Pedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDto(Long id, EstadoPedido estado, BigDecimal total, String direccionEntrega,
                                LocalDateTime createdAt, Long compradorId, String compradorNombre,
                                Long productorId, String nombreFinca, List<Detalle> detalles) {

    public record Detalle(Long productoId, String nombreProducto, String unidad, BigDecimal cantidad,
                          BigDecimal precioUnitario, BigDecimal subtotal) {

        static Detalle from(DetallePedido d) {
            return new Detalle(d.getProducto().getId(), d.getProducto().getNombre(), d.getProducto().getUnidad(),
                    d.getCantidad(), d.getPrecioUnitario(),
                    d.getPrecioUnitario().multiply(d.getCantidad()).setScale(2, RoundingMode.HALF_UP));
        }
    }

    public static PedidoResponseDto from(Pedido p) {
        return new PedidoResponseDto(p.getId(), p.getEstado(), p.getTotal(), p.getDireccionEntrega(),
                p.getCreatedAt(), p.getComprador().getId(), p.getComprador().getNombre(),
                p.getProductor().getId(), p.getProductor().getNombreFinca(),
                p.getDetalles().stream().map(Detalle::from).toList());
    }
}
