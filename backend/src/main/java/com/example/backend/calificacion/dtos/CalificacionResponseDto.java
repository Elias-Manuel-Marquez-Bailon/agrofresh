package com.example.backend.calificacion.dtos;

import com.example.backend.calificacion.model.Calificacion;

/** Reseña pública: solo se muestra el primer nombre del comprador. */
public record CalificacionResponseDto(Long id, Long pedidoId, Integer puntuacion, String comentario,
                                      String compradorNombre) {

    public static CalificacionResponseDto from(Calificacion c) {
        String nombre = c.getComprador().getNombre();
        String primerNombre = nombre == null ? "" : nombre.trim().split("\\s+")[0];
        return new CalificacionResponseDto(c.getId(), c.getPedido().getId(), c.getPuntuacion(), c.getComentario(),
                primerNombre);
    }
}
