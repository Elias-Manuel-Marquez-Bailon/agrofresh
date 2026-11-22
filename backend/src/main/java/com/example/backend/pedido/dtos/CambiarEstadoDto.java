package com.example.backend.pedido.dtos;

import com.example.backend.pedido.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoDto {

    @NotNull(message = "El estado es obligatorio")
    private EstadoPedido estado;
}
