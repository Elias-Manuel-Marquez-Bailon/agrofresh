import api from './axios'

// Cada función devuelve el "data" del ApiResponse del backend.

// ---------- Categorías ----------

export async function listarCategorias() {
  const { data } = await api.get('/categorias')
  return data.data // [{ id, nombre }]
}

export async function crearCategoria(nombre) {
  const { data } = await api.post('/categorias', { nombre })
  return data.data
}

export async function actualizarCategoria(id, nombre) {
  const { data } = await api.put(`/categorias/${id}`, { nombre })
  return data.data
}

export async function eliminarCategoria(id) {
  await api.delete(`/categorias/${id}`)
}

// ---------- Productos (público) ----------

/** filtros: { nombre, categoriaId, pagina, tamanio } */
export async function buscarProductos(filtros = {}) {
  const params = Object.fromEntries(Object.entries(filtros).filter(([, v]) => v !== '' && v != null))
  const { data } = await api.get('/productos', { params })
  return data.data // { contenido, pagina, tamanio, totalElementos, totalPaginas }
}

export async function obtenerProducto(id) {
  const { data } = await api.get(`/productos/${id}`)
  return data.data
}

// ---------- Productos (productor) ----------

export async function misProductos() {
  const { data } = await api.get('/productos/mios')
  return data.data
}

export async function crearProducto(producto) {
  const { data } = await api.post('/productos', producto)
  return data.data
}

export async function actualizarProducto(id, producto) {
  const { data } = await api.put(`/productos/${id}`, producto)
  return data.data
}

export async function cambiarEstadoProducto(id, activo) {
  const { data } = await api.patch(`/productos/${id}/estado`, null, { params: { activo } })
  return data.data
}

/** archivo: Blob o File (ya reducido con reducirImagen). */
export async function subirImagenProducto(id, archivo) {
  const form = new FormData()
  form.append('archivo', archivo, 'imagen.jpg')
  // Se indica multipart explícitamente: el cliente tiene JSON por defecto.
  const { data } = await api.put(`/productos/${id}/imagen`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data.data
}

export async function eliminarImagenProducto(id) {
  const { data } = await api.delete(`/productos/${id}/imagen`)
  return data.data
}
