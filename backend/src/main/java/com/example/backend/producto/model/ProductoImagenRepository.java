package com.example.backend.producto.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Long> {

    /** Solo id y fecha (sin el binario) para armar las URLs de las imágenes al listar. */
    interface ImagenInfo {
        Long getProductoId();

        LocalDateTime getActualizadoEn();
    }

    @Query("SELECT i.productoId AS productoId, i.actualizadoEn AS actualizadoEn "
            + "FROM ProductoImagen i WHERE i.productoId IN :ids")
    List<ImagenInfo> findInfoByProductoIds(@Param("ids") Collection<Long> ids);
}
