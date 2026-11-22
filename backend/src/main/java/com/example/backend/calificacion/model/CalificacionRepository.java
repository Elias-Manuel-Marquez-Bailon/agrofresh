package com.example.backend.calificacion.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    /** Promedio y total de calificaciones de un productor. */
    interface Resumen {
        Long getProductorId();

        Double getPromedio();

        Long getTotal();
    }

    boolean existsByPedidoId(Long pedidoId);

    @EntityGraph(attributePaths = {"comprador"})
    Page<Calificacion> findByProductorId(Long productorId, Pageable pageable);

    /** Para saber qué pedidos de una página ya están calificados. */
    @Query("SELECT c FROM Calificacion c WHERE c.pedido.id IN :pedidoIds")
    List<Calificacion> findByPedidoIds(@Param("pedidoIds") Collection<Long> pedidoIds);

    @Query("SELECT c.productor.id AS productorId, AVG(c.puntuacion) AS promedio, COUNT(c) AS total "
            + "FROM Calificacion c WHERE c.productor.id IN :productorIds GROUP BY c.productor.id")
    List<Resumen> resumenPorProductores(@Param("productorIds") Collection<Long> productorIds);
}
