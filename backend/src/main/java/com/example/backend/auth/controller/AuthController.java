package com.example.backend.auth.controller;

import com.example.backend.auth.dtos.LoginDto;
import com.example.backend.auth.dtos.RegistroDto;
import com.example.backend.kernel.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse> registro(@Valid @RequestBody RegistroDto dto) {
        return authService.registrar(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginDto dto) {
        return authService.login(dto);
    }

    /** Devuelve el usuario del token actual. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(Authentication authentication) {
        return authService.perfil(authentication.getName());
    }
}
