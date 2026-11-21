import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Spinner from '../components/ui/Spinner'

/** Deja pasar solo si hay sesión; si no, manda a /login. */
export default function PrivateRoute() {
  const { autenticado, cargando } = useAuth()
  const location = useLocation()

  if (cargando) return <Spinner pantallaCompleta />
  if (!autenticado) return <Navigate to="/login" replace state={{ desde: location.pathname }} />
  return <Outlet />
}
