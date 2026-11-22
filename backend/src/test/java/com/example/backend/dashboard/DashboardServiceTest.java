package com.example.backend.dashboard;

import com.example.backend.calificacion.model.CalificacionRepository;
import com.example.backend.dashboard.controller.DashboardService;
import com.example.backend.dashboard.dtos.DashboardDto;
import com.example.backend.dashboard.model.DashboardRepository;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.pedido.model.DetallePedido;
import com.example.backend.pedido.model.EstadoPedido;
import com.example.backend.pedido.model.Pedido;
import com.example.backend.producto.model.Producto;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import com.example.backend.usuario.model.Rol;
import com.example.backend.usuario.model.Usuario;
import com.example.backend.usuario.model.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardRepository dashboardRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProductorRepository productorRepository;
    @Mock
    private CalificacionRepository calificacionRepository;
    @InjectMocks
    private DashboardService dashboardService;

    private final Usuario ana = Usuario.builder().id(1L).nombre("Ana").email("ana@correo.com")
            .rol(Rol.COMPRADOR).build();
    private final Usuario usuarioJuan = Usuario.builder().id(2L).nombre("Juan").email("juan@finca.com")
            .rol(Rol.PRODUCTOR).build();
    private final Productor juan = Productor.builder().id(7L).usuario(usuarioJuan).nombreFinca("La Esperanza").build();

    private DashboardRepository.ConteoEstado conteo(EstadoPedido estado, long cantidad, String monto) {
        DashboardRepository.ConteoEstado c = mock(DashboardRepository.ConteoEstado.class);
        when(c.getEstado()).thenReturn(estado);
        when(c.getCantidad()).thenReturn(cantidad);
        when(c.getMonto()).thenReturn(new BigDecimal(monto));
        return c;
    }

    private static DashboardDto datos(ResponseEntity<ApiResponse> res) {
        return (DashboardDto) res.getBody().getData();
    }

    @Test
    void productor_sumaComoVentaAceptadosEnCaminoYEntregados() {
        when(productorRepository.findByUsuarioEmail(usuarioJuan.getEmail())).thenReturn(Optional.of(juan));
        List<DashboardRepository.ConteoEstado> conteos = List.of(
                conteo(EstadoPedido.PENDIENTE, 2, "500.00"),
                conteo(EstadoPedido.ACEPTADO, 1, "100.00"),
                conteo(EstadoPedido.EN_CAMINO, 1, "50.00"),
                conteo(EstadoPedido.ENTREGADO, 2, "150.00"),
                conteo(EstadoPedido.CANCELADO, 3, "900.00"));
        when(dashboardRepository.conteoPorEstado(null, 7L)).thenReturn(conteos);
        CalificacionRepository.Resumen resumen = mock(CalificacionRepository.Resumen.class);
        when(resumen.getPromedio()).thenReturn(4.666);
        when(resumen.getTotal()).thenReturn(3L);
        when(calificacionRepository.resumenPorProductores(List.of(7L))).thenReturn(List.of(resumen));

        ResponseEntity<ApiResponse> res = dashboardService.productor(usuarioJuan.getEmail());

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        DashboardDto.Tarjetas t = datos(res).getTarjetas();
        assertThat(t.getMonto()).isEqualByComparingTo("300.00");   // 100 + 50 + 150
        assertThat(t.getPedidosVenta()).isEqualTo(4);
        assertThat(t.getPedidosTotales()).isEqualTo(9);
        assertThat(t.getTicketPromedio()).isEqualByComparingTo("75.00");
        assertThat(t.getCalificacionPromedio()).isEqualTo(4.7);
        assertThat(t.getTotalCalificaciones()).isEqualTo(3L);
        // Solo consulta los datos de su propio perfil de productor.
        verify(dashboardRepository).ventasPorMes(isNull(), eq(7L), anyCollection(), any());
        verify(dashboardRepository).topProductos(isNull(), eq(7L), anyCollection(), any());
        assertThat(datos(res).getPorCategoria()).isNull();
    }

    @Test
    void porEstado_devuelveLosCincoEstadosEnOrdenAunqueNoHayaPedidos() {
        when(usuarioRepository.findByEmail(ana.getEmail())).thenReturn(Optional.of(ana));
        DashboardRepository.ConteoEstado entregados = conteo(EstadoPedido.ENTREGADO, 1, "80.00");
        when(dashboardRepository.conteoPorEstado(1L, null)).thenReturn(List.of(entregados));

        DashboardDto dto = datos(dashboardService.comprador(ana.getEmail()));

        assertThat(dto.getPorEstado()).extracting(DashboardDto.Estado::estado)
                .containsExactly("PENDIENTE", "ACEPTADO", "EN_CAMINO", "ENTREGADO", "CANCELADO");
        assertThat(dto.getPorEstado().get(0).cantidad()).isZero();
        assertThat(dto.getPorEstado().get(3).cantidad()).isEqualTo(1);
    }

    @Test
    void porMes_rellenaConCeroLosUltimosSeisMeses() {
        when(usuarioRepository.findByEmail(ana.getEmail())).thenReturn(Optional.of(ana));
        YearMonth actual = YearMonth.now();
        DashboardRepository.VentaMes venta = mock(DashboardRepository.VentaMes.class);
        when(venta.getAnio()).thenReturn(actual.getYear());
        when(venta.getMes()).thenReturn(actual.getMonthValue());
        when(venta.getMonto()).thenReturn(new BigDecimal("120.5"));
        when(venta.getPedidos()).thenReturn(2L);
        when(dashboardRepository.ventasPorMes(eq(1L), isNull(), anyCollection(), any())).thenReturn(List.of(venta));

        List<DashboardDto.Mes> meses = datos(dashboardService.comprador(ana.getEmail())).getPorMes();

        assertThat(meses).hasSize(6);
        assertThat(meses.get(0).periodo()).isEqualTo(actual.minusMonths(5).toString());
        assertThat(meses.get(0).monto()).isEqualByComparingTo("0");
        assertThat(meses.get(5).periodo()).isEqualTo(actual.toString());
        assertThat(meses.get(5).monto()).isEqualByComparingTo("120.50");
        assertThat(meses.get(5).pedidos()).isEqualTo(2);
    }

    @Test
    void productor_sinPerfilDevuelve404() {
        when(productorRepository.findByUsuarioEmail("x@x.com")).thenReturn(Optional.empty());

        assertThat(dashboardService.productor("x@x.com").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void admin_incluyeCategoriasYUsuariosPorRol() {
        DashboardRepository.VentaCategoria sinCategoria = mock(DashboardRepository.VentaCategoria.class);
        when(sinCategoria.getNombre()).thenReturn(null);
        when(sinCategoria.getMonto()).thenReturn(new BigDecimal("10"));
        when(dashboardRepository.ventasPorCategoria(DashboardService.ESTADOS_VENTA)).thenReturn(List.of(sinCategoria));
        DashboardRepository.ConteoRol rol = mock(DashboardRepository.ConteoRol.class);
        when(rol.getRol()).thenReturn(Rol.PRODUCTOR);
        when(rol.getCantidad()).thenReturn(4L);
        when(dashboardRepository.usuariosPorRol()).thenReturn(List.of(rol));

        DashboardDto dto = datos(dashboardService.admin());

        assertThat(dto.getPorCategoria()).containsExactly(
                new DashboardDto.Categoria("Sin categoría", new BigDecimal("10.00")));
        assertThat(dto.getUsuariosPorRol()).containsExactly(new DashboardDto.Rol("PRODUCTOR", 4));
    }

    // ---------- CSV ----------

    @Test
    void csv_generaEncabezadoFilasYBomConAcentos() {
        Producto jitomate = Producto.builder().id(10L).nombre("Jitomate, saladet").unidad("kg").build();
        Pedido pedido = Pedido.builder().id(5L).comprador(ana).productor(juan).estado(EstadoPedido.ENTREGADO)
                .total(new BigDecimal("75.00")).direccionEntrega("Calle Morelos 1")
                .createdAt(LocalDateTime.of(2026, 11, 20, 9, 30)).build();
        pedido.agregarDetalle(DetallePedido.builder().producto(jitomate).cantidad(new BigDecimal("3.00"))
                .precioUnitario(new BigDecimal("25")).build());
        when(dashboardRepository.pedidosParaCsv(null, null, null, null, null)).thenReturn(List.of(pedido));

        ResponseEntity<?> res = dashboardService.csvAdmin(null, null, null);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getHeaders().getContentDisposition().getFilename()).startsWith("pedidos-").endsWith(".csv");
        byte[] bytes = (byte[]) res.getBody();
        assertThat(Arrays.copyOf(bytes, 3)).containsExactly(0xEF, 0xBB, 0xBF);
        String[] lineas = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8).split("\r\n");
        assertThat(lineas[0]).isEqualTo("Pedido,Fecha,Estado,Comprador,Productor,Productos,Total,"
                + "Fecha de entrega,Dirección");
        assertThat(lineas[1]).isEqualTo("5,2026-11-20 09:30,ENTREGADO,Ana,La Esperanza,"
                + "\"Jitomate, saladet x 3 kg\",75.00,,Calle Morelos 1");
    }

    @Test
    void csv_hastaEsInclusivoYFiltraPorElProductorDelToken() {
        when(productorRepository.findByUsuarioEmail(usuarioJuan.getEmail())).thenReturn(Optional.of(juan));

        dashboardService.csvProductor(usuarioJuan.getEmail(), EstadoPedido.ENTREGADO,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        verify(dashboardRepository).pedidosParaCsv(null, 7L, EstadoPedido.ENTREGADO,
                LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.of(2026, 12, 1, 0, 0));
    }

    @Test
    void csv_rangoDeFechasInvertidoDevuelve400() {
        ResponseEntity<?> res = dashboardService.csvAdmin(null, LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(dashboardRepository, never()).pedidosParaCsv(any(), any(), any(), any(), any());
    }

    @Test
    void escapar_protegeContraFormulasYComillas() throws Exception {
        Method escapar = DashboardService.class.getDeclaredMethod("escapar", String.class);
        escapar.setAccessible(true);

        assertThat(escapar.invoke(null, "=HYPERLINK(\"x\")")).isEqualTo("\"'=HYPERLINK(\"\"x\"\")\"");
        assertThat(escapar.invoke(null, "-15.50")).isEqualTo("-15.50");
        assertThat(escapar.invoke(null, "@usuario")).isEqualTo("'@usuario");
        assertThat(escapar.invoke(null, (Object) null)).isEqualTo("");
    }
}
