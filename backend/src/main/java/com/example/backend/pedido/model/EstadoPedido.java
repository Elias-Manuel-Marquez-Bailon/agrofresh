package com.example.backend.pedido.model;

/** Flujo: PENDIENTE -> ACEPTADO -> EN_CAMINO -> ENTREGADO, o CANCELADO (desde PENDIENTE o ACEPTADO). */
public enum EstadoPedido {
    PENDIENTE,
    ACEPTADO,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO
}
