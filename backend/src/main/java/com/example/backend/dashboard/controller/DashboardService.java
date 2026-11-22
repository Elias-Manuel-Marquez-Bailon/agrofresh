package com.example.backend.dashboard.controller;

import com.example.backend.calificacion.model.CalificacionRepository;
import com.example.backend.dashboard.dtos.DashboardDto;
import com.example.backend.dashboard.model.DashboardRepository;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.pedido.model.DetallePedido;
import com.example.backend.pedido.model.EstadoPedido;
import com.example.backend.pedido.model.Pedido;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import com.example.backend.usuario.model.Usuario;
import com.example.backend.usuario.model.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DashboardService {

    /** Estados que cuentan como venta (decisión del usuario): todo lo aceptado y no cancelado. */
    public static final Set<EstadoPedido> ESTADOS_VENTA =
            Set.of(EstadoPedido.ACEPTADO, EstadoPedido.EN_CAMINO, EstadoPedido.ENTREGADO);
    private static final int MESES = 6;
    private static final int TOP = 5;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DashboardRepository dashboardRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductorRepository productorRepository;
    private final CalificacionRepository calificacionRepository;

    // ---------- Dashboards ----------

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> productor(String email) {
        Optional<Productor> productorOpt = productorRepository.findByUsuarioEmail(email);
        if (productorOpt.isEmpty()) {
            return respuesta("Perfil de productor no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        Long productorId = productorOpt.get().getId();
        List<DashboardRepository.ConteoEstado> conteo = dashboardRepository.conteoPorEstado(null, productorId);
        DashboardDto.Tarjetas base = tarjetas(conteo);
        Optional<CalificacionRepository.Resumen> resumen =
                calificacionRepository.resumenPorProductores(List.of(productorId)).stream().findFirst();
        Double promedio = resumen.map(CalificacionRepository.Resumen::getPromedio)
                .map(p -> Math.round(p * 10) / 10.0).orElse(null);
        DashboardDto.Tarjetas tarjetas = new DashboardDto.Tarjetas(base.getMonto(), base.getPedidosTotales(),
                base.getPedidosVenta(), base.getTicketPromedio(), promedio,
                resumen.map(CalificacionRepository.Resumen::getTotal).orElse(0L));

        DashboardDto dto = DashboardDto.builder()
                .tarjetas(tarjetas)
                .porEstado(porEstado(conteo))
                .porMes(porMes(null, productorId))
                .topProductos(topProductos(null, productorId))
                .build();
        return respuesta("Dashboard del productor", dto, false, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> comprador(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return respuesta("Usuario no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        Long compradorId = usuarioOpt.get().getId();
        List<DashboardRepository.ConteoEstado> conteo = dashboardRepository.conteoPorEstado(compradorId, null);
        DashboardDto dto = DashboardDto.builder()
                .tarjetas(tarjetas(conteo))
                .porEstado(porEstado(conteo))
                .porMes(porMes(compradorId, null))
                .topProductos(topProductos(compradorId, null))
                .topProductores(topProductores(compradorId))
                .build();
        return respuesta("Dashboard del comprador", dto, false, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> admin() {
        List<DashboardRepository.ConteoEstado> conteo = dashboardRepository.conteoPorEstado(null, null);
        DashboardDto dto = DashboardDto.builder()
                .tarjetas(tarjetas(conteo))
                .porEstado(porEstado(conteo))
                .porMes(porMes(null, null))
                .topProductos(topProductos(null, null))
                .topProductores(topProductores(null))
                .porCategoria(dashboardRepository.ventasPorCategoria(ESTADOS_VENTA).stream()
                        .map(c -> new DashboardDto.Categoria(c.getNombre() == null ? "Sin categoría" : c.getNombre(),
                                dinero(c.getMonto())))
                        .toList())
                .usuariosPorRol(dashboardRepository.usuariosPorRol().stream()
                        .map(r -> new DashboardDto.Rol(r.getRol().name(), r.getCantidad()))
                        .toList())
                .build();
        return respuesta("Dashboard del administrador", dto, false, HttpStatus.OK);
    }

    // ---------- CSV ----------

    @Transactional(readOnly = true)
    public ResponseEntity<?> csvProductor(String email, EstadoPedido estado, LocalDate desde, LocalDate hasta) {
        Optional<Productor> productorOpt = productorRepository.findByUsuarioEmail(email);
        if (productorOpt.isEmpty()) {
            return respuesta("Perfil de productor no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        return csv(null, productorOpt.get().getId(), estado, desde, hasta, "pedidos-recibidos");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> csvComprador(String email, EstadoPedido estado, LocalDate desde, LocalDate hasta) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return respuesta("Usuario no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        return csv(usuarioOpt.get().getId(), null, estado, desde, hasta, "mis-pedidos");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> csvAdmin(EstadoPedido estado, LocalDate desde, LocalDate hasta) {
        return csv(null, null, estado, desde, hasta, "pedidos");
    }

    private ResponseEntity<?> csv(Long compradorId, Long productorId, EstadoPedido estado,
                                  LocalDate desde, LocalDate hasta, String nombre) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            return respuesta("La fecha 'desde' no puede ser posterior a 'hasta'", null, true, HttpStatus.BAD_REQUEST);
        }
        List<Pedido> pedidos = dashboardRepository.pedidosParaCsv(compradorId, productorId, estado,
                desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay()); // "hasta" es inclusivo

        StringBuilder sb = new StringBuilder();
        fila(sb, "Pedido", "Fecha", "Estado", "Comprador", "Productor", "Productos", "Total", "Fecha de entrega",
                "Dirección");
        for (Pedido p : pedidos) {
            String productos = p.getDetalles().stream().map(DashboardService::describir)
                    .collect(Collectors.joining("; "));
            fila(sb, String.valueOf(p.getId()), formatear(p.getCreatedAt()), p.getEstado().name(),
                    p.getComprador().getNombre(), p.getProductor().getNombreFinca(), productos,
                    p.getTotal().toPlainString(), formatear(p.getFechaEntrega()), p.getDireccionEntrega());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // BOM: sin él, Excel abre el archivo como ANSI y rompe los acentos.
        out.writeBytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        out.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));

        String archivo = nombre + "-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo).build().toString())
                .body(out.toByteArray());
    }

    private static String describir(DetallePedido d) {
        return d.getProducto().getNombre() + " x " + d.getCantidad().stripTrailingZeros().toPlainString()
                + " " + d.getProducto().getUnidad();
    }

    private static String formatear(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    private static void fila(StringBuilder sb, String... valores) {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapar(valores[i]));
        }
        sb.append("\r\n");
    }

    /**
     * Comillas si el valor trae coma, comillas o salto de línea. Si un texto empieza con = + - @
     * se le antepone un apóstrofo para que Excel no lo ejecute como fórmula (inyección de CSV).
     */
    static String escapar(String valor) {
        if (valor == null) return "";
        String v = valor;
        boolean esNumero = v.matches("-?\\d+(\\.\\d+)?");
        if (!v.isEmpty() && !esNumero && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    // ---------- Auxiliares ----------

    private DashboardDto.Tarjetas tarjetas(List<DashboardRepository.ConteoEstado> conteo) {
        long totales = 0;
        long venta = 0;
        BigDecimal monto = BigDecimal.ZERO;
        for (DashboardRepository.ConteoEstado c : conteo) {
            totales += c.getCantidad();
            if (ESTADOS_VENTA.contains(c.getEstado())) {
                venta += c.getCantidad();
                monto = monto.add(c.getMonto());
            }
        }
        BigDecimal ticket = venta == 0 ? dinero(null)
                : monto.divide(BigDecimal.valueOf(venta), 2, RoundingMode.HALF_UP);
        return new DashboardDto.Tarjetas(dinero(monto), totales, venta, ticket, null, null);
    }

    /** Siempre devuelve los 5 estados, en el orden del flujo, aunque alguno tenga 0. */
    private List<DashboardDto.Estado> porEstado(List<DashboardRepository.ConteoEstado> conteo) {
        Map<EstadoPedido, DashboardRepository.ConteoEstado> mapa = new EnumMap<>(EstadoPedido.class);
        conteo.forEach(c -> mapa.put(c.getEstado(), c));
        List<DashboardDto.Estado> lista = new ArrayList<>();
        for (EstadoPedido e : EstadoPedido.values()) {
            DashboardRepository.ConteoEstado c = mapa.get(e);
            lista.add(new DashboardDto.Estado(e.name(), c == null ? 0 : c.getCantidad(),
                    dinero(c == null ? null : c.getMonto())));
        }
        return lista;
    }

    /** Últimos 6 meses (incluido el actual); los meses sin ventas salen en 0 para que la gráfica no salte. */
    private List<DashboardDto.Mes> porMes(Long compradorId, Long productorId) {
        YearMonth actual = YearMonth.now();
        YearMonth inicio = actual.minusMonths(MESES - 1);
        Map<YearMonth, DashboardRepository.VentaMes> mapa = new HashMap<>();
        dashboardRepository.ventasPorMes(compradorId, productorId, ESTADOS_VENTA, inicio.atDay(1).atStartOfDay())
                .forEach(v -> mapa.put(YearMonth.of(v.getAnio(), v.getMes()), v));
        List<DashboardDto.Mes> lista = new ArrayList<>();
        for (YearMonth m = inicio; !m.isAfter(actual); m = m.plusMonths(1)) {
            DashboardRepository.VentaMes v = mapa.get(m);
            lista.add(new DashboardDto.Mes(m.toString(), dinero(v == null ? null : v.getMonto()),
                    v == null ? 0 : v.getPedidos()));
        }
        return lista;
    }

    private List<DashboardDto.Producto> topProductos(Long compradorId, Long productorId) {
        return dashboardRepository.topProductos(compradorId, productorId, ESTADOS_VENTA, PageRequest.of(0, TOP))
                .stream()
                .map(p -> new DashboardDto.Producto(p.getId(), p.getNombre(), p.getUnidad(),
                        p.getCantidad().stripTrailingZeros(), dinero(p.getMonto())))
                .toList();
    }

    private List<DashboardDto.Productor> topProductores(Long compradorId) {
        return dashboardRepository.topProductores(compradorId, null, ESTADOS_VENTA, PageRequest.of(0, TOP))
                .stream()
                .map(p -> new DashboardDto.Productor(p.getId(), p.getNombre(), p.getPedidos(), dinero(p.getMonto())))
                .toList();
    }

    private static BigDecimal dinero(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    private ResponseEntity<ApiResponse> respuesta(String msg, Object data, boolean error, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse(msg, data, error, status), status);
    }
}
