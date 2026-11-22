package com.example.backend.calificacion.model;

import com.example.backend.pedido.model.Pedido;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Calificación de un pedido ENTREGADO (una por pedido). */
@Entity
@Table(name = "calificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Usuario comprador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "productor_id", nullable = false)
    private Productor productor;

    @Column(nullable = false)
    private Integer puntuacion;

    @Column(length = 500)
    private String comentario;
}
