package com.example.backend.calificacion.dtos;

import com.example.backend.producto.dtos.PaginaDto;

/** Resumen de un productor + una página de sus reseñas. promedio es null si aún no tiene. */
public record ResenasDto(Long productorId, String nombreFinca, Double promedio, long total,
                         PaginaDto<CalificacionResponseDto> resenas) {
}
