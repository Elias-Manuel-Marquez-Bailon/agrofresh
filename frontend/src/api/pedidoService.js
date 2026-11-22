import api from './axios'

// Cada función devuelve el "data" del ApiResponse del backend.

/** items: [{ productoId, cantidad }]. Devuelve la lista de pedidos creados (uno por productor). */
export async function crearPedido(direccionEntrega, items) {
  const { data } = await api.post('/pedidos', { direccionEntrega, items })
  return { pedidos: data.data, mensaje: data.message }
}

/** Devuelve { contenido, pagina, tamanio, totalElementos, totalPaginas } */
export async function misPedidos({ estado, pagina = 0, tamanio = 10 } = {}) {
  const { data } = await api.get('/pedidos/mis-pedidos', { params: { estado: estado || undefined, pagina, tamanio } })
  return data.data
}

export async function pedidosRecibidos({ estado, pagina = 0, tamanio = 10 } = {}) {
  const { data } = await api.get('/pedidos/recibidos', { params: { estado: estado || undefined, pagina, tamanio } })
  return data.data
}

/** lotes (opcional, solo al aceptar): { productoId: loteId } */
export async function cambiarEstadoPedido(id, estado, lotes) {
  const { data } = await api.patch(`/pedidos/${id}/estado`, { estado, lotes })
  return { pedido: data.data, mensaje: data.message }
}

export async function obtenerTrazabilidad(id) {
  const { data } = await api.get(`/pedidos/${id}/trazabilidad`)
  return data.data
}
