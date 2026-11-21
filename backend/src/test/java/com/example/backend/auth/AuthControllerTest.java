package com.example.backend.auth;

import com.example.backend.auth.controller.AuthController;
import com.example.backend.auth.controller.AuthService;
import com.example.backend.config.security.JwtAuthFilter;
import com.example.backend.config.security.JwtService;
import com.example.backend.config.security.SecurityConfig;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.usuario.model.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba la capa web: validaciones de DTOs y seguridad de las rutas (sin BD). */
@WebMvcTest(controllers = AuthController.class,
        properties = {"cors.allowed-origins=http://localhost:5173"})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void registroConDatosInvalidos_devuelve400ConErroresPorCampo() throws Exception {
        String body = """
                {"nombre":"","email":"no-es-correo","password":"123","rol":"ADMIN"}
                """;

        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.password").exists())
                .andExpect(jsonPath("$.data.rol").value("El rol debe ser PRODUCTOR o COMPRADOR"));

        verify(authService, never()).registrar(any());
    }

    @Test
    void loginEsPublico() throws Exception {
        when(authService.login(any())).thenReturn(new ResponseEntity<>(
                new ApiResponse("Inicio de sesión exitoso", null, false, HttpStatus.OK), HttpStatus.OK));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@test.com\",\"password\":\"Secreta123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inicio de sesión exitoso"));
    }

    @Test
    void meSinToken_devuelve401() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Debes iniciar sesión"));
    }
}
