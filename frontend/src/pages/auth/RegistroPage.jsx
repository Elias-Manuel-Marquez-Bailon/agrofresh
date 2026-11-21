import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { leerError, rutaInicio } from '../../utils/errores'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import Alert from '../../components/ui/Alert'

const ROLES = [
  { valor: 'COMPRADOR', titulo: 'Comprador', desc: 'Quiero comprar productos frescos' },
  { valor: 'PRODUCTOR', titulo: 'Productor', desc: 'Quiero vender mi cosecha' },
]

const INICIAL = { nombre: '', email: '', password: '', confirmar: '', rol: 'COMPRADOR', nombreFinca: '', ubicacion: '' }

export default function RegistroPage() {
  const { registro } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState(INICIAL)
  const [errores, setErrores] = useState({})
  const [mensaje, setMensaje] = useState('')
  const [cargando, setCargando] = useState(false)
  const esProductor = form.rol === 'PRODUCTOR'

  const cambiar = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrores({ ...errores, [e.target.name]: undefined })
  }

  // Mismas reglas que RegistroDto en el backend
  const validar = () => {
    const e = {}
    if (!form.nombre.trim()) e.nombre = 'El nombre es obligatorio'
    if (!form.email.trim()) e.email = 'El correo es obligatorio'
    else if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = 'El correo no tiene un formato válido'
    if (form.password.length < 8) e.password = 'La contraseña debe tener al menos 8 caracteres'
    if (form.confirmar !== form.password) e.confirmar = 'Las contraseñas no coinciden'
    if (esProductor && !form.nombreFinca.trim()) e.nombreFinca = 'El nombre de la finca es obligatorio'
    setErrores(e)
    return Object.keys(e).length === 0
  }

  const enviar = async (e) => {
    e.preventDefault()
    setMensaje('')
    if (!validar()) return

    const datos = {
      nombre: form.nombre,
      email: form.email,
      password: form.password,
      rol: form.rol,
      ...(esProductor && { nombreFinca: form.nombreFinca, ubicacion: form.ubicacion || null }),
    }

    setCargando(true)
    try {
      const usuario = await registro(datos)
      navigate(rutaInicio(usuario.rol), { replace: true })
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
      <h1 className="text-2xl font-bold">Crear cuenta</h1>
      <p className="mt-1 mb-6 text-sm text-gray-500">Únete a AgroFresh</p>

      <form onSubmit={enviar} className="space-y-4" noValidate>
        <Alert>{mensaje}</Alert>

        <fieldset>
          <legend className="mb-2 text-sm font-medium text-gray-700">¿Cómo usarás AgroFresh?</legend>
          <div className="grid grid-cols-2 gap-3">
            {ROLES.map((r) => (
              <label
                key={r.valor}
                className={`cursor-pointer rounded-xl border p-3 transition ${
                  form.rol === r.valor ? 'border-marca-600 bg-marca-50 ring-1 ring-marca-600' : 'border-gray-300 hover:bg-gray-50'
                }`}
              >
                <input type="radio" name="rol" value={r.valor} checked={form.rol === r.valor} onChange={cambiar} className="sr-only" />
                <span className="block text-sm font-semibold">{r.titulo}</span>
                <span className="block text-xs text-gray-500">{r.desc}</span>
              </label>
            ))}
          </div>
          {errores.rol && <p className="mt-1 text-xs text-red-600">{errores.rol}</p>}
        </fieldset>

        <Input label="Nombre completo" name="nombre" autoComplete="name" value={form.nombre} onChange={cambiar} error={errores.nombre} />
        <Input label="Correo electrónico" name="email" type="email" autoComplete="email" value={form.email} onChange={cambiar} error={errores.email} />

        <div className="grid gap-4 sm:grid-cols-2">
          <Input label="Contraseña" name="password" type="password" autoComplete="new-password" value={form.password} onChange={cambiar} error={errores.password} />
          <Input label="Confirmar contraseña" name="confirmar" type="password" autoComplete="new-password" value={form.confirmar} onChange={cambiar} error={errores.confirmar} />
        </div>

        {esProductor && (
          <div className="space-y-4 rounded-xl bg-gray-50 p-4">
            <Input label="Nombre de la finca" name="nombreFinca" value={form.nombreFinca} onChange={cambiar} error={errores.nombreFinca} />
            <Input label="Ubicación (opcional)" name="ubicacion" placeholder="Municipio, estado" value={form.ubicacion} onChange={cambiar} error={errores.ubicacion} />
          </div>
        )}

        <Button type="submit" cargando={cargando} className="w-full">
          Crear cuenta
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-600">
        ¿Ya tienes cuenta?{' '}
        <Link to="/login" className="font-semibold text-marca-700 hover:underline">
          Inicia sesión
        </Link>
      </p>
    </>
  )
}
