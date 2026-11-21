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
import NoAutorizado from '../pages/NoAutorizado'
import NotFound from '../pages/NotFound'
import Spinner from '../components/ui/Spinner'

/** "/" manda a cada quien a su inicio según su rol. */
function Inicio() {
  const { usuario, cargando } = useAuth()
  if (cargando) return <Spinner pantallaCompleta />
  return <Navigate to={usuario ? rutaInicio(usuario.rol) : '/login'} replace />
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

      {/* Privadas, por rol */}
      <Route element={<PrivateRoute />}>
        <Route element={<MainLayout />}>
          <Route element={<RoleRoute roles={['ADMIN']} />}>
            <Route path="/admin" element={<AdminHome />} />
          </Route>
          <Route element={<RoleRoute roles={['PRODUCTOR']} />}>
            <Route path="/productor" element={<ProductorHome />} />
          </Route>
          <Route element={<RoleRoute roles={['COMPRADOR']} />}>
            <Route path="/comprador" element={<CompradorHome />} />
          </Route>
          <Route path="/no-autorizado" element={<NoAutorizado />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
