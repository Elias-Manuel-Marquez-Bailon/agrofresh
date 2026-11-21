package com.example.backend.categoria.dtos;

import com.example.backend.categoria.model.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaDto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede tener más de 80 caracteres")
    private String nombre;

    public static CategoriaDto from(Categoria c) {
        return new CategoriaDto(c.getId(), c.getNombre());
    }
}
