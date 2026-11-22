package com.example.backend.producto.model;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /** Catálogo público: solo activos, con búsqueda por nombre y filtro por categoría (ambos opcionales). */
    @Query(value = "SELECT p FROM Producto p JOIN FETCH p.categoria JOIN FETCH p.productor "
            + "WHERE p.activo = true "
            + "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId) "
            + "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))",
            countQuery = "SELECT COUNT(p) FROM Producto p "
            + "WHERE p.activo = true "
            + "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId) "
            + "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    Page<Producto> buscarCatalogo(@Param("nombre") String nombre,
                                  @Param("categoriaId") Long categoriaId,
                                  Pageable pageable);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria JOIN FETCH p.productor "
            + "WHERE p.productor.id = :productorId ORDER BY p.id DESC")
    List<Producto> findDelProductor(@Param("productorId") Long productorId);

    boolean existsByCategoriaId(Long categoriaId);

    /** Bloquea la fila del producto para descontar o reponer stock sin condiciones de carrera. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdParaActualizar(@Param("id") Long id);
}
