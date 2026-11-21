package com.example.backend.producto;

import com.example.backend.categoria.model.Categoria;
import com.example.backend.categoria.model.CategoriaRepository;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.producto.controller.ProductoService;
import com.example.backend.producto.dtos.ProductoDto;
import com.example.backend.producto.dtos.ProductoResponseDto;
import com.example.backend.producto.model.Producto;
import com.example.backend.producto.model.ProductoImagen;
import com.example.backend.producto.model.ProductoImagenRepository;
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
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    private static final String EMAIL = "juan@finca.com";

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private ProductoImagenRepository imagenRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private ProductorRepository productorRepository;
    @InjectMocks
    private ProductoService productoService;

    private final Productor juan = Productor.builder().id(1L).nombreFinca("La Esperanza").build();
    private final Productor otro = Productor.builder().id(2L).nombreFinca("El Roble").build();
    private final Categoria frutas = Categoria.builder().id(1L).nombre("Frutas").build();

    private ProductoDto dto() {
        return ProductoDto.builder().nombre(" Mango ").precio(new BigDecimal("25.50"))
                .unidad("kg").stock(new BigDecimal("100")).categoriaId(1L).build();
    }

    private Producto productoDe(Productor p) {
        return Producto.builder().id(10L).productor(p).categoria(frutas).nombre("Mango")
                .precio(BigDecimal.TEN).unidad("kg").stock(BigDecimal.ONE).activo(true).build();
    }

    // ---------- Crear ----------

    @Test
    void crear_asignaElProductorDelTokenYQuedaActivo() {
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(frutas));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> {
            Producto p = i.getArgument(0);
            p.setId(10L);
            return p;
        });

        ResponseEntity<ApiResponse> res = productoService.crear(EMAIL, dto());

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertThat(captor.getValue().getProductor()).isSameAs(juan);
        assertThat(captor.getValue().getNombre()).isEqualTo("Mango");
        assertThat(captor.getValue().getActivo()).isTrue();
    }

    @Test
    void crear_categoriaInexistente_devuelve404() {
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse> res = productoService.crear(EMAIL, dto());

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(productoRepository, never()).save(any());
    }

    // ---------- Solo el dueño modifica ----------

    @Test
    void actualizar_productoDeOtroProductor_devuelve403() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(otro)));
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));

        ResponseEntity<ApiResponse> res = productoService.actualizar(EMAIL, 10L, dto());

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_propio_desactivaElProducto() {
        Producto producto = productoDe(juan);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));
        when(productoRepository.save(producto)).thenReturn(producto);
        when(imagenRepository.findInfoByProductoIds(List.of(10L))).thenReturn(List.of());

        ResponseEntity<ApiResponse> res = productoService.cambiarEstado(EMAIL, 10L, false);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(producto.getActivo()).isFalse();
    }

    // ---------- Catálogo ----------

    @Test
    void detalle_productoInactivo_noSeMuestraEnCatalogo() {
        Producto producto = productoDe(juan);
        producto.setActivo(false);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        ResponseEntity<ApiResponse> res = productoService.detalle(10L);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ---------- Imágenes ----------

    @Test
    void subirImagen_pngValido_seGuardaYDevuelveUrl() throws Exception {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(juan)));
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
        MockMultipartFile archivo = new MockMultipartFile("archivo", "mango.png", "image/png", png);

        ResponseEntity<ApiResponse> res = productoService.subirImagen(EMAIL, 10L, archivo);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<ProductoImagen> captor = ArgumentCaptor.forClass(ProductoImagen.class);
        verify(imagenRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("image/png");
        assertThat(((ProductoResponseDto) res.getBody().getData()).getImagenUrl())
                .startsWith("/api/productos/10/imagen?v=");
    }

    @Test
    void subirImagen_archivoQueNoEsImagen_devuelve400() throws Exception {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(juan)));
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));
        // Se hace pasar por imagen con la extensión y el Content-Type, pero el contenido es texto.
        MockMultipartFile falso = new MockMultipartFile("archivo", "virus.png", "image/png",
                "no soy una imagen".getBytes());

        ResponseEntity<ApiResponse> res = productoService.subirImagen(EMAIL, 10L, falso);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(imagenRepository, never()).save(any());
    }

    @Test
    void subirImagen_masDe2MB_devuelve400() throws Exception {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoDe(juan)));
        when(productorRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(juan));
        MockMultipartFile grande = new MockMultipartFile("archivo", "g.jpg", "image/jpeg",
                new byte[2 * 1024 * 1024 + 1]);

        ResponseEntity<ApiResponse> res = productoService.subirImagen(EMAIL, 10L, grande);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(imagenRepository, never()).save(any());
    }
}
