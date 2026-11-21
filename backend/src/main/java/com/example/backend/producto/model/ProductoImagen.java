package com.example.backend.producto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Imagen opcional de un producto (tabla aparte para no cargar binarios al listar). */
@Entity
@Table(name = "producto_imagenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoImagen {

    @Id
    @Column(name = "producto_id")
    private Long productoId;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] datos;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
