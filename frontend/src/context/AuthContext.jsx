import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import * as authService from '../api/authService'
import { TOKEN_KEY, USUARIO_KEY } from '../api/axios'

const AuthContext = createContext(null)

function leerUsuarioGuardado() {
  try {
    return JSON.parse(localStorage.getItem(USUARIO_KEY))
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(leerUsuarioGuardado)
  const [cargando, setCargando] = useState(!!localStorage.getItem(TOKEN_KEY))

  const guardarSesion = useCallback(({ token, usuario }) => {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario))
    setUsuario(usuario)
    return usuario
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USUARIO_KEY)
    setUsuario(null)
  }, [])

  // Al abrir la app, si hay token se confirma con el backend que sigue siendo válido
  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) return
    authService
      .me()
      .then((u) => {
        localStorage.setItem(USUARIO_KEY, JSON.stringify(u))
        setUsuario(u)
      })
      .catch(logout)
      .finally(() => setCargando(false))
  }, [logout])

  const login = useCallback(
    async (email, password) => guardarSesion(await authService.login(email, password)),
    [guardarSesion],
  )

  const registro = useCallback(
    async (datos) => guardarSesion(await authService.registro(datos)),
    [guardarSesion],
  )

  const value = useMemo(
    () => ({ usuario, cargando, autenticado: !!usuario, login, registro, logout }),
    [usuario, cargando, login, registro, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return ctx
}
