package com.example.backend.calificacion;

import com.example.backend.calificacion.controller.CalificacionService;
import com.example.backend.calificacion.dtos.CalificacionDto;
import com.example.backend.calificacion.model.Calificacion;
import com.example.backend.calificacion.model.CalificacionRepository;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.pedido.model.EstadoPedido;
import com.example.backend.pedido.model.Pedido;
import com.example.backend.pedido.model.PedidoRepository;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import com.example.backend.usuario.model.Rol;
import com.example.backend.usuario.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceTest {

    @Mock
    private CalificacionRepository calificacionRepository;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProductorRepository productorRepository;
    @InjectMocks
    private CalificacionService calificacionService;

    private final Usuario ana = Usuario.builder().id(1L).nombre("Ana López").email("ana@correo.com")
            .rol(Rol.COMPRADOR).build();
    private final Productor juan = Productor.builder().id(1L).nombreFinca("La Esperanza").build();

    private Pedido pedido(EstadoPedido estado) {
        return Pedido.builder().id(100L).comprador(ana).productor(juan).estado(estado).total(BigDecimal.TEN)
                .direccionEntrega("Calle 1").build();
    }

    private CalificacionDto dto() {
        return CalificacionDto.builder().pedidoId(100L).puntuacion(5).comentario(" Muy frescos ").build();
    }

    @Test
    void calificar_pedidoEntregadoDelComprador() {
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido(EstadoPedido.ENTREGADO)));
        when(calificacionRepository.existsByPedidoId(100L)).thenReturn(false);
        when(calificacionRepository.save(any(Calificacion.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<ApiResponse> res = calificacionService.calificar("ana@correo.com", dto());

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Calificacion> captor = ArgumentCaptor.forClass(Calificacion.class);
        verify(calificacionRepository).save(captor.capture());
        assertThat(captor.getValue().getProductor()).isSameAs(juan);
        assertThat(captor.getValue().getComentario()).isEqualTo("Muy frescos");
    }

    @Test
    void calificar_rechazaPedidoNoEntregado() {
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido(EstadoPedido.EN_CAMINO)));

        assertThat(calificacionService.calificar("ana@correo.com", dto()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void calificar_soloUnaVezPorPedido() {
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido(EstadoPedido.ENTREGADO)));
        when(calificacionRepository.existsByPedidoId(100L)).thenReturn(true);

        assertThat(calificacionService.calificar("ana@correo.com", dto()).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void calificar_rechazaPedidoDeOtroComprador() {
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido(EstadoPedido.ENTREGADO)));

        assertThat(calificacionService.calificar("eva@correo.com", dto()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void redondear_aUnDecimal() {
        assertThat(CalificacionService.redondear(4.666)).isEqualTo(4.7);
        assertThat(CalificacionService.redondear(null)).isNull();
    }
}
