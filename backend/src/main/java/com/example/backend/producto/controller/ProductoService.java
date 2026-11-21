package com.example.backend.producto.controller;

import com.example.backend.categoria.model.Categoria;
import com.example.backend.categoria.model.CategoriaRepository;
import com.example.backend.kernel.ApiResponse;
import com.example.backend.producto.dtos.PaginaDto;
import com.example.backend.producto.dtos.ProductoDto;
import com.example.backend.producto.dtos.ProductoResponseDto;
import com.example.backend.producto.model.Producto;
import com.example.backend.producto.model.ProductoImagen;
import com.example.backend.producto.model.ProductoImagenRepository;
import com.example.backend.producto.model.ProductoRepository;
import com.example.backend.usuario.model.Productor;
import com.example.backend.usuario.model.ProductorRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductoService {

    static final long TAMANIO_MAX_IMAGEN = 2L * 1024 * 1024; // 2 MB
    private static final int TAMANIO_MAX_PAGINA = 50;

    private final ProductoRepository productoRepository;
    private final ProductoImagenRepository imagenRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductorRepository productorRepository;

    // ---------- Público ----------

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> catalogo(String nombre, Long categoriaId, int pagina, int tamanio) {
        if (pagina < 0) pagina = 0;
        if (tamanio < 1 || tamanio > TAMANIO_MAX_PAGINA) tamanio = 12;
        String filtro = (nombre == null || nombre.isBlank()) ? null : nombre.trim();

        Page<Producto> page = productoRepository.buscarCatalogo(filtro, categoriaId,
                PageRequest.of(pagina, tamanio, Sort.by(Sort.Direction.DESC, "id")));

        PaginaDto<ProductoResponseDto> dto = new PaginaDto<>(convertir(page.getContent()),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        return respuesta("Catálogo de productos", dto, false, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> detalle(Long id) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty() || !productoOpt.get().getActivo()) {
            return respuesta("Producto no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        return respuesta("Detalle del producto", convertir(List.of(productoOpt.get())).get(0), false, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> imagen(Long id) {
        Optional<ProductoImagen> imagenOpt = imagenRepository.findById(id);
        if (imagenOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ProductoImagen img = imagenOpt.get();
        // La URL lleva ?v=<fecha>, así que se puede cachear sin miedo a mostrar una imagen vieja.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.getTipo()))
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(img.getDatos());
    }

    // ---------- Productor ----------

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> misProductos(String email) {
        Optional<Productor> productorOpt = productorRepository.findByUsuarioEmail(email);
        if (productorOpt.isEmpty()) {
            return respuesta("Perfil de productor no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        List<Producto> productos = productoRepository.findDelProductor(productorOpt.get().getId());
        return respuesta("Mis productos", convertir(productos), false, HttpStatus.OK);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> crear(String email, ProductoDto dto) {
        Optional<Productor> productorOpt = productorRepository.findByUsuarioEmail(email);
        if (productorOpt.isEmpty()) {
            return respuesta("Perfil de productor no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(dto.getCategoriaId());
        if (categoriaOpt.isEmpty()) {
            return respuesta("La categoría no existe", null, true, HttpStatus.NOT_FOUND);
        }

        Producto producto = Producto.builder()
                .productor(productorOpt.get())
                .categoria(categoriaOpt.get())
                .activo(true)
                .build();
        copiarDatos(dto, producto);
        producto = productoRepository.save(producto);
        return respuesta("Producto creado correctamente", ProductoResponseDto.from(producto, null),
                false, HttpStatus.CREATED);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> actualizar(String email, Long id, ProductoDto dto) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        ResponseEntity<ApiResponse> error = validarDuenio(email, productoOpt);
        if (error != null) return error;

        Optional<Categoria> categoriaOpt = categoriaRepository.findById(dto.getCategoriaId());
        if (categoriaOpt.isEmpty()) {
            return respuesta("La categoría no existe", null, true, HttpStatus.NOT_FOUND);
        }

        Producto producto = productoOpt.get();
        producto.setCategoria(categoriaOpt.get());
        copiarDatos(dto, producto);
        producto = productoRepository.save(producto);
        return respuesta("Producto actualizado correctamente", convertir(List.of(producto)).get(0),
                false, HttpStatus.OK);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> cambiarEstado(String email, Long id, boolean activo) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        ResponseEntity<ApiResponse> error = validarDuenio(email, productoOpt);
        if (error != null) return error;

        Producto producto = productoOpt.get();
        producto.setActivo(activo);
        producto = productoRepository.save(producto);
        String msg = activo ? "Producto activado" : "Producto desactivado";
        return respuesta(msg, convertir(List.of(producto)).get(0), false, HttpStatus.OK);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> subirImagen(String email, Long id, MultipartFile archivo) throws IOException {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        ResponseEntity<ApiResponse> error = validarDuenio(email, productoOpt);
        if (error != null) return error;

        if (archivo == null || archivo.isEmpty()) {
            return respuesta("Selecciona una imagen", null, true, HttpStatus.BAD_REQUEST);
        }
        if (archivo.getSize() > TAMANIO_MAX_IMAGEN) {
            return respuesta("La imagen no puede pesar más de 2 MB", null, true, HttpStatus.BAD_REQUEST);
        }
        byte[] datos = archivo.getBytes();
        // Se revisa el contenido real del archivo, no solo la extensión o el Content-Type del cliente.
        String tipo = detectarTipo(datos);
        if (tipo == null) {
            return respuesta("Formato no permitido: usa JPG, PNG o WEBP", null, true, HttpStatus.BAD_REQUEST);
        }

        ProductoImagen imagen = ProductoImagen.builder()
                .productoId(id)
                .tipo(tipo)
                .datos(datos)
                .actualizadoEn(LocalDateTime.now())
                .build();
        imagenRepository.save(imagen);
        return respuesta("Imagen guardada correctamente",
                ProductoResponseDto.from(productoOpt.get(), imagen.getActualizadoEn()), false, HttpStatus.OK);
    }

    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<ApiResponse> eliminarImagen(String email, Long id) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        ResponseEntity<ApiResponse> error = validarDuenio(email, productoOpt);
        if (error != null) return error;

        if (imagenRepository.existsById(id)) {
            imagenRepository.deleteById(id);
        }
        return respuesta("Imagen eliminada", ProductoResponseDto.from(productoOpt.get(), null), false, HttpStatus.OK);
    }

    // ---------- Auxiliares ----------

    /** Devuelve una respuesta de error si el producto no existe o no es del productor; null si todo bien. */
    private ResponseEntity<ApiResponse> validarDuenio(String email, Optional<Producto> productoOpt) {
        if (productoOpt.isEmpty()) {
            return respuesta("Producto no encontrado", null, true, HttpStatus.NOT_FOUND);
        }
        Optional<Productor> productorOpt = productorRepository.findByUsuarioEmail(email);
        if (productorOpt.isEmpty()
                || !productoOpt.get().getProductor().getId().equals(productorOpt.get().getId())) {
            return respuesta("Solo puedes modificar tus propios productos", null, true, HttpStatus.FORBIDDEN);
        }
        return null;
    }

    private void copiarDatos(ProductoDto dto, Producto producto) {
        producto.setNombre(dto.getNombre().trim());
        producto.setDescripcion(dto.getDescripcion() == null || dto.getDescripcion().isBlank()
                ? null : dto.getDescripcion().trim());
        producto.setPrecio(dto.getPrecio());
        producto.setUnidad(dto.getUnidad().trim());
        producto.setStock(dto.getStock());
    }

    /** Convierte a DTO consultando de una sola vez qué productos tienen imagen (sin cargar los binarios). */
    private List<ProductoResponseDto> convertir(List<Producto> productos) {
        if (productos.isEmpty()) return List.of();
        Map<Long, LocalDateTime> imagenes = imagenRepository
                .findInfoByProductoIds(productos.stream().map(Producto::getId).toList()).stream()
                .collect(Collectors.toMap(ProductoImagenRepository.ImagenInfo::getProductoId,
                        ProductoImagenRepository.ImagenInfo::getActualizadoEn));
        return productos.stream()
                .map(p -> ProductoResponseDto.from(p, imagenes.get(p.getId())))
                .toList();
    }

    /** Identifica JPG, PNG o WEBP por sus primeros bytes ("firma" del archivo). */
    static String detectarTipo(byte[] d) {
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (d.length >= 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return "image/png";
        }
        if (d.length >= 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private ResponseEntity<ApiResponse> respuesta(String msg, Object data, boolean error, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse(msg, data, error, status), status);
    }
}
