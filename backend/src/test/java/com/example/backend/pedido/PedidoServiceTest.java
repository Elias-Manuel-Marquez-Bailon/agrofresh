package com.example.backend.pedido;

import com.example.backend.kernel.ApiResponse;
import com.example.backend.pedido.controller.PedidoService;
import com.example.backend.pedido.dtos.CrearPedidoDto;
import com.example.backend.pedido.dtos.ItemPedidoDto;
import com.example.backend.pedido.dtos.PedidoResponseDto;
import com.example.backend.pedido.model.DetallePedido;
import com.example.backend.pedido.model.EstadoPedido;
import com.example.backend.pedido.model.Pedido;
import com.example.backend.pedido.model.PedidoRepository;
import com.example.backend.producto.model.Producto;
import com.example.backend.producto.model.ProductoRepository;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import com.example.backend.usuario.model.Rol;
import com.example.backend.usuario.model.Usuario;
import com.example.backend.usuario.model.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProductorRepository productorRepository;
    @InjectMocks
    private PedidoService pedidoService;

    private final Usuario ana = Usuario.builder().id(1L).nombre("Ana").email("ana@correo.com")
            .rol(Rol.COMPRADOR).build();
    private final Usuario intrusa = Usuario.builder().id(9L).nombre("Eva").email("eva@correo.com")
            .rol(Rol.COMPRADOR).build();
    private final Usuario usuarioJuan = Usuario.builder().id(2L).nombre("Juan").email("juan@finca.com")
            .rol(Rol.PRODUCTOR).build();
    private final Usuario usuarioLuis = Usuario.builder().id(3L).nombre("Luis").email("luis@finca.com")
            .rol(Rol.PRODUCTOR).build();
    private final Productor juan = Productor.builder().id(1L).usuario(usuarioJuan).nombreFinca("La Esperanza").build();
    private final Productor luis = Productor.builder().id(2L).usuario(usuarioLuis).nombreFinca("El Roble").build();

    private Producto producto(Long id, Productor p, String precio, String stock) {
        return Producto.builder().id(id).productor(p).nombre("Producto " + id).unidad("kg")
                .precio(new BigDecimal(precio)).stock(new BigDecimal(stock)).activo(true).build();
    }

    private ItemPedidoDto item(Long productoId, String cantidad) {
        return ItemPedidoDto.builder().productoId(productoId).cantidad(new BigDecimal(cantidad)).build();
    }

    private Pedido pedido(EstadoPedido estado, Producto producto, String cantidad) {
        Pedido p = Pedido.builder().id(100L).comprador(ana).productor(producto.getProductor()).estado(estado)
                .direccionEntrega("Calle 1").total(BigDecimal.TEN).build();
        p.agregarDetalle(DetallePedido.builder().producto(producto).cantidad(new BigDecimal(cantidad))
                .precioUnitario(producto.getPrecio()).build());
        return p;
    }

    private void prepararCambio(Usuario usuario, Pedido pedido) {
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(pedidoRepository.findByIdParaActualizar(100L)).thenReturn(Optional.of(pedido));
    }

    // ---------- Crear ----------

    @Test
    @SuppressWarnings("unchecked")
    void crear_separaElCarritoEnUnPedidoPorProductorYCalculaTotales() {
        Producto mango = producto(10L, juan, "25.50", "100");
        Producto limon = producto(11L, juan, "10.00", "50");
        Producto papa = producto(20L, luis, "8.00", "30");
        when(usuarioRepository.findByEmail(ana.getEmail())).thenReturn(Optional.of(ana));
        when(productoRepository.findAllById(any())).thenReturn(List.of(mango, limon, papa));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        CrearPedidoDto dto = CrearPedidoDto.builder().direccionEntrega(" Calle 1 ")
                .items(List.of(item(10L, "2"), item(11L, "1.5"), item(20L, "3"))).build();
        ResponseEntity<ApiResponse> res = pedidoService.crear(ana.getEmail(), dto);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        List<PedidoResponseDto> creados = (List<PedidoResponseDto>) res.getBody().getData();
        assertThat(creados).hasSize(2);
        assertThat(creados.get(0).productorId()).isEqualTo(1L);
        assertThat(creados.get(0).total()).isEqualByComparingTo("66.00"); // 2*25.50 + 1.5*10
        assertThat(creados.get(0).estado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(creados.get(0).direccionEntrega()).isEqualTo("Calle 1");
        assertThat(creados.get(1).productorId()).isEqualTo(2L);
        assertThat(creados.get(1).total()).isEqualByComparingTo("24.00");
        // Crear el pedido no descuenta stock.
        assertThat(mango.getStock()).isEqualByComparingTo("100");
    }

    @Test
    void crear_sumaCantidadesDelMismoProductoYRechazaSiPasaElStock() {
        Producto mango = producto(10L, juan, "25.50", "5");
        when(usuarioRepository.findByEmail(ana.getEmail())).thenReturn(Optional.of(ana));
        when(productoRepository.findAllById(any())).thenReturn(List.of(mango));

        CrearPedidoDto dto = CrearPedidoDto.builder().direccionEntrega("Calle 1")
                .items(List.of(item(10L, "3"), item(10L, "3"))).build();
        ResponseEntity<ApiResponse> res = pedidoService.crear(ana.getEmail(), dto);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crear_rechazaProductoInactivo() {
        Producto mango = producto(10L, juan, "25.50", "100");
        mango.setActivo(false);
        when(usuarioRepository.findByEmail(ana.getEmail())).thenReturn(Optional.of(ana));
        when(productoRepository.findAllById(any())).thenReturn(List.of(mango));

        CrearPedidoDto dto = CrearPedidoDto.builder().direccionEntrega("Calle 1")
                .items(List.of(item(10L, "1"))).build();

        assertThat(pedidoService.crear(ana.getEmail(), dto).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(pedidoRepository, never()).save(any());
    }

    // ---------- Aceptar (descuenta stock) ----------

    @Test
    void aceptar_descuentaStock() {
        Producto mango = producto(10L, juan, "25.50", "10");
        Pedido pedido = pedido(EstadoPedido.PENDIENTE, mango, "4");
        prepararCambio(usuarioJuan, pedido);
        when(productoRepository.findByIdParaActualizar(10L)).thenReturn(Optional.of(mango));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<ApiResponse> res = pedidoService.cambiarEstado(usuarioJuan.getEmail(), 100L,
                EstadoPedido.ACEPTADO);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ACEPTADO);
        assertThat(mango.getStock()).isEqualByComparingTo("6");
        verify(productoRepository).save(mango);
    }

    @Test
    void aceptar_sinStockSuficienteNoCambiaNada() {
        Producto mango = producto(10L, juan, "25.50", "3");
        Pedido pedido = pedido(EstadoPedido.PENDIENTE, mango, "4");
        prepararCambio(usuarioJuan, pedido);
        when(productoRepository.findByIdParaActualizar(10L)).thenReturn(Optional.of(mango));

        ResponseEntity<ApiResponse> res = pedidoService.cambiarEstado(usuarioJuan.getEmail(), 100L,
                EstadoPedido.ACEPTADO);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(mango.getStock()).isEqualByComparingTo("3");
        verify(productoRepository, never()).save(any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void aceptar_noDescuentaNadaSiUnoDeVariosProductosNoAlcanza() {
        Producto mango = producto(10L, juan, "25.50", "10");
        Producto limon = producto(11L, juan, "10.00", "1");
        Pedido pedido = pedido(EstadoPedido.PENDIENTE, mango, "2");
        pedido.agregarDetalle(DetallePedido.builder().producto(limon).cantidad(new BigDecimal("5"))
                .precioUnitario(limon.getPrecio()).build());
        prepararCambio(usuarioJuan, pedido);
        when(productoRepository.findByIdParaActualizar(10L)).thenReturn(Optional.of(mango));
        when(productoRepository.findByIdParaActualizar(11L)).thenReturn(Optional.of(limon));

        ResponseEntity<ApiResponse> res = pedidoService.cambiarEstado(usuarioJuan.getEmail(), 100L,
                EstadoPedido.ACEPTADO);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(mango.getStock()).isEqualByComparingTo("10");
        verify(productoRepository, never()).save(any());
    }

    // ---------- Cancelar ----------

    @Test
    void cancelarAceptado_porProductorReponeStock() {
        Producto mango = producto(10L, juan, "25.50", "6");
        Pedido pedido = pedido(EstadoPedido.ACEPTADO, mango, "4");
        prepararCambio(usuarioJuan, pedido);
        when(productoRepository.findByIdParaActualizar(10L)).thenReturn(Optional.of(mango));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<ApiResponse> res = pedidoService.cambiarEstado(usuarioJuan.getEmail(), 100L,
                EstadoPedido.CANCELADO);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(mango.getStock()).isEqualByComparingTo("10");
    }

    @Test
    void cancelarPendiente_porCompradorNoTocaStock() {
        Producto mango = producto(10L, juan, "25.50", "10");
        Pedido pedido = pedido(EstadoPedido.PENDIENTE, mango, "4");
        prepararCambio(ana, pedido);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<ApiResponse> res = pedidoService.cambiarEstado(ana.getEmail(), 100L, EstadoPedido.CANCELADO);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        verify(productoRepository, never()).findByIdParaActualizar(any());
    }

    @Test
    void comprador_noPuedeCancelarUnPedidoAceptado() {
        Pedido pedido = pedido(EstadoPedido.ACEPTADO, producto(10L, juan, "25.50", "10"), "4");
        prepararCambio(ana, pedido);

        ResponseEntity<ApiResponse> res = pedidoService.cambiarEstado(ana.getEmail(), 100L, EstadoPedido.CANCELADO);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ACEPTADO);
    }

    @Test
    void comprador_noPuedeAceptarSuPropioPedido() {
        Pedido pedido = pedido(EstadoPedido.PENDIENTE, producto(10L, juan, "25.50", "10"), "4");
        prepararCambio(ana, pedido);

        ResponseEntity<ApiResponse> res = pedidoService.cambiarEstado(ana.getEmail(), 100L, EstadoPedido.ACEPTADO);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(productoRepository, never()).findByIdParaActualizar(any());
    }

    // ---------- Transiciones y permisos ----------

    @Test
    void flujoCompleto_aceptadoEnCaminoEntregado() {
        Pedido pedido = pedido(EstadoPedido.ACEPTADO, producto(10L, juan, "25.50", "10"), "4");
        prepararCambio(usuarioJuan, pedido);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(pedidoService.cambiarEstado(usuarioJuan.getEmail(), 100L, EstadoPedido.EN_CAMINO)
                .getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pedidoService.cambiarEstado(usuarioJuan.getEmail(), 100L, EstadoPedido.ENTREGADO)
                .getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ENTREGADO);
        verify(pedidoRepository, times(2)).save(pedido);
    }

    @Test
    void transicionInvalida_devuelve400() {
        Pedido pedido = pedido(EstadoPedido.PENDIENTE, producto(10L, juan, "25.50", "10"), "4");
        prepararCambio(usuarioJuan, pedido);

        ResponseEntity<ApiResponse> res = pedidoService.cambiarEstado(usuarioJuan.getEmail(), 100L,
                EstadoPedido.ENTREGADO);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
    }

    @Test
    void pedidoEntregadoOCancelado_yaNoCambia() {
        Pedido pedido = pedido(EstadoPedido.ENTREGADO, producto(10L, juan, "25.50", "10"), "4");
        prepararCambio(usuarioJuan, pedido);

        assertThat(pedidoService.cambiarEstado(usuarioJuan.getEmail(), 100L, EstadoPedido.CANCELADO)
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void otroProductor_noPuedeModificarElPedido() {
        Pedido pedido = pedido(EstadoPedido.PENDIENTE, producto(10L, juan, "25.50", "10"), "4");
        prepararCambio(usuarioLuis, pedido);

        ResponseEntity<ApiResponse> res = pedidoService.cambiarEstado(usuarioLuis.getEmail(), 100L,
                EstadoPedido.ACEPTADO);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void detalle_soloLoVenLasPartesDelPedidoOElAdmin() {
        Pedido pedido = pedido(EstadoPedido.PENDIENTE, producto(10L, juan, "25.50", "10"), "4");
        Usuario admin = Usuario.builder().id(50L).email("admin@agrofresh.com").rol(Rol.ADMIN).build();
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByEmail(intrusa.getEmail())).thenReturn(Optional.of(intrusa));
        when(usuarioRepository.findByEmail(ana.getEmail())).thenReturn(Optional.of(ana));
        when(usuarioRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        assertThat(pedidoService.detalle(intrusa.getEmail(), 100L).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(pedidoService.detalle(ana.getEmail(), 100L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pedidoService.detalle(admin.getEmail(), 100L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void misPedidos_filtraPorElCompradorDelToken() {
        when(usuarioRepository.findByEmail(ana.getEmail())).thenReturn(Optional.of(ana));
        when(pedidoRepository.buscar(any(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        pedidoService.misPedidos(ana.getEmail(), EstadoPedido.PENDIENTE, 0, 10);

        ArgumentCaptor<Long> comprador = ArgumentCaptor.forClass(Long.class);
        verify(pedidoRepository).buscar(comprador.capture(), any(), any(), any());
        assertThat(comprador.getValue()).isEqualTo(1L);
    }
}
