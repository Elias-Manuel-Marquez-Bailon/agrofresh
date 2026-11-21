package com.example.backend.categoria;

import com.example.backend.categoria.controller.CategoriaService;
import com.example.backend.categoria.dtos.CategoriaDto;
import com.example.backend.categoria.model.Categoria;
import com.example.backend.categoria.model.CategoriaRepository;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.producto.model.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void crear_guardaConNombreSinEspacios() {
        when(categoriaRepository.existsByNombreIgnoreCase("Hongos")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> {
            Categoria c = i.getArgument(0);
            c.setId(11L);
            return c;
        });

        ResponseEntity<ApiResponse> res = categoriaService.crear(new CategoriaDto(null, "  Hongos "));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(((CategoriaDto) res.getBody().getData()).getNombre()).isEqualTo("Hongos");
    }

    @Test
    void crear_nombreDuplicado_devuelve409() {
        when(categoriaRepository.existsByNombreIgnoreCase("Frutas")).thenReturn(true);

        ResponseEntity<ApiResponse> res = categoriaService.crear(new CategoriaDto(null, "Frutas"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void actualizar_inexistente_devuelve404() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse> res = categoriaService.actualizar(99L, new CategoriaDto(null, "X"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void eliminar_conProductos_devuelve409YNoBorra() {
        when(categoriaRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.existsByCategoriaId(1L)).thenReturn(true);

        ResponseEntity<ApiResponse> res = categoriaService.eliminar(1L);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(categoriaRepository, never()).deleteById(anyLong());
    }

    @Test
    void eliminar_sinProductos_borra() {
        when(categoriaRepository.existsById(2L)).thenReturn(true);
        when(productoRepository.existsByCategoriaId(2L)).thenReturn(false);

        ResponseEntity<ApiResponse> res = categoriaService.eliminar(2L);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(categoriaRepository).deleteById(2L);
    }
}
