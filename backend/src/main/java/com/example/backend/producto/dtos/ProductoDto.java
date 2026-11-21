package com.example.backend.producto.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Datos que envía el productor para crear o editar un producto. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede tener más de 150 caracteres")
    private String nombre;

    @Size(max = 2000, message = "La descripción no puede tener más de 2000 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio admite máximo 2 decimales")
    private BigDecimal precio;

    @NotBlank(message = "La unidad es obligatoria")
    @Size(max = 20, message = "La unidad no puede tener más de 20 caracteres")
    private String unidad;

    @NotNull(message = "El stock es obligatorio")
    @DecimalMin(value = "0.0", message = "El stock no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El stock admite máximo 2 decimales")
    private BigDecimal stock;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;
}
