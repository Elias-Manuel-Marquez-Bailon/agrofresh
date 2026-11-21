package com.example.backend.config.security;

import com.example.backend.usuario.model.EstadoUsuario;
import com.example.backend.usuario.model.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el header "Authorization: Bearer <token>", valida el token y,
 * si el usuario existe y está ACTIVO, lo deja autenticado con su rol.
 */
@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String email = jwtService.extraerEmail(header.substring(7));

            if (email != null) {
                usuarioRepository.findByEmail(email)
                        .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO)
                        .ifPresent(u -> {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    u.getEmail(), null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        }

        chain.doFilter(request, response);
    }
}
