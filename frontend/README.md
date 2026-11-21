# AgroFresh — Frontend

React 19 + Vite + React Router + Axios + Tailwind CSS 4 (JavaScript).

## Desarrollo local

```bash
npm install
npm run dev        # http://localhost:5173
```

El backend debe estar corriendo en `http://localhost:8080`. Vite reenvía todas las
peticiones a `/api` hacia ese puerto (ver `vite.config.js`), así que no hace falta
configurar CORS ni URLs.

## Scripts

| Comando           | Qué hace                              |
|-------------------|---------------------------------------|
| `npm run dev`     | Servidor de desarrollo                |
| `npm run build`   | Compila a `dist/` (lo que sirve Nginx)|
| `npm run preview` | Sirve el build localmente             |
| `npm run lint`    | Revisa el código con oxlint           |

## Estructura

```
src/
├── api/          axios.js (instancia + interceptor JWT) y servicios por módulo
├── context/      AuthContext: usuario, token, login(), registro(), logout()
├── routes/       AppRouter, PrivateRoute (requiere sesión), RoleRoute (requiere rol)
├── layouts/      AuthLayout (login/registro) y MainLayout (navbar + contenido)
├── components/   Navbar, Logo y componentes ui/ reutilizables
├── pages/        auth/, admin/, productor/, comprador/
└── utils/        errores.js (lee el ApiResponse de error) y rutaInicio(rol)
```

## Rutas

| Ruta          | Acceso         |
|---------------|----------------|
| `/login`      | Público        |
| `/registro`   | Público        |
| `/admin`      | ADMIN          |
| `/productor`  | PRODUCTOR      |
| `/comprador`  | COMPRADOR      |

El token se guarda en `localStorage`; si el backend responde 401 se cierra la sesión.
