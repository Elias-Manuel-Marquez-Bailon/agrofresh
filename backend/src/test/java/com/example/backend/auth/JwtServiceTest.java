package com.example.backend.auth;

import com.example.backend.config.security.JwtService;
import com.example.backend.usuario.model.Rol;
import com.example.backend.usuario.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRETO = "clave-de-pruebas-unitarias-con-mas-de-32-caracteres";

    private final Usuario usuario = Usuario.builder()
            .id(5L).email("pedro@test.com").rol(Rol.PRODUCTOR).build();

    @Test
    void tokenGenerado_seLeeCorrectamente() {
        JwtService jwt = new JwtService(SECRETO, 60_000);
        String token = jwt.generarToken(usuario);
        assertThat(jwt.extraerEmail(token)).isEqualTo("pedro@test.com");
    }

    @Test
    void tokenFirmadoConOtraClave_esInvalido() {
        String token = new JwtService("otra-clave-distinta-tambien-de-mas-de-32-chars", 60_000)
                .generarToken(usuario);
        assertThat(new JwtService(SECRETO, 60_000).extraerEmail(token)).isNull();
    }

    @Test
    void tokenExpirado_esInvalido() {
        JwtService jwt = new JwtService(SECRETO, -1000);
        assertThat(jwt.extraerEmail(jwt.generarToken(usuario))).isNull();
    }

    @Test
    void tokenBasura_esInvalido() {
        assertThat(new JwtService(SECRETO, 60_000).extraerEmail("no.es.un.token")).isNull();
    }

    @Test
    void secretoCorto_lanzaError() {
        assertThatThrownBy(() -> new JwtService("corto", 60_000))
                .isInstanceOf(IllegalStateException.class);
    }
}
