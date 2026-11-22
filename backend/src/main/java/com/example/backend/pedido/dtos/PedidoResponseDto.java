package com.example.backend.pedido.dtos;

import com.example.backend.calificacion.model.Calificacion;
import com.example.backend.pedido.model.DetallePedido;
import com.example.backend.pedido.model.EstadoPedido;
import com.example.backend.pedido.model.Pedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/** calificacion es null si el pedido aún no se califica. */
public record PedidoResponseDto(Long id, EstadoPedido estado, BigDecimal total, String direccionEntrega,
                                LocalDateTime createdAt, LocalDateTime fechaEntrega, Long compradorId,
                                String compradorNombre, Long productorId, String nombreFinca,
                                List<Detalle> detalles, CalificacionResumen calificacion) {

    public record Detalle(Long productoId, String nombreProducto, String unidad, BigDecimal cantidad,
                          BigDecimal precioUnitario, BigDecimal subtotal, Long loteId) {

        static Detalle from(DetallePedido d) {
            return new Detalle(d.getProducto().getId(), d.getProducto().getNombre(), d.getProducto().getUnidad(),
                    d.getCantidad(), d.getPrecioUnitario(),
                    d.getPrecioUnitario().multiply(d.getCantidad()).setScale(2, RoundingMode.HALF_UP),
                    d.getLoteId());
        }
    }

    public record CalificacionResumen(Integer puntuacion, String comentario) {
    }

    public static PedidoResponseDto from(Pedido p) {
        return from(p, null);
    }

    public static PedidoResponseDto from(Pedido p, Calificacion c) {
        return new PedidoResponseDto(p.getId(), p.getEstado(), p.getTotal(), p.getDireccionEntrega(),
                p.getCreatedAt(), p.getFechaEntrega(), p.getComprador().getId(), p.getComprador().getNombre(),
                p.getProductor().getId(), p.getProductor().getNombreFinca(),
                p.getDetalles().stream().map(Detalle::from).toList(),
                c == null ? null : new CalificacionResumen(c.getPuntuacion(), c.getComentario()));
    }
}
