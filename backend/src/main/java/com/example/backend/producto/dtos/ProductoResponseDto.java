package com.example.backend.producto.dtos;

import com.example.backend.producto.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponseDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String unidad;
    private BigDecimal stock;
    private Boolean activo;
    private Long categoriaId;
    private String categoriaNombre;
    private Long productorId;
    private String nombreFinca;
    /** null si no tiene imagen. Lleva ?v= para que el navegador refresque la caché al cambiarla. */
    private String imagenUrl;
    /** Promedio de estrellas del productor (null si aún no tiene calificaciones). */
    private Double calificacionPromedio;
    @Builder.Default
    private Long totalCalificaciones = 0L;

    public static ProductoResponseDto from(Producto p, LocalDateTime imagenActualizada) {
        String url = imagenActualizada == null ? null
                : "/api/productos/" + p.getId() + "/imagen?v="
                + imagenActualizada.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return ProductoResponseDto.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .unidad(p.getUnidad())
                .stock(p.getStock())
                .activo(p.getActivo())
                .categoriaId(p.getCategoria().getId())
                .categoriaNombre(p.getCategoria().getNombre())
                .productorId(p.getProductor().getId())
                .nombreFinca(p.getProductor().getNombreFinca())
                .imagenUrl(url)
                .build();
    }
}
