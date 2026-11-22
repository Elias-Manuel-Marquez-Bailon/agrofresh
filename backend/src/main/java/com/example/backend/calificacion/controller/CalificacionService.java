package com.example.backend.calificacion.controller;

import com.example.backend.calificacion.dtos.CalificacionDto;
import com.example.backend.calificacion.dtos.CalificacionResponseDto;
import com.example.backend.calificacion.dtos.ResenasDto;
import com.example.backend.calificacion.model.Calificacion;
import com.example.backend.calificacion.model.CalificacionRepository;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.pedido.model.EstadoPedido;
import com.example.backend.pedido.model.Pedido;
import com.example.backend.pedido.model.PedidoRepository;
import com.example.backend.producto.dtos.PaginaDto;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CalificacionService {

    private static final int TAMANIO_MAX_PAGINA = 50;

    private final CalificacionRepository calificacionRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductorRepository productorRepository;

    /** Solo el comprador del pedido, si está ENTREGADO y una sola vez. */
    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> calificar(String email, CalificacionDto dto) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(dto.getPedidoId());
        if (pedidoOpt.isEmpty()) {
            return respuesta("Pedido no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        Pedido pedido = pedidoOpt.get();
        if (!pedido.getComprador().getEmail().equalsIgnoreCase(email)) {
            return respuesta("Solo puedes calificar tus propios pedidos", null, true, HttpStatus.FORBIDDEN);
        }
        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            return respuesta("Solo puedes calificar pedidos entregados", null, true, HttpStatus.BAD_REQUEST);
        }
        if (calificacionRepository.existsByPedidoId(pedido.getId())) {
            return respuesta("Este pedido ya fue calificado", null, true, HttpStatus.CONFLICT);
        }

        Calificacion calificacion = Calificacion.builder()
                .pedido(pedido)
                .comprador(pedido.getComprador())
                .productor(pedido.getProductor())
                .puntuacion(dto.getPuntuacion())
                .comentario(dto.getComentario() == null || dto.getComentario().isBlank()
                        ? null : dto.getComentario().trim())
                .build();
        calificacion = calificacionRepository.save(calificacion);
        return respuesta("¡Gracias por tu calificación!", CalificacionResponseDto.from(calificacion), false,
                HttpStatus.CREATED);
    }

    /** Público: promedio y reseñas de un productor. */
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> deProductor(Long productorId, int pagina, int tamanio) {
        Optional<Productor> productorOpt = productorRepository.findById(productorId);
        if (productorOpt.isEmpty()) {
            return respuesta("Productor no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        return resenas(productorOpt.get(), pagina, tamanio);
    }

    /** Las del productor con sesión (para su página de inicio). */
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> mias(String email, int pagina, int tamanio) {
        Optional<Productor> productorOpt = productorRepository.findByUsuarioEmail(email);
        if (productorOpt.isEmpty()) {
            return respuesta("Perfil de productor no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        return resenas(productorOpt.get(), pagina, tamanio);
    }

    private ResponseEntity<ApiResponse> resenas(Productor productor, int pagina, int tamanio) {
        if (pagina < 0) pagina = 0;
        if (tamanio < 1 || tamanio > TAMANIO_MAX_PAGINA) tamanio = 5;

        Page<Calificacion> page = calificacionRepository.findByProductorId(productor.getId(),
                PageRequest.of(pagina, tamanio, Sort.by(Sort.Direction.DESC, "id")));
        PaginaDto<CalificacionResponseDto> resenas = new PaginaDto<>(
                page.getContent().stream().map(CalificacionResponseDto::from).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());

        List<CalificacionRepository.Resumen> resumen =
                calificacionRepository.resumenPorProductores(List.of(productor.getId()));
        Double promedio = resumen.isEmpty() ? null : redondear(resumen.get(0).getPromedio());
        long total = resumen.isEmpty() ? 0 : resumen.get(0).getTotal();

        return respuesta("Reseñas del productor",
                new ResenasDto(productor.getId(), productor.getNombreFinca(), promedio, total, resenas),
                false, HttpStatus.OK);
    }

    /** 4.666 -> 4.7 */
    public static Double redondear(Double valor) {
        return valor == null ? null : BigDecimal.valueOf(valor).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private ResponseEntity<ApiResponse> respuesta(String msg, Object data, boolean error, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse(msg, data, error, status), status);
    }
}
