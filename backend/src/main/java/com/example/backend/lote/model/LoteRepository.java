package com.example.backend.lote.model;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    /** Lotes de un productor; productoId es opcional (null = todos sus productos). */
    @Query("SELECT l FROM Lote l JOIN FETCH l.producto p "
            + "WHERE p.productor.id = :productorId AND (:productoId IS NULL OR p.id = :productoId) "
            + "ORDER BY l.fechaSiembra DESC, l.id DESC")
    List<Lote> findDelProductor(@Param("productorId") Long productorId, @Param("productoId") Long productoId);

    /** Últimos lotes cosechados de un producto (para mostrar el origen en el catálogo). */
    @Query("SELECT l FROM Lote l WHERE l.producto.id = :productoId AND l.fechaCosecha IS NOT NULL "
            + "ORDER BY l.fechaCosecha DESC, l.id DESC")
    List<Lote> findCosechadosDelProducto(@Param("productoId") Long productoId, Pageable pageable);

    /** true si algún detalle de pedido ya usa este lote (entonces no se puede borrar). */
    @Query("SELECT COUNT(d) > 0 FROM DetallePedido d WHERE d.loteId = :loteId")
    boolean estaAsignado(@Param("loteId") Long loteId);
}
