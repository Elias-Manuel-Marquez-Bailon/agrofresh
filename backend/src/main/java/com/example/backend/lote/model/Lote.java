package com.example.backend.lote.model;

import com.example.backend.producto.model.Producto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Lote de producción de un producto: de la siembra a la cosecha. */
@Entity
@Table(name = "lotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "fecha_siembra", nullable = false)
    private LocalDate fechaSiembra;

    /** null mientras no se ha cosechado. */
    @Column(name = "fecha_cosecha")
    private LocalDate fechaCosecha;

    @Column(length = 100)
    private String parcela;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_cultivo", length = 50)
    private MetodoCultivo metodoCultivo;

    @Column(columnDefinition = "TEXT")
    private String notas;
}
