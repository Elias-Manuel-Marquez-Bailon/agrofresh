import api from './axios'

/** rol: "productor" | "comprador" | "admin". Devuelve el "data" del ApiResponse. */
export async function obtenerDashboard(rol) {
  const { data } = await api.get(`/dashboard/${rol}`)
  return data.data
}

/**
 * Descarga el CSV de pedidos. filtros: { estado, desde, hasta } (todos opcionales, fechas "AAAA-MM-DD").
 * Se pide como blob (con el token del interceptor) y se descarga con un link temporal.
 */
export async function descargarCsv(rol, { estado, desde, hasta } = {}) {
  const res = await api.get(`/dashboard/${rol}/pedidos.csv`, {
    params: { estado: estado || undefined, desde: desde || undefined, hasta: hasta || undefined },
    responseType: 'blob',
  })
  const disposicion = res.headers['content-disposition'] ?? ''
  const nombre = /filename="?([^";]+)"?/.exec(disposicion)?.[1] ?? 'pedidos.csv'

  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url
  a.download = nombre
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

/**
 * Con responseType "blob", el error JSON del backend también llega como blob.
 * Esto lo convierte de vuelta para que leerError() pueda leer el mensaje.
 */
export async function normalizarErrorBlob(error) {
  const data = error.response?.data
  if (data instanceof Blob) {
    try {
      error.response.data = JSON.parse(await data.text())
    } catch {
      // no era JSON; se deja como está
    }
  }
  return error
}
