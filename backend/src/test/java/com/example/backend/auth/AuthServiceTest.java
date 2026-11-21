package com.example.backend.auth;

import com.example.backend.auth.controller.AuthService;
import com.example.backend.auth.dtos.AuthResponseDto;
import com.example.backend.auth.dtos.LoginDto;
import com.example.backend.auth.dtos.RegistroDto;
import com.example.backend.config.security.JwtService;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.usuario.model.EstadoUsuario;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import com.example.backend.usuario.model.Rol;
import com.example.backend.usuario.model.Usuario;
import com.example.backend.usuario.model.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProductorRepository productorRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService =
            new JwtService("clave-de-pruebas-unitarias-con-mas-de-32-caracteres", 3_600_000);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, productorRepository, passwordEncoder, jwtService);
    }

    private Usuario usuario(EstadoUsuario estado) {
        return Usuario.builder().id(1L).nombre("Ana").email("ana@test.com")
                .passwordHash(passwordEncoder.encode("Secreta123"))
                .rol(Rol.COMPRADOR).estado(estado).build();
    }

    // ---------- Registro ----------

    @Test
    void registrarComprador_creaUsuarioConPasswordHasheadaYDevuelveToken() {
        RegistroDto dto = RegistroDto.builder().nombre("Ana").email("  ANA@Test.com ")
                .password("Secreta123").rol("COMPRADOR").build();
        when(usuarioRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(10L);
            return u;
        });

        ResponseEntity<ApiResponse> res = authService.registrar(dto);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertThat(guardado.getEmail()).isEqualTo("ana@test.com");
        assertThat(guardado.getPasswordHash()).isNotEqualTo("Secreta123");
        assertThat(passwordEncoder.matches("Secreta123", guardado.getPasswordHash())).isTrue();
        verify(productorRepository, never()).save(any());

        AuthResponseDto auth = (AuthResponseDto) res.getBody().getData();
        assertThat(jwtService.extraerEmail(auth.getToken())).isEqualTo("ana@test.com");
        assertThat(auth.getUsuario().getRol()).isEqualTo("COMPRADOR");
    }

    @Test
    void registrarProductor_creaTambienPerfilDeProductor() {
        RegistroDto dto = RegistroDto.builder().nombre("Luis").email("luis@test.com")
                .password("Secreta123").rol("PRODUCTOR").nombreFinca("La Esperanza").build();
        when(usuarioRepository.existsByEmail("luis@test.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<ApiResponse> res = authService.registrar(dto);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Productor> captor = ArgumentCaptor.forClass(Productor.class);
        verify(productorRepository).save(captor.capture());
        assertThat(captor.getValue().getNombreFinca()).isEqualTo("La Esperanza");
    }

    @Test
    void registrarProductorSinFinca_devuelve400() {
        RegistroDto dto = RegistroDto.builder().nombre("Luis").email("luis@test.com")
                .password("Secreta123").rol("PRODUCTOR").build();
        when(usuarioRepository.existsByEmail("luis@test.com")).thenReturn(false);

        ResponseEntity<ApiResponse> res = authService.registrar(dto);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarEmailDuplicado_devuelve409() {
        RegistroDto dto = RegistroDto.builder().nombre("Ana").email("ana@test.com")
                .password("Secreta123").rol("COMPRADOR").build();
        when(usuarioRepository.existsByEmail("ana@test.com")).thenReturn(true);

        ResponseEntity<ApiResponse> res = authService.registrar(dto);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody().isError()).isTrue();
        verify(usuarioRepository, never()).save(any());
    }

    // ---------- Login ----------

    @Test
    void loginCorrecto_devuelveToken() {
        when(usuarioRepository.findByEmail("ana@test.com"))
                .thenReturn(Optional.of(usuario(EstadoUsuario.ACTIVO)));

        ResponseEntity<ApiResponse> res = authService.login(new LoginDto("ana@test.com", "Secreta123"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponseDto auth = (AuthResponseDto) res.getBody().getData();
        assertThat(auth.getToken()).isNotBlank();
        assertThat(auth.getTipo()).isEqualTo("Bearer");
    }

    @Test
    void loginPasswordIncorrecta_devuelve401() {
        when(usuarioRepository.findByEmail("ana@test.com"))
                .thenReturn(Optional.of(usuario(EstadoUsuario.ACTIVO)));

        ResponseEntity<ApiResponse> res = authService.login(new LoginDto("ana@test.com", "otra"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody().getData()).isNull();
    }

    @Test
    void loginEmailInexistente_devuelve401ConMismoMensaje() {
        when(usuarioRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse> res = authService.login(new LoginDto("nadie@test.com", "x"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody().getMessage()).isEqualTo("Correo o contraseña incorrectos");
    }

    @Test
    void loginUsuarioBloqueado_devuelve403() {
        when(usuarioRepository.findByEmail("ana@test.com"))
                .thenReturn(Optional.of(usuario(EstadoUsuario.BLOQUEADO)));

        ResponseEntity<ApiResponse> res = authService.login(new LoginDto("ana@test.com", "Secreta123"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
