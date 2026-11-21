/**
 * Convierte un error de Axios en algo fácil de mostrar:
 *  - mensaje: texto general (message del ApiResponse)
 *  - campos:  errores por campo cuando el backend devuelve { email: "...", password: "..." }
 */
export function leerError(error) {
  if (!error.response) {
    return { mensaje: 'No se pudo conectar con el servidor', campos: {} }
  }
  const body = error.response.data ?? {}
  const campos = body.data && typeof body.data === 'object' && !Array.isArray(body.data) ? body.data : {}
  return { mensaje: body.message || 'Ocurrió un error inesperado', campos }
}

/** Ruta de inicio según el rol del usuario. */
export function rutaInicio(rol) {
  switch (rol) {
    case 'ADMIN':
      return '/admin'
    case 'PRODUCTOR':
      return '/productor'
    case 'COMPRADOR':
      return '/comprador'
    default:
      return '/login'
  }
}
