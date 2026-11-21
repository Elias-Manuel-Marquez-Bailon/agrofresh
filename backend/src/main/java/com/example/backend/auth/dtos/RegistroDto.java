package com.example.backend.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registro público. Solo se permite PRODUCTOR o COMPRADOR
 * (los ADMIN no se registran desde la API).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    @Pattern(regexp = "PRODUCTOR|COMPRADOR", message = "El rol debe ser PRODUCTOR o COMPRADOR")
    private String rol;

    // Solo para PRODUCTOR
    @Size(max = 150, message = "El nombre de la finca no puede exceder 150 caracteres")
    private String nombreFinca;

    @Size(max = 200, message = "La ubicación no puede exceder 200 caracteres")
    private String ubicacion;
}
