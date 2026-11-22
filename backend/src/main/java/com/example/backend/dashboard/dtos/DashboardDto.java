package com.example.backend.dashboard.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos de un dashboard. Las secciones que no aplican a un rol quedan en null y no se envían.
 * "Ventas" = pedidos ACEPTADOS, EN_CAMINO o ENTREGADOS (los PENDIENTES y CANCELADOS no cuentan).
 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardDto {

    private Tarjetas tarjetas;
    private List<Estado> porEstado;
    private List<Mes> porMes;
    private List<Producto> topProductos;
    /** Productor: no aplica. Comprador: a quién le compra más. Admin: quién vende más. */
    private List<Productor> topProductores;
    /** Solo ADMIN. */
    private List<Categoria> porCategoria;
    /** Solo ADMIN. */
    private List<Rol> usuariosPorRol;

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Tarjetas {
        /** Monto vendido (o gastado, para el comprador). */
        private BigDecimal monto;
        private long pedidosTotales;
        /** Pedidos que cuentan como venta. */
        private long pedidosVenta;
        private BigDecimal ticketPromedio;
        /** Solo PRODUCTOR. */
        private Double calificacionPromedio;
        private Long totalCalificaciones;
    }

    public record Estado(String estado, long cantidad, BigDecimal monto) {
    }

    /** periodo = "2026-11" */
    public record Mes(String periodo, BigDecimal monto, long pedidos) {
    }

    public record Producto(Long id, String nombre, String unidad, BigDecimal cantidad, BigDecimal monto) {
    }

    public record Productor(Long id, String nombre, long pedidos, BigDecimal monto) {
    }

    public record Categoria(String nombre, BigDecimal monto) {
    }

    public record Rol(String rol, long cantidad) {
    }
}
