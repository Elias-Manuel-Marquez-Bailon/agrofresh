import api from './axios'

// Cada función devuelve el "data" del ApiResponse del backend.

export async function login(email, password) {
  const { data } = await api.post('/auth/login', { email, password })
  return data.data // { token, tipo, expiraEn, usuario }
}

export async function registro(datos) {
  const { data } = await api.post('/auth/registro', datos)
  return data.data // { token, tipo, expiraEn, usuario }
}

export async function me() {
  const { data } = await api.get('/auth/me')
  return data.data // { id, nombre, email, rol, estado }
}
