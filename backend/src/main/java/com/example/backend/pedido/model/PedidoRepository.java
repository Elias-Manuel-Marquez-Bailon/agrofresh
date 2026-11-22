package com.example.backend.pedido.model;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** Listado paginado; cada filtro es opcional (null = no filtra). */
    @EntityGraph(attributePaths = {"comprador", "productor"})
    @Query(value = "SELECT p FROM Pedido p "
            + "WHERE (:compradorId IS NULL OR p.comprador.id = :compradorId) "
            + "AND (:productorId IS NULL OR p.productor.id = :productorId) "
            + "AND (:estado IS NULL OR p.estado = :estado)",
            countQuery = "SELECT COUNT(p) FROM Pedido p "
            + "WHERE (:compradorId IS NULL OR p.comprador.id = :compradorId) "
            + "AND (:productorId IS NULL OR p.productor.id = :productorId) "
            + "AND (:estado IS NULL OR p.estado = :estado)")
    Page<Pedido> buscar(@Param("compradorId") Long compradorId,
                        @Param("productorId") Long productorId,
                        @Param("estado") EstadoPedido estado,
                        Pageable pageable);

    /** Bloquea la fila (SELECT ... FOR UPDATE) para que dos cambios de estado simultáneos no se pisen. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findByIdParaActualizar(@Param("id") Long id);
}
