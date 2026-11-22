# 🌱 AgroFresh

Plataforma que conecta **pequeños productores agrícolas** directamente con **compradores**: gestiona pedidos y registra la **trazabilidad** de cada producto (siembra → cosecha → entrega).

> Proyecto final — *Gestión del proceso de software*.

## Problemática
Los pequeños productores venden a intermediarios que se quedan con gran parte de la ganancia, y los compradores no conocen el origen de lo que consumen. AgroFresh elimina al intermediario y hace visible el origen del producto.

## Stack
| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3 · Java 17 · Maven · Spring Security + JWT · Spring Data JPA · Validation · Lombok · springdoc-openapi (Swagger) |
| Frontend | React · Vite · React Router · Axios (interceptor JWT) · Recharts · Tailwind CSS |
| Base de datos | MySQL 8 |
| Despliegue | Docker Compose (db · backend · frontend con Nginx → proxy `/api`) |
| CI/CD | Jenkins (Jenkinsfile) en instancia Ubuntu de AWS |

## Arquitectura
```
 Navegador ──► :80  Nginx (frontend)
                    ├── /        → SPA React (archivos estáticos)
                    └── /api/*   → backend:8080 (Spring Boot)
                                       └── db:3306 (MySQL, solo red interna)
```
Solo el puerto **80** se expone al exterior. MySQL y el backend viven en la red interna de Docker.

## Estructura del monorepo
```
.
├── docker-compose.yml                  # producción (sin 3306 expuesto)
├── docker-compose.override.example.yml # solo local: expone 3307 y 8080
├── Jenkinsfile                         # (Fase 7)
├── .env.example                        # plantilla de variables de entorno
├── db/init.sql                         # esquema + admin + categorías
├── backend/                            # proyecto Maven (abrir con IntelliJ IDEA)
└── frontend/                           # proyecto Vite (abrir con VS Code)
```

## Roles
- **ADMIN**: usuarios (aprobar/bloquear productores), categorías, dashboard global, bitácora.
- **PRODUCTOR**: CRUD de productos, lotes de cosecha, gestión de pedidos, dashboard propio.
- **COMPRADOR**: catálogo con filtros, carrito y pedidos, trazabilidad, calificar al productor.

Flujo de pedidos: `PENDIENTE → ACEPTADO → EN_CAMINO → ENTREGADO` (o `CANCELADO`). Al aceptar se descuenta stock; al cancelar se repone.

## Puesta en marcha con Docker
```bash
cp .env.example .env          # y edita las contraseñas y JWT_SECRET
docker compose up -d --build
```
- App: http://localhost
- Swagger: http://localhost/api/swagger-ui.html

### Credenciales iniciales
| Rol | Email | Password |
|---|---|---|
| ADMIN | admin@agrofresh.com | `Admin123*` |

⚠️ Cámbiala tras el primer inicio de sesión.

> `init.sql` solo se ejecuta la primera vez que se crea el volumen. Para reiniciar la BD desde cero: `docker compose down -v`.

### Desarrollo local
```bash
cp docker-compose.override.example.yml docker-compose.override.yml
docker compose up -d db       # MySQL en localhost:3307
```
Luego ejecuta el backend desde IntelliJ y el frontend con `npm run dev` (ver `backend/README.md` y `frontend/README.md`).

## Fases del proyecto
- [x] Fase 0 — Estructura, docker-compose, init.sql
- [x] Fase 1 — Backend base + autenticación JWT + tests
- [x] Fase 2 — Frontend base + login/registro + rutas por rol
- [x] Fase 3 — Catálogo público de productos (búsqueda, filtro, paginación e imagen) y categorías
- [x] Fase 4 — Pedidos, carrito y estados
- [ ] Fase 5 — Trazabilidad y calificaciones
- [ ] Fase 6 — Dashboards y exportación CSV
- [ ] Fase 7 — Dockerfiles, Jenkinsfile y despliegue en AWS
