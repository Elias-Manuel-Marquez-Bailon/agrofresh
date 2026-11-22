# AgroFresh — Backend

API REST de AgroFresh: Spring Boot 3.5 · Java 17 · Maven · Spring Security + JWT · JPA (MySQL 8) · springdoc (Swagger).

## Requisitos

- Java 17 y Maven 3.9+ (o el Maven integrado de IntelliJ IDEA)
- MySQL 8 con la base `agrofresh` creada a partir de [`../db/init.sql`](../db/init.sql)

## Cómo correrlo

### En IntelliJ IDEA
1. Abre la carpeta `backend/` como proyecto (IntelliJ detecta el `pom.xml`).
2. En la configuración de ejecución de `BackendApplication`, agrega en *Environment variables*, por ejemplo:
   `DB_USER=root;DB_PASSWORD=tu_password`
3. Ejecuta `BackendApplication`. La API queda en http://localhost:8080.

### Desde la terminal
```bash
mvn spring-boot:run
```

## Variables de entorno

Todas tienen un valor por defecto para desarrollo local (ver `src/main/resources/application.properties`). En Docker se toman del `.env` de la raíz.

| Variable | Por defecto | Descripción |
|---|---|---|
| `DB_HOST` | `localhost` | Host de MySQL (`db` dentro de Docker) |
| `DB_PORT` | `3306` | Puerto de MySQL |
| `DB_NAME` | `agrofresh` | Nombre de la base |
| `DB_USER` | `root` | Usuario de MySQL |
| `DB_PASSWORD` | *(vacío)* | Contraseña de MySQL |
| `JWT_SECRET` | clave de desarrollo | Clave para firmar los tokens. **En producción, cámbiala por una larga y aleatoria** |
| `JWT_EXPIRATION` | `86400000` | Vigencia del token en ms (24 h) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost` | Orígenes permitidos, separados por coma |

## Pruebas

```bash
mvn test
```
Son pruebas unitarias con JUnit 5 y Mockito (auth, JWT, categorías, productos y pedidos). No necesitan base de datos.

## Swagger

http://localhost:8080/swagger-ui.html — usa **Authorize** y pega el token que devuelve `/api/auth/login`.

## Endpoints

Todas las respuestas usan el formato `{ message, data, error, status }`.

### Auth
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/registro` | Público |
| POST | `/api/auth/login` | Público |
| GET | `/api/auth/me` | Con sesión |

### Categorías
| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/categorias` | Público |
| POST / PUT / DELETE | `/api/categorias[/{id}]` | ADMIN |

### Productos
| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/productos?nombre=&categoriaId=&pagina=&tamanio=` | Público (catálogo) |
| GET | `/api/productos/{id}` y `/api/productos/{id}/imagen` | Público |
| GET | `/api/productos/mios` | PRODUCTOR |
| POST / PUT | `/api/productos[/{id}]` | PRODUCTOR (solo los suyos) |
| PATCH | `/api/productos/{id}/estado?activo=` | PRODUCTOR |
| PUT / DELETE | `/api/productos/{id}/imagen` | PRODUCTOR (JPG, PNG o WEBP, máx. 2 MB) |

### Pedidos
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/pedidos` | COMPRADOR: crea un pedido por cada productor del carrito |
| GET | `/api/pedidos/mis-pedidos?estado=&pagina=&tamanio=` | COMPRADOR |
| GET | `/api/pedidos/recibidos?estado=&pagina=&tamanio=` | PRODUCTOR |
| GET | `/api/pedidos?estado=&pagina=&tamanio=` | ADMIN |
| GET | `/api/pedidos/{id}` | Comprador o productor del pedido, o ADMIN |
| PATCH | `/api/pedidos/{id}/estado` | Comprador o productor del pedido, o ADMIN (solo cancelar) |

**Estados:** `PENDIENTE → ACEPTADO → EN_CAMINO → ENTREGADO`, o `CANCELADO`.
- Al **aceptar**, se descuenta el stock (con bloqueo de fila para evitar sobreventa). Si un producto no alcanza, no se acepta.
- Al **cancelar** un pedido ya aceptado, se repone el stock.
- El productor avanza los estados y puede cancelar mientras el pedido esté PENDIENTE o ACEPTADO.
- El comprador solo puede cancelar mientras esté PENDIENTE.
- El ADMIN solo puede cancelar, mientras esté PENDIENTE o ACEPTADO.

**Lotes al aceptar (opcional):** `{ "estado": "ACEPTADO", "lotes": { "<productoId>": <loteId> } }`. El lote debe ser del mismo producto y estar cosechado. Al pasar a ENTREGADO se guarda `fecha_entrega`.

| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/pedidos/{id}/trazabilidad` | Comprador o productor del pedido, o ADMIN: siembra → cosecha → entrega |

### Lotes (trazabilidad)
| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/lotes/producto/{productoId}` | Público: últimos lotes cosechados |
| GET | `/api/lotes/mios?productoId=` | PRODUCTOR |
| POST / PUT | `/api/lotes[/{id}]` | PRODUCTOR (solo sus productos; la cosecha no puede ser anterior a la siembra ni futura) |
| DELETE | `/api/lotes/{id}` | PRODUCTOR (si no está asignado a pedidos) |

### Calificaciones
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/calificaciones` | COMPRADOR: una por pedido, solo si está ENTREGADO |
| GET | `/api/calificaciones/productor/{id}?pagina=&tamanio=` | Público: promedio, total y reseñas |
| GET | `/api/calificaciones/mias` | PRODUCTOR |

El catálogo (`/api/productos`) incluye `calificacionPromedio` y `totalCalificaciones` del productor.

### Dashboards y CSV
| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/dashboard/productor` | PRODUCTOR: ventas, pedidos por estado, ventas por mes, top productos, calificación |
| GET | `/api/dashboard/comprador` | COMPRADOR: gasto, pedidos por estado, gasto por mes, top productos y productores |
| GET | `/api/dashboard/admin` | ADMIN: lo anterior para toda la plataforma + ventas por categoría y usuarios por rol |
| GET | `/api/dashboard/{productor\|comprador\|admin}/pedidos.csv?estado=&desde=&hasta=` | El rol correspondiente |

- **Ventas** = pedidos ACEPTADOS, EN_CAMINO o ENTREGADOS (los pendientes y cancelados no cuentan).
- Ventas por mes: últimos 6 meses, los meses sin ventas salen en 0. Tops: los 5 primeros por monto.
- CSV: UTF-8 con BOM (Excel respeta los acentos), fechas `AAAA-MM-DD` y `hasta` inclusivo. Los textos que empiezan con `= + - @` se escapan para evitar inyección de fórmulas.

> **Base ya creada:** si tu base `agrofresh` existía antes de la Fase 5, ejecuta una vez:
> `ALTER TABLE pedidos ADD COLUMN fecha_entrega DATETIME NULL;`

## Estructura

```
com.example.backend
├── auth/        registro, login y /me
├── categoria/   CRUD de categorías
├── producto/    catálogo, productos del productor e imágenes
├── pedido/      carrito → pedidos, cambios de estado y trazabilidad
├── lote/        lotes de siembra y cosecha
├── calificacion/ reseñas de pedidos entregados
├── usuario/     entidades Usuario y Productor
├── config/      seguridad (JWT, CORS) y Swagger
└── kernel/      ApiResponse y manejo global de excepciones
```
Cada módulo tiene `model/` (entidad y repositorio), `controller/` (controller y service) y `dtos/`.
