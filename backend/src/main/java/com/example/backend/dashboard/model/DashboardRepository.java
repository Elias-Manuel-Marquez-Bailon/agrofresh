package com.example.backend.dashboard.model;

import com.example.backend.pedido.model.EstadoPedido;
import com.example.backend.pedido.model.Pedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Consultas de solo lectura para los dashboards y el CSV.
 * En todas, compradorId y productorId son opcionales (null = no filtra), igual que en PedidoRepository.
 */
@org.springframework.stereotype.Repository
public interface DashboardRepository extends Repository<Pedido, Long> {

    interface ConteoEstado {
        EstadoPedido getEstado();

        Long getCantidad();

        BigDecimal getMonto();
    }

    interface VentaMes {
        Integer getAnio();

        Integer getMes();

        BigDecimal getMonto();

        Long getPedidos();
    }

    interface TopProducto {
        Long getId();

        String getNombre();

        String getUnidad();

        BigDecimal getCantidad();

        BigDecimal getMonto();
    }

    interface TopProductor {
        Long getId();

        String getNombre();

        Long getPedidos();

        BigDecimal getMonto();
    }

    interface VentaCategoria {
        String getNombre();

        BigDecimal getMonto();
    }

    interface ConteoRol {
        com.example.backend.usuario.model.Rol getRol();

        Long getCantidad();
    }

    String FILTRO = "(:compradorId IS NULL OR p.comprador.id = :compradorId) "
            + "AND (:productorId IS NULL OR p.productor.id = :productorId) ";

    @Query("SELECT p.estado AS estado, COUNT(p) AS cantidad, SUM(p.total) AS monto "
            + "FROM Pedido p WHERE " + FILTRO + "GROUP BY p.estado")
    List<ConteoEstado> conteoPorEstado(@Param("compradorId") Long compradorId,
                                       @Param("productorId") Long productorId);

    @Query("SELECT YEAR(p.createdAt) AS anio, MONTH(p.createdAt) AS mes, SUM(p.total) AS monto, COUNT(p) AS pedidos "
            + "FROM Pedido p WHERE " + FILTRO + "AND p.estado IN :estados AND p.createdAt >= :desde "
            + "GROUP BY YEAR(p.createdAt), MONTH(p.createdAt)")
    List<VentaMes> ventasPorMes(@Param("compradorId") Long compradorId, @Param("productorId") Long productorId,
                                @Param("estados") Collection<EstadoPedido> estados,
                                @Param("desde") LocalDateTime desde);

    /** Se ordena por monto porque los productos tienen unidades distintas (kg, pieza...). */
    @Query("SELECT pr.id AS id, pr.nombre AS nombre, pr.unidad AS unidad, SUM(d.cantidad) AS cantidad, "
            + "SUM(d.cantidad * d.precioUnitario) AS monto "
            + "FROM DetallePedido d JOIN d.pedido p JOIN d.producto pr "
            + "WHERE " + FILTRO + "AND p.estado IN :estados "
            + "GROUP BY pr.id, pr.nombre, pr.unidad ORDER BY SUM(d.cantidad * d.precioUnitario) DESC")
    List<TopProducto> topProductos(@Param("compradorId") Long compradorId, @Param("productorId") Long productorId,
                                   @Param("estados") Collection<EstadoPedido> estados, Pageable pageable);

    @Query("SELECT pd.id AS id, pd.nombreFinca AS nombre, COUNT(p) AS pedidos, SUM(p.total) AS monto "
            + "FROM Pedido p JOIN p.productor pd "
            + "WHERE " + FILTRO + "AND p.estado IN :estados "
            + "GROUP BY pd.id, pd.nombreFinca ORDER BY SUM(p.total) DESC")
    List<TopProductor> topProductores(@Param("compradorId") Long compradorId, @Param("productorId") Long productorId,
                                      @Param("estados") Collection<EstadoPedido> estados, Pageable pageable);

    @Query("SELECT c.nombre AS nombre, SUM(d.cantidad * d.precioUnitario) AS monto "
            + "FROM DetallePedido d JOIN d.pedido p JOIN d.producto pr LEFT JOIN pr.categoria c "
            + "WHERE p.estado IN :estados GROUP BY c.id, c.nombre ORDER BY SUM(d.cantidad * d.precioUnitario) DESC")
    List<VentaCategoria> ventasPorCategoria(@Param("estados") Collection<EstadoPedido> estados);

    @Query("SELECT u.rol AS rol, COUNT(u) AS cantidad FROM Usuario u GROUP BY u.rol")
    List<ConteoRol> usuariosPorRol();

    /** Para el CSV. Las fechas son opcionales. Los detalles se cargan por lotes (BatchSize en Pedido). */
    @EntityGraph(attributePaths = {"comprador", "productor"})
    @Query("SELECT p FROM Pedido p WHERE " + FILTRO
            + "AND (:estado IS NULL OR p.estado = :estado) "
            + "AND (:desde IS NULL OR p.createdAt >= :desde) AND (:hasta IS NULL OR p.createdAt < :hasta) "
            + "ORDER BY p.id DESC")
    List<Pedido> pedidosParaCsv(@Param("compradorId") Long compradorId, @Param("productorId") Long productorId,
                                @Param("estado") EstadoPedido estado,
                                @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
