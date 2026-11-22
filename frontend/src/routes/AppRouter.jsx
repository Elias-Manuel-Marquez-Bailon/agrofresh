import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { rutaInicio } from '../utils/errores'
import PrivateRoute from './PrivateRoute'
import RoleRoute from './RoleRoute'
import AuthLayout from '../layouts/AuthLayout'
import MainLayout from '../layouts/MainLayout'
import LoginPage from '../pages/auth/LoginPage'
import RegistroPage from '../pages/auth/RegistroPage'
import AdminHome from '../pages/admin/AdminHome'
import ProductorHome from '../pages/productor/ProductorHome'
import CompradorHome from '../pages/comprador/CompradorHome'
import CatalogoPage from '../pages/catalogo/CatalogoPage'
import ProductoDetallePage from '../pages/catalogo/ProductoDetallePage'
import MisProductosPage from '../pages/productor/MisProductosPage'
import CategoriasPage from '../pages/admin/CategoriasPage'
import CarritoPage from '../pages/comprador/CarritoPage'
import MisPedidosPage from '../pages/comprador/MisPedidosPage'
import PedidosRecibidosPage from '../pages/productor/PedidosRecibidosPage'
import LotesPage from '../pages/productor/LotesPage'
import PedidosAdminPage from '../pages/admin/PedidosAdminPage'
import NoAutorizado from '../pages/NoAutorizado'
import NotFound from '../pages/NotFound'
import Spinner from '../components/ui/Spinner'

// Los dashboards usan Recharts (pesado): se cargan solo cuando se abren.
const DashboardAdminPage = lazy(() => import('../pages/admin/DashboardAdminPage'))
const DashboardProductorPage = lazy(() => import('../pages/productor/DashboardProductorPage'))
const DashboardCompradorPage = lazy(() => import('../pages/comprador/DashboardCompradorPage'))

const conCarga = (pagina) => <Suspense fallback={<div className="py-20 text-center"><Spinner /></div>}>{pagina}</Suspense>

/** "/" manda a cada quien a su inicio según su rol; sin sesión, al catálogo público. */
function Inicio() {
  const { usuario, cargando } = useAuth()
  if (cargando) return <Spinner pantallaCompleta />
  return <Navigate to={usuario ? rutaInicio(usuario.rol) : '/catalogo'} replace />
}

/** Si ya hay sesión, no tiene sentido ver login/registro. */
function SoloInvitados({ children }) {
  const { usuario, cargando } = useAuth()
  if (cargando) return <Spinner pantallaCompleta />
  return usuario ? <Navigate to={rutaInicio(usuario.rol)} replace /> : children
}

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<Inicio />} />

      {/* Públicas */}
      <Route element={<SoloInvitados><AuthLayout /></SoloInvitados>}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/registro" element={<RegistroPage />} />
      </Route>

      {/* Catálogo público (con o sin sesión) */}
      <Route element={<MainLayout />}>
        <Route path="/catalogo" element={<CatalogoPage />} />
        <Route path="/catalogo/:id" element={<ProductoDetallePage />} />
      </Route>

      {/* Privadas, por rol */}
      <Route element={<PrivateRoute />}>
        <Route element={<MainLayout />}>
          <Route element={<RoleRoute roles={['ADMIN']} />}>
            <Route path="/admin" element={<AdminHome />} />
            <Route path="/admin/categorias" element={<CategoriasPage />} />
            <Route path="/admin/dashboard" element={conCarga(<DashboardAdminPage />)} />
            <Route path="/admin/pedidos" element={<PedidosAdminPage />} />
          </Route>
          <Route element={<RoleRoute roles={['PRODUCTOR']} />}>
            <Route path="/productor" element={<ProductorHome />} />
            <Route path="/productor/productos" element={<MisProductosPage />} />
            <Route path="/productor/pedidos" element={<PedidosRecibidosPage />} />
            <Route path="/productor/lotes" element={<LotesPage />} />
            <Route path="/productor/dashboard" element={conCarga(<DashboardProductorPage />)} />
          </Route>
          <Route element={<RoleRoute roles={['COMPRADOR']} />}>
            <Route path="/comprador" element={<CompradorHome />} />
            <Route path="/carrito" element={<CarritoPage />} />
            <Route path="/comprador/pedidos" element={<MisPedidosPage />} />
            <Route path="/comprador/dashboard" element={conCarga(<DashboardCompradorPage />)} />
          </Route>
          <Route path="/no-autorizado" element={<NoAutorizado />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
