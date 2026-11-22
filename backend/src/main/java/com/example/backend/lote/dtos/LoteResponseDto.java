package com.example.backend.lote.dtos;

import com.example.backend.lote.model.Lote;
import com.example.backend.lote.model.MetodoCultivo;

import java.time.LocalDate;

public record LoteResponseDto(Long id, Long productoId, String nombreProducto, LocalDate fechaSiembra,
                              LocalDate fechaCosecha, String parcela, MetodoCultivo metodoCultivo, String notas) {

    public static LoteResponseDto from(Lote l) {
        return new LoteResponseDto(l.getId(), l.getProducto().getId(), l.getProducto().getNombre(),
                l.getFechaSiembra(), l.getFechaCosecha(), l.getParcela(), l.getMetodoCultivo(), l.getNotas());
    }
}
