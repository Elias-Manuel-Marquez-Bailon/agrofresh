import api from './axios'

// Cada función devuelve el "data" del ApiResponse del backend.

/** productoId opcional: sin él devuelve los lotes de todos mis productos. */
export async function misLotes(productoId) {
  const { data } = await api.get('/lotes/mios', { params: { productoId: productoId || undefined } })
  return data.data
}

export async function crearLote(lote) {
  const { data } = await api.post('/lotes', lote)
  return data.data
}

export async function actualizarLote(id, lote) {
  const { data } = await api.put(`/lotes/${id}`, lote)
  return data.data
}

export async function eliminarLote(id) {
  await api.delete(`/lotes/${id}`)
}

/** Público: últimos lotes cosechados de un producto. */
export async function origenProducto(productoId) {
  const { data } = await api.get(`/lotes/producto/${productoId}`)
  return data.data
}
