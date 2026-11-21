import axios from 'axios'

export const TOKEN_KEY = 'agrofresh_token'
export const USUARIO_KEY = 'agrofresh_usuario'

// baseURL "/api": en desarrollo lo resuelve el proxy de Vite y en Docker, Nginx.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Agrega el token a cada petición
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Si el token expiró o es inválido (401), se cierra la sesión.
// Se omiten login/registro, donde un 401 solo significa "credenciales incorrectas".
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const url = error.config?.url ?? ''
    const esAuth = url.includes('/auth/login') || url.includes('/auth/registro')
    if (error.response?.status === 401 && !esAuth) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USUARIO_KEY)
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default api
