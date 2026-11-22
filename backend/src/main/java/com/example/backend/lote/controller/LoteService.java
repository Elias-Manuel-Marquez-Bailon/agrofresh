package com.example.backend.lote.controller;

import com.example.backend.kernel.ApiResponse;
import com.example.backend.lote.dtos.LoteDto;
import com.example.backend.lote.dtos.LoteResponseDto;
import com.example.backend.lote.model.Lote;
import com.example.backend.lote.model.LoteRepository;
import com.example.backend.producto.model.Producto;
import com.example.backend.producto.model.ProductoRepository;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LoteService {

    private static final int LOTES_PUBLICOS = 3;

    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final ProductorRepository productorRepository;

    // ---------- Productor ----------

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> misLotes(String email, Long productoId) {
        Optional<Productor> productorOpt = productorRepository.findByUsuarioEmail(email);
        if (productorOpt.isEmpty()) {
            return respuesta("Perfil de productor no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        List<LoteResponseDto> lotes = loteRepository.findDelProductor(productorOpt.get().getId(), productoId)
                .stream().map(LoteResponseDto::from).toList();
        return respuesta("Mis lotes", lotes, false, HttpStatus.OK);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> crear(String email, LoteDto dto) {
        ResponseEntity<ApiResponse> error = validarFechas(dto);
        if (error != null) return error;

        Optional<Producto> productoOpt = productoRepository.findById(dto.getProductoId());
        error = validarProductoPropio(email, productoOpt);
        if (error != null) return error;

        Lote lote = Lote.builder().producto(productoOpt.get()).build();
        copiarDatos(dto, lote);
        lote = loteRepository.save(lote);
        return respuesta("Lote registrado correctamente", LoteResponseDto.from(lote), false, HttpStatus.CREATED);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> actualizar(String email, Long id, LoteDto dto) {
        ResponseEntity<ApiResponse> error = validarFechas(dto);
        if (error != null) return error;

        Optional<Lote> loteOpt = loteRepository.findById(id);
        if (loteOpt.isEmpty()) {
            return respuesta("Lote no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        error = validarProductoPropio(email, Optional.of(loteOpt.get().getProducto()));
        if (error != null) return error;

        Lote lote = loteOpt.get();
        // Si cambia de producto, el nuevo también debe ser suyo y el lote no debe estar en pedidos.
        if (!lote.getProducto().getId().equals(dto.getProductoId())) {
            if (loteRepository.estaAsignado(id)) {
                return respuesta("No puedes cambiar el producto de un lote que ya está en pedidos", null, true,
                        HttpStatus.CONFLICT);
            }
            Optional<Producto> nuevoOpt = productoRepository.findById(dto.getProductoId());
            error = validarProductoPropio(email, nuevoOpt);
            if (error != null) return error;
            lote.setProducto(nuevoOpt.get());
        }
        copiarDatos(dto, lote);
        lote = loteRepository.save(lote);
        return respuesta("Lote actualizado correctamente", LoteResponseDto.from(lote), false, HttpStatus.OK);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> eliminar(String email, Long id) {
        Optional<Lote> loteOpt = loteRepository.findById(id);
        if (loteOpt.isEmpty()) {
            return respuesta("Lote no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        ResponseEntity<ApiResponse> error = validarProductoPropio(email, Optional.of(loteOpt.get().getProducto()));
        if (error != null) return error;
        if (loteRepository.estaAsignado(id)) {
            return respuesta("No se puede eliminar: el lote ya está asignado a pedidos", null, true,
                    HttpStatus.CONFLICT);
        }
        loteRepository.delete(loteOpt.get());
        return respuesta("Lote eliminado", null, false, HttpStatus.OK);
    }

    // ---------- Público ----------

    /** Últimos lotes cosechados de un producto activo, para la sección "Origen" del catálogo. */
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> origenDelProducto(Long productoId) {
        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        if (productoOpt.isEmpty() || !productoOpt.get().getActivo()) {
            return respuesta("Producto no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        List<LoteResponseDto> lotes = loteRepository
                .findCosechadosDelProducto(productoId, PageRequest.of(0, LOTES_PUBLICOS)).stream()
                .map(LoteResponseDto::from).toList();
        return respuesta("Origen del producto", lotes, false, HttpStatus.OK);
    }

    // ---------- Auxiliares ----------

    static ResponseEntity<ApiResponse> validarFechas(LoteDto dto) {
        if (dto.getFechaCosecha() != null && dto.getFechaSiembra() != null
                && dto.getFechaCosecha().isBefore(dto.getFechaSiembra())) {
            return new ResponseEntity<>(new ApiResponse("La fecha de cosecha no puede ser anterior a la siembra",
                    null, true, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    private ResponseEntity<ApiResponse> validarProductoPropio(String email, Optional<Producto> productoOpt) {
        if (productoOpt.isEmpty()) {
            return respuesta("Producto no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        Optional<Productor> productorOpt = productorRepository.findByUsuarioEmail(email);
        if (productorOpt.isEmpty()
                || !productoOpt.get().getProductor().getId().equals(productorOpt.get().getId())) {
            return respuesta("Solo puedes gestionar lotes de tus propios productos", null, true,
                    HttpStatus.FORBIDDEN);
        }
        return null;
    }

    private void copiarDatos(LoteDto dto, Lote lote) {
        lote.setFechaSiembra(dto.getFechaSiembra());
        lote.setFechaCosecha(dto.getFechaCosecha());
        lote.setParcela(vacioANull(dto.getParcela()));
        lote.setMetodoCultivo(dto.getMetodoCultivo());
        lote.setNotas(vacioANull(dto.getNotas()));
    }

    private static String vacioANull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private ResponseEntity<ApiResponse> respuesta(String msg, Object data, boolean error, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse(msg, data, error, status), status);
    }
}
