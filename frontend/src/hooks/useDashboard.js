import { useEffect, useState } from 'react'
import { obtenerDashboard } from '../api/dashboardService'
import { leerError } from '../utils/errores'

/** Carga el dashboard del rol indicado. Devuelve { datos, error }. */
export default function useDashboard(rol) {
  const [datos, setDatos] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let activo = true
    obtenerDashboard(rol)
      .then((d) => activo && setDatos(d))
      .catch((e) => activo && setError(leerError(e).mensaje))
    return () => {
      activo = false
    }
  }, [rol])

  return { datos, error }
}
