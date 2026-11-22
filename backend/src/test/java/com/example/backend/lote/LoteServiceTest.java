package com.example.backend.lote;

import com.example.backend.kernel.ApiResponse;
import com.example.backend.lote.controller.LoteService;
import com.example.backend.lote.dtos.LoteDto;
import com.example.backend.lote.model.Lote;
import com.example.backend.lote.model.LoteRepository;
import com.example.backend.lote.model.MetodoCultivo;
import com.example.backend.producto.model.Producto;
import com.example.backend.producto.model.ProductoRepository;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    private static final String EMAIL = "juan@finca.com";

    @Mock
    private LoteRepository loteRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private ProductorRepository productorRepository;
    @InjectMocks
    private LoteService loteService;

    private final Productor juan = Productor.builder().id(1L).nombreFinca("La Esperanza").build();
    private final Productor otro = Productor.builder().id(2L).nombreFinca("El Roble").build();

    private Producto productoDe(Productor p) {
        return Producto.builder().id(10L).productor(p).nombre("Mango").precio(BigDecimal.TEN).unidad("kg")
                .stock(BigDecimal.ONE).activo(true).build();
    }

    private LoteDto dto(LocalDate siembra, LocalDate cosecha) {
        return LoteDto.builder().productoId(10L).fechaSiembra(siembra).fechaCosecha(cosecha)
                .parcela(" Parcela norte ").metodoCultivo(MetodoCultivo.ORGANICO).notas("  ").build();
    }

    @Test
    void crear_guardaElLoteDelProductoPropio() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(juan)));
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));
        when(loteRepository.save(any(Lote.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<ApiResponse> res = loteService.crear(EMAIL,
                dto(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 10, 1)));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Lote> captor = ArgumentCaptor.forClass(Lote.class);
        verify(loteRepository).save(captor.capture());
        assertThat(captor.getValue().getParcela()).isEqualTo("Parcela norte");
        assertThat(captor.getValue().getNotas()).isNull();
    }

    @Test
    void crear_rechazaCosechaAnteriorALaSiembra() {
        ResponseEntity<ApiResponse> res = loteService.crear(EMAIL,
                dto(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 1)));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(loteRepository, never()).save(any());
    }

    @Test
    void crear_rechazaProductoDeOtroProductor() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(otro)));
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));

        ResponseEntity<ApiResponse> res = loteService.crear(EMAIL, dto(LocalDate.of(2026, 6, 1), null));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(loteRepository, never()).save(any());
    }

    @Test
    void eliminar_noPermiteBorrarUnLoteAsignadoAPedidos() {
        Lote lote = Lote.builder().id(5L).producto(productoDe(juan)).fechaSiembra(LocalDate.of(2026, 6, 1)).build();
        when(loteRepository.findById(5L)).thenReturn(Optional.of(lote));
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));
        when(loteRepository.estaAsignado(5L)).thenReturn(true);

        assertThat(loteService.eliminar(EMAIL, 5L).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(loteRepository, never()).delete(any());
    }

    @Test
    void eliminar_borraUnLoteLibre() {
        Lote lote = Lote.builder().id(5L).producto(productoDe(juan)).fechaSiembra(LocalDate.of(2026, 6, 1)).build();
        when(loteRepository.findById(5L)).thenReturn(Optional.of(lote));
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));
        when(loteRepository.estaAsignado(5L)).thenReturn(false);

        assertThat(loteService.eliminar(EMAIL, 5L).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(loteRepository).delete(lote);
    }
}
