package com.example.backend.auth.dtos;

import com.example.backend.usuario.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos públicos del usuario (nunca incluye la contraseña). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {
    private Long id;
    private String nombre;
    private String email;
    private String rol;
    private String estado;

    public static UsuarioDto from(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombre(), u.getEmail(),
                u.getRol().name(), u.getEstado().name());
    }
}
