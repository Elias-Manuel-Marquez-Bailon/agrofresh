package com.example.backend.pedido.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Un renglón del carrito. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedidoDto {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "La cantidad admite máximo 2 decimales")
    private BigDecimal cantidad;
}
