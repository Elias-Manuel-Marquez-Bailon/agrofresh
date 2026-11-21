import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { leerError, rutaInicio } from '../../utils/errores'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import Alert from '../../components/ui/Alert'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [form, setForm] = useState({ email: '', password: '' })
  const [errores, setErrores] = useState({})
  const [mensaje, setMensaje] = useState('')
  const [cargando, setCargando] = useState(false)

  const cambiar = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrores({ ...errores, [e.target.name]: undefined })
  }

  const validar = () => {
    const e = {}
    if (!form.email.trim()) e.email = 'El correo es obligatorio'
    else if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = 'El correo no tiene un formato válido'
    if (!form.password) e.password = 'La contraseña es obligatoria'
    setErrores(e)
    return Object.keys(e).length === 0
  }

  const enviar = async (e) => {
    e.preventDefault()
    setMensaje('')
    if (!validar()) return

    setCargando(true)
    try {
      const usuario = await login(form.email, form.password)
      // Si venía de una ruta protegida, regresa ahí; si no, a su inicio
      navigate(location.state?.desde || rutaInicio(usuario.rol), { replace: true })
    } catch (err) {
      const { mensaje, campos } = leerError(err)
      setMensaje(mensaje)
      setErrores(campos)
    } finally {
      setCargando(false)
    }
  }

  return (
    <>
      <h1 className="text-2xl font-bold">Iniciar sesión</h1>
      <p className="mt-1 mb-6 text-sm text-gray-500">Bienvenido de nuevo a AgroFresh</p>

      <form onSubmit={enviar} className="space-y-4" noValidate>
        <Alert>{mensaje}</Alert>
        <Input
          label="Correo electrónico"
          name="email"
          type="email"
          autoComplete="email"
          value={form.email}
          onChange={cambiar}
          error={errores.email}
        />
        <Input
          label="Contraseña"
          name="password"
          type="password"
          autoComplete="current-password"
          value={form.password}
          onChange={cambiar}
          error={errores.password}
        />
        <Button type="submit" cargando={cargando} className="w-full">
          Entrar
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-600">
        ¿No tienes cuenta?{' '}
        <Link to="/registro" className="font-semibold text-marca-700 hover:underline">
          Regístrate
        </Link>
      </p>
    </>
  )
}
