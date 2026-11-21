package com.example.backend.producto.dtos;

import java.util.List;

/** Página de resultados en un formato simple para el frontend. */
public record PaginaDto<T>(List<T> contenido, int pagina, int tamanio, long totalElementos, int totalPaginas) {
}
