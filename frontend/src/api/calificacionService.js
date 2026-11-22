import api from './axios'

// Cada función devuelve el "data" del ApiResponse del backend.

export async function calificarPedido(pedidoId, puntuacion, comentario) {
  const { data } = await api.post('/calificaciones', { pedidoId, puntuacion, comentario })
  return { calificacion: data.data, mensaje: data.message }
}

/** Público. Devuelve { productorId, nombreFinca, promedio, total, resenas: { contenido, ... } } */
export async function resenasProductor(productorId, { pagina = 0, tamanio = 5 } = {}) {
  const { data } = await api.get(`/calificaciones/productor/${productorId}`, { params: { pagina, tamanio } })
  return data.data
}

/** Las del productor con sesión. */
export async function misResenas({ pagina = 0, tamanio = 5 } = {}) {
  const { data } = await api.get('/calificaciones/mias', { params: { pagina, tamanio } })
  return data.data
}
