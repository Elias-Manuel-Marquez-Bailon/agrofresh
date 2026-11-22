package com.example.backend.dashboard.controller;

import com.example.backend.kernel.ApiResponse;
import com.example.backend.pedido.model.EstadoPedido;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Dashboards por rol y exportación de pedidos a CSV.
 * CSV: ?estado=ENTREGADO&desde=2026-11-01&hasta=2026-11-30 (todos opcionales; "hasta" es inclusivo).
 */
@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/productor")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<ApiResponse> productor(Authentication auth) {
        return dashboardService.productor(auth.getName());
    }

    @GetMapping("/comprador")
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<ApiResponse> comprador(Authentication auth) {
        return dashboardService.comprador(auth.getName());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> admin() {
        return dashboardService.admin();
    }

    @GetMapping("/productor/pedidos.csv")
    @PreAuthorize("hasRole('PRODUCTOR')")
    public ResponseEntity<?> csvProductor(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth) {
        return dashboardService.csvProductor(auth.getName(), estado, desde, hasta);
    }

    @GetMapping("/comprador/pedidos.csv")
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<?> csvComprador(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth) {
        return dashboardService.csvComprador(auth.getName(), estado, desde, hasta);
    }

    @GetMapping("/admin/pedidos.csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> csvAdmin(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return dashboardService.csvAdmin(estado, desde, hasta);
    }
}
