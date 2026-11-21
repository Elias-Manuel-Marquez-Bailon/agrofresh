-- ==========================================================
-- AgroFresh - Esquema de base de datos + datos semilla
-- MySQL 8. Se ejecuta automáticamente la PRIMERA vez que se crea
-- el volumen de MySQL (docker-entrypoint-initdb.d).
-- ==========================================================

SET NAMES utf8mb4;
SET time_zone = '-06:00';

-- La base la crea el contenedor a partir de MYSQL_DATABASE; aquí solo la usamos.

-- ----------------------------------------------------------
-- usuarios
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol           ENUM('ADMIN','PRODUCTOR','COMPRADOR') NOT NULL,
    estado        ENUM('ACTIVO','PENDIENTE','BLOQUEADO') NOT NULL DEFAULT 'ACTIVO',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuarios_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- productores (perfil extendido de un usuario con rol PRODUCTOR)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS productores (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id   BIGINT NOT NULL,
    nombre_finca VARCHAR(150) NOT NULL,
    ubicacion    VARCHAR(200),
    descripcion  TEXT,
    verificado   BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_productores_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_productores_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- categorias
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    CONSTRAINT uk_categorias_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- productos
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS productos (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    productor_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    nombre       VARCHAR(150) NOT NULL,
    descripcion  TEXT,
    precio       DECIMAL(10,2) NOT NULL,
    unidad       VARCHAR(20) NOT NULL,          -- kg, pieza, caja, litro, manojo...
    stock        DECIMAL(10,2) NOT NULL DEFAULT 0,
    activo       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_productos_precio CHECK (precio >= 0),
    CONSTRAINT chk_productos_stock  CHECK (stock >= 0),
    CONSTRAINT fk_productos_productor FOREIGN KEY (productor_id) REFERENCES productores(id),
    CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    INDEX idx_productos_categoria (categoria_id),
    INDEX idx_productos_productor (productor_id),
    INDEX idx_productos_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- producto_imagenes (una imagen opcional por producto, en binario)
-- Tabla aparte para que listar productos no cargue las imágenes.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS producto_imagenes (
    producto_id    BIGINT PRIMARY KEY,
    tipo           VARCHAR(30) NOT NULL,         -- image/jpeg, image/png, image/webp
    datos          MEDIUMBLOB NOT NULL,          -- máx. 2 MB (validado en el backend)
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_imagen_producto FOREIGN KEY (producto_id)
        REFERENCES productos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- lotes (trazabilidad: siembra -> cosecha)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS lotes (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id    BIGINT NOT NULL,
    fecha_siembra  DATE NOT NULL,
    fecha_cosecha  DATE,
    parcela        VARCHAR(100),
    metodo_cultivo VARCHAR(50),                 -- ORGANICO, CONVENCIONAL, HIDROPONICO...
    notas          TEXT,
    CONSTRAINT fk_lotes_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    INDEX idx_lotes_producto (producto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- pedidos
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedidos (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprador_id      BIGINT NOT NULL,           -- usuarios.id (rol COMPRADOR)
    productor_id      BIGINT NOT NULL,           -- productores.id
    estado            ENUM('PENDIENTE','ACEPTADO','EN_CAMINO','ENTREGADO','CANCELADO')
                      NOT NULL DEFAULT 'PENDIENTE',
    total             DECIMAL(12,2) NOT NULL DEFAULT 0,
    direccion_entrega VARCHAR(255) NOT NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedidos_comprador FOREIGN KEY (comprador_id) REFERENCES usuarios(id),
    CONSTRAINT fk_pedidos_productor FOREIGN KEY (productor_id) REFERENCES productores(id),
    INDEX idx_pedidos_comprador (comprador_id),
    INDEX idx_pedidos_productor_estado (productor_id, estado),
    INDEX idx_pedidos_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- detalle_pedido
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalle_pedido (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id       BIGINT NOT NULL,
    producto_id     BIGINT NOT NULL,
    lote_id         BIGINT NULL,
    cantidad        DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_detalle_pedido   FOREIGN KEY (pedido_id)   REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT fk_detalle_lote     FOREIGN KEY (lote_id)     REFERENCES lotes(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- calificaciones (una por pedido ENTREGADO)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS calificaciones (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id    BIGINT NOT NULL,
    comprador_id BIGINT NOT NULL,
    productor_id BIGINT NOT NULL,
    puntuacion   TINYINT NOT NULL,
    comentario   VARCHAR(500),
    CONSTRAINT uk_calificaciones_pedido UNIQUE (pedido_id),
    CONSTRAINT chk_calificaciones_puntuacion CHECK (puntuacion BETWEEN 1 AND 5),
    CONSTRAINT fk_calif_pedido    FOREIGN KEY (pedido_id)    REFERENCES pedidos(id),
    CONSTRAINT fk_calif_comprador FOREIGN KEY (comprador_id) REFERENCES usuarios(id),
    CONSTRAINT fk_calif_productor FOREIGN KEY (productor_id) REFERENCES productores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- bitacora (registro de actividad)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS bitacora (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NULL,
    accion     VARCHAR(255) NOT NULL,
    fecha      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bitacora_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_bitacora_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================================
-- DATOS SEMILLA
-- ==========================================================

-- Administrador por defecto
--   email:    admin@agrofresh.com
--   password: Admin123*      (hash BCrypt, cost 10)
--   ¡CÁMBIALA después del primer inicio de sesión!
INSERT INTO usuarios (nombre, email, password_hash, rol, estado)
VALUES ('Administrador', 'admin@agrofresh.com',
        '$2a$10$SQU.odSlDWsWJy8EtWKbOOvTosSgMLiVZW41JZEuqFLr4CC/W/.TW',
        'ADMIN', 'ACTIVO');

INSERT INTO bitacora (usuario_id, accion)
VALUES (1, 'Inicialización del sistema: usuario administrador creado');

-- Categorías iniciales
INSERT INTO categorias (nombre) VALUES
    ('Frutas'),
    ('Verduras'),
    ('Hortalizas de hoja'),
    ('Granos y cereales'),
    ('Legumbres'),
    ('Tubérculos'),
    ('Hierbas aromáticas'),
    ('Lácteos'),
    ('Huevos'),
    ('Miel y derivados');
