package com.example.backend.auth.controller;

import com.example.backend.auth.dtos.AuthResponseDto;
import com.example.backend.auth.dtos.LoginDto;
import com.example.backend.auth.dtos.RegistroDto;
import com.example.backend.auth.dtos.UsuarioDto;
import com.example.backend.config.security.JwtService;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.usuario.model.EstadoUsuario;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import com.example.backend.usuario.model.Rol;
import com.example.backend.usuario.model.Usuario;
import com.example.backend.usuario.model.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ProductorRepository productorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> registrar(RegistroDto dto) {
        String email = dto.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            return respuesta("El correo ya está registrado", null, true, HttpStatus.CONFLICT);
        }

        Rol rol = Rol.valueOf(dto.getRol());

        if (rol == Rol.PRODUCTOR && (dto.getNombreFinca() == null || dto.getNombreFinca().isBlank())) {
            return respuesta("El nombre de la finca es obligatorio para productores", null, true, HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .rol(rol)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuario = usuarioRepository.save(usuario);

        if (rol == Rol.PRODUCTOR) {
            productorRepository.save(Productor.builder()
                    .usuario(usuario)
                    .nombreFinca(dto.getNombreFinca().trim())
                    .ubicacion(dto.getUbicacion())
                    .build());
        }

        return respuesta("Usuario registrado correctamente", construirAuth(usuario), false, HttpStatus.CREATED);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> login(LoginDto dto) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(dto.getEmail().trim().toLowerCase());

        // Mismo mensaje si el correo no existe o la contraseña es incorrecta (no revelar cuál falló)
        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(dto.getPassword(), usuarioOpt.get().getPasswordHash())) {
            return respuesta("Correo o contraseña incorrectos", null, true, HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getEstado() == EstadoUsuario.BLOQUEADO) {
            return respuesta("Tu cuenta está bloqueada, contacta al administrador", null, true, HttpStatus.FORBIDDEN);
        }
        if (usuario.getEstado() == EstadoUsuario.PENDIENTE) {
            return respuesta("Tu cuenta está pendiente de aprobación", null, true, HttpStatus.FORBIDDEN);
        }

        return respuesta("Inicio de sesión exitoso", construirAuth(usuario), false, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> perfil(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return respuesta("Usuario no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        return respuesta("Perfil del usuario", UsuarioDto.from(usuarioOpt.get()), false, HttpStatus.OK);
    }

    private AuthResponseDto construirAuth(Usuario usuario) {
        return new AuthResponseDto(jwtService.generarToken(usuario), "Bearer",
                jwtService.getExpiracionMs(), UsuarioDto.from(usuario));
    }

    private ResponseEntity<ApiResponse> respuesta(String msg, Object data, boolean error, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse(msg, data, error, status), status);
    }
}
