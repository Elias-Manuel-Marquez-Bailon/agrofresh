import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { rutaInicio } from '../utils/errores'

export default function NoAutorizado() {
  const { usuario } = useAuth()
  return (
    <div className="py-20 text-center">
      <p className="text-5xl font-bold text-marca-700">403</p>
      <h1 className="mt-3 text-xl font-semibold">No tienes permiso para ver esta página</h1>
      <Link to={rutaInicio(usuario?.rol)} className="mt-6 inline-block font-semibold text-marca-700 hover:underline">
        Volver a mi inicio
      </Link>
    </div>
  )
}
