import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/** Deja pasar solo si el rol del usuario está en "roles". Uso: <RoleRoute roles={['ADMIN']} /> */
export default function RoleRoute({ roles }) {
  const { usuario } = useAuth()
  if (!roles.includes(usuario?.rol)) return <Navigate to="/no-autorizado" replace />
  return <Outlet />
}
