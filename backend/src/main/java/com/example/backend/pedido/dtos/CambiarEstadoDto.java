package com.example.backend.pedido.dtos;

import com.example.backend.pedido.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoDto {

    @NotNull(message = "El estado es obligatorio")
    private EstadoPedido estado;

    /**
     * Opcional, solo al ACEPTAR: { productoId: loteId } para indicar de qué lote sale cada producto.
     * Los productos que no aparezcan quedan sin lote.
     */
    private Map<Long, Long> lotes;

    public CambiarEstadoDto(EstadoPedido estado) {
        this.estado = estado;
    }
}
