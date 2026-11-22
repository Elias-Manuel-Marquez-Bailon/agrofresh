package com.example.backend.pedido.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Lo que envía el comprador al confirmar el carrito. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPedidoDto {

    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Size(max = 255, message = "La dirección no puede tener más de 255 caracteres")
    private String direccionEntrega;

    @NotEmpty(message = "El carrito está vacío")
    @Size(max = 50, message = "El carrito admite máximo 50 productos")
    @Valid
    private List<ItemPedidoDto> items;
}
