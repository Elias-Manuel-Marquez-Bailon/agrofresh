package com.example.backend.pedido.controller;

import com.example.backend.kernel.ApiResponse;
import com.example.backend.pedido.dtos.CrearPedidoDto;
import com.example.backend.pedido.dtos.ItemPedidoDto;
import com.example.backend.pedido.dtos.PedidoResponseDto;
import com.example.backend.pedido.model.DetallePedido;
import com.example.backend.pedido.model.EstadoPedido;
import com.example.backend.pedido.model.Pedido;
import com.example.backend.pedido.model.PedidoRepository;
import com.example.backend.producto.dtos.PaginaDto;
import com.example.backend.producto.model.Producto;
import com.example.backend.producto.model.ProductoRepository;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import com.example.backend.usuario.model.Rol;
import com.example.backend.usuario.model.Usuario;
import com.example.backend.usuario.model.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PedidoService {

    private static final int TAMANIO_MAX_PAGINA = 50;

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductorRepository productorRepository;

    // ---------- Crear (comprador) ----------

    /**
     * Convierte el carrito en pedidos: uno por cada productor. Valida stock pero NO lo descuenta;
     * el stock se descuenta cuando el productor acepta el pedido.
     */
    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> crear(String email, CrearPedidoDto dto) {
        Optional<Usuario> compradorOpt = usuarioRepository.findByEmail(email);
        if (compradorOpt.isEmpty()) {
            return respuesta("Usuario no encontrado", null, true, HttpStatus.NOT_FOUND);
        }

        // Si el mismo producto viene dos veces, se suman las cantidades.
        Map<Long, BigDecimal> cantidades = new LinkedHashMap<>();
        for (ItemPedidoDto item : dto.getItems()) {
            cantidades.merge(item.getProductoId(), item.getCantidad(), BigDecimal::add);
        }

        Map<Long, Producto> productos = productoRepository.findAllById(cantidades.keySet()).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        for (Map.Entry<Long, BigDecimal> e : cantidades.entrySet()) {
            Producto producto = productos.get(e.getKey());
            if (producto == null || !producto.getActivo()) {
                return respuesta("Un producto del carrito ya no está disponible", null, true, HttpStatus.NOT_FOUND);
            }
            if (e.getValue().compareTo(producto.getStock()) > 0) {
                return respuesta("Stock insuficiente de " + producto.getNombre() + " (disponible: "
                        + producto.getStock().stripTrailingZeros().toPlainString() + " " + producto.getUnidad() + ")",
                        null, true, HttpStatus.CONFLICT);
            }
        }

        // Agrupar por productor: cada pedido pertenece a un solo productor.
        Map<Long, Pedido> pedidosPorProductor = new LinkedHashMap<>();
        for (Map.Entry<Long, BigDecimal> e : cantidades.entrySet()) {
            Producto producto = productos.get(e.getKey());
            Pedido pedido = pedidosPorProductor.computeIfAbsent(producto.getProductor().getId(),
                    id -> Pedido.builder()
                            .comprador(compradorOpt.get())
                            .productor(producto.getProductor())
                            .estado(EstadoPedido.PENDIENTE)
                            .direccionEntrega(dto.getDireccionEntrega().trim())
                            .total(BigDecimal.ZERO)
                            .build());
            pedido.agregarDetalle(DetallePedido.builder()
                    .producto(producto)
                    .cantidad(e.getValue())
                    .precioUnitario(producto.getPrecio())
                    .build());
            pedido.setTotal(pedido.getTotal().add(producto.getPrecio().multiply(e.getValue())));
        }

        List<PedidoResponseDto> creados = new ArrayList<>();
        for (Pedido pedido : pedidosPorProductor.values()) {
            pedido.setTotal(pedido.getTotal().setScale(2, RoundingMode.HALF_UP));
            creados.add(PedidoResponseDto.from(pedidoRepository.save(pedido)));
        }

        String msg = creados.size() == 1 ? "Pedido creado correctamente"
                : "Se crearon " + creados.size() + " pedidos (uno por productor)";
        return respuesta(msg, creados, false, HttpStatus.CREATED);
    }

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> misPedidos(String email, EstadoPedido estado, int pagina, int tamanio) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return respuesta("Usuario no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        return pagina("Mis pedidos", usuarioOpt.get().getId(), null, estado, pagina, tamanio);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> recibidos(String email, EstadoPedido estado, int pagina, int tamanio) {
        Optional<Productor> productorOpt = productorRepository.findByUsuarioEmail(email);
        if (productorOpt.isEmpty()) {
            return respuesta("Perfil de productor no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        return pagina("Pedidos recibidos", null, productorOpt.get().getId(), estado, pagina, tamanio);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> todos(EstadoPedido estado, int pagina, int tamanio) {
        return pagina("Todos los pedidos", null, null, estado, pagina, tamanio);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> detalle(String email, Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (usuarioOpt.isEmpty() || pedidoOpt.isEmpty()) {
            return respuesta("Pedido no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        Usuario usuario = usuarioOpt.get();
        Pedido pedido = pedidoOpt.get();
        if (usuario.getRol() != Rol.ADMIN && !esComprador(usuario, pedido) && !esProductor(usuario, pedido)) {
            return respuesta("No tienes permiso para ver este pedido", null, true, HttpStatus.FORBIDDEN);
        }
        return respuesta("Detalle del pedido", PedidoResponseDto.from(pedido), false, HttpStatus.OK);
    }

    // ---------- Cambio de estado ----------

    /**
     * Reglas:
     * - Productor: PENDIENTE -> ACEPTADO (descuenta stock) -> EN_CAMINO -> ENTREGADO.
     * - Productor: cancela si está PENDIENTE o ACEPTADO (si estaba ACEPTADO, repone el stock).
     * - Comprador: solo cancela si está PENDIENTE.
     */
    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> cambiarEstado(String email, Long id, EstadoPedido nuevo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        Optional<Pedido> pedidoOpt = pedidoRepository.findByIdParaActualizar(id);
        if (usuarioOpt.isEmpty() || pedidoOpt.isEmpty()) {
            return respuesta("Pedido no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        Usuario usuario = usuarioOpt.get();
        Pedido pedido = pedidoOpt.get();
        boolean esComprador = esComprador(usuario, pedido);
        boolean esProductor = esProductor(usuario, pedido);
        if (!esComprador && !esProductor) {
            return respuesta("No tienes permiso para modificar este pedido", null, true, HttpStatus.FORBIDDEN);
        }

        EstadoPedido actual = pedido.getEstado();
        if (!esTransicionValida(actual, nuevo)) {
            return respuesta("No se puede pasar de " + actual + " a " + nuevo, null, true, HttpStatus.BAD_REQUEST);
        }
        if (esComprador && !(nuevo == EstadoPedido.CANCELADO && actual == EstadoPedido.PENDIENTE)) {
            return respuesta("Como comprador solo puedes cancelar pedidos pendientes", null, true,
                    HttpStatus.FORBIDDEN);
        }

        if (nuevo == EstadoPedido.ACEPTADO) {
            ResponseEntity<ApiResponse> error = descontarStock(pedido);
            if (error != null) return error;
        } else if (nuevo == EstadoPedido.CANCELADO && actual == EstadoPedido.ACEPTADO) {
            reponerStock(pedido);
        }

        pedido.setEstado(nuevo);
        pedido = pedidoRepository.save(pedido);
        return respuesta("Pedido " + nuevo.name().toLowerCase().replace('_', ' '),
                PedidoResponseDto.from(pedido), false, HttpStatus.OK);
    }

    static boolean esTransicionValida(EstadoPedido actual, EstadoPedido nuevo) {
        return switch (actual) {
            case PENDIENTE -> nuevo == EstadoPedido.ACEPTADO || nuevo == EstadoPedido.CANCELADO;
            case ACEPTADO -> nuevo == EstadoPedido.EN_CAMINO || nuevo == EstadoPedido.CANCELADO;
            case EN_CAMINO -> nuevo == EstadoPedido.ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }

    // ---------- Auxiliares ----------

    /**
     * Primero bloquea y valida TODOS los productos y solo después descuenta, para no dejar
     * un descuento a medias si uno no alcanza. Se bloquean en orden de id para evitar deadlocks.
     */
    private ResponseEntity<ApiResponse> descontarStock(Pedido pedido) {
        List<DetallePedido> detalles = ordenadosPorProducto(pedido);
        List<Producto> bloqueados = new ArrayList<>();
        for (DetallePedido d : detalles) {
            Optional<Producto> productoOpt = productoRepository.findByIdParaActualizar(d.getProducto().getId());
            if (productoOpt.isEmpty()) {
                return respuesta("Un producto del pedido ya no existe", null, true, HttpStatus.CONFLICT);
            }
            Producto producto = productoOpt.get();
            if (d.getCantidad().compareTo(producto.getStock()) > 0) {
                return respuesta("Stock insuficiente de " + producto.getNombre() + " para aceptar el pedido",
                        null, true, HttpStatus.CONFLICT);
            }
            bloqueados.add(producto);
        }
        for (int i = 0; i < detalles.size(); i++) {
            Producto producto = bloqueados.get(i);
            producto.setStock(producto.getStock().subtract(detalles.get(i).getCantidad()));
            productoRepository.save(producto);
        }
        return null;
    }

    private void reponerStock(Pedido pedido) {
        for (DetallePedido d : ordenadosPorProducto(pedido)) {
            productoRepository.findByIdParaActualizar(d.getProducto().getId()).ifPresent(producto -> {
                producto.setStock(producto.getStock().add(d.getCantidad()));
                productoRepository.save(producto);
            });
        }
    }

    private List<DetallePedido> ordenadosPorProducto(Pedido pedido) {
        return pedido.getDetalles().stream()
                .sorted(Comparator.comparing(d -> d.getProducto().getId()))
                .toList();
    }

    private boolean esComprador(Usuario usuario, Pedido pedido) {
        return pedido.getComprador().getId().equals(usuario.getId());
    }

    private boolean esProductor(Usuario usuario, Pedido pedido) {
        return pedido.getProductor().getUsuario() != null
                && pedido.getProductor().getUsuario().getId().equals(usuario.getId());
    }

    private ResponseEntity<ApiResponse> pagina(String msg, Long compradorId, Long productorId, EstadoPedido estado,
                                               int pagina, int tamanio) {
        if (pagina < 0) pagina = 0;
        if (tamanio < 1 || tamanio > TAMANIO_MAX_PAGINA) tamanio = 10;
        Page<Pedido> page = pedidoRepository.buscar(compradorId, productorId, estado,
                PageRequest.of(pagina, tamanio, Sort.by(Sort.Direction.DESC, "id")));
        PaginaDto<PedidoResponseDto> dto = new PaginaDto<>(
                page.getContent().stream().map(PedidoResponseDto::from).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        return respuesta(msg, dto, false, HttpStatus.OK);
    }

    private ResponseEntity<ApiResponse> respuesta(String msg, Object data, boolean error, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse(msg, data, error, status), status);
    }
}
