package com.example.backend.categoria.controller;

import com.example.backend.categoria.dtos.CategoriaDto;
import com.example.backend.categoria.model.Categoria;
import com.example.backend.categoria.model.CategoriaRepository;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.producto.model.ProductoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> listar() {
        List<CategoriaDto> categorias = categoriaRepository.findAll(Sort.by("nombre")).stream()
                .map(CategoriaDto::from).toList();
        return respuesta("Lista de categorías", categorias, false, HttpStatus.OK);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> crear(CategoriaDto dto) {
        String nombre = dto.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            return respuesta("Ya existe una categoría con ese nombre", null, true, HttpStatus.CONFLICT);
        }
        Categoria categoria = categoriaRepository.save(Categoria.builder().nombre(nombre).build());
        return respuesta("Categoría creada correctamente", CategoriaDto.from(categoria), false, HttpStatus.CREATED);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> actualizar(Long id, CategoriaDto dto) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
        if (categoriaOpt.isEmpty()) {
            return respuesta("Categoría no encontrada", null, true, HttpStatus.NOT_FOUND);
        }
        String nombre = dto.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            return respuesta("Ya existe una categoría con ese nombre", null, true, HttpStatus.CONFLICT);
        }
        Categoria categoria = categoriaOpt.get();
        categoria.setNombre(nombre);
        return respuesta("Categoría actualizada correctamente", CategoriaDto.from(categoriaRepository.save(categoria)),
                false, HttpStatus.OK);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> eliminar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            return respuesta("Categoría no encontrada", null, true, HttpStatus.NOT_FOUND);
        }
        if (productoRepository.existsByCategoriaId(id)) {
            return respuesta("No se puede eliminar: la categoría tiene productos asociados", null, true, HttpStatus.CONFLICT);
        }
        categoriaRepository.deleteById(id);
        return respuesta("Categoría eliminada correctamente", null, false, HttpStatus.OK);
    }

    private ResponseEntity<ApiResponse> respuesta(String msg, Object data, boolean error, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse(msg, data, error, status), status);
    }
}
