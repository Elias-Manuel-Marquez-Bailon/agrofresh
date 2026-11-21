import { useEffect, useState } from 'react'
import { actualizarCategoria, crearCategoria, eliminarCategoria, listarCategorias } from '../../api/catalogoService'
import { leerError } from '../../utils/errores'
import { iconoCategoria } from '../../utils/categorias'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import Alert from '../../components/ui/Alert'
import Spinner from '../../components/ui/Spinner'

export default function CategoriasPage() {
  const [categorias, setCategorias] = useState(null)
  const [nueva, setNueva] = useState('')
  const [editando, setEditando] = useState({ id: null, nombre: '' })
  const [aviso, setAviso] = useState({ tipo: 'exito', texto: '' })
  const [cargando, setCargando] = useState(false)

  const cargar = () =>
    listarCategorias()
      .then(setCategorias)
      .catch((e) => setAviso({ tipo: 'error', texto: leerError(e).mensaje }))

  useEffect(() => {
    cargar()
  }, [])

  // Ejecuta una acción, muestra el resultado y recarga la lista
  const ejecutar = async (accion, exito) => {
    setCargando(true)
    setAviso({ tipo: 'exito', texto: '' })
    try {
      await accion()
      setAviso({ tipo: 'exito', texto: exito })
      await cargar()
      return true
    } catch (e) {
      const { mensaje, campos } = leerError(e)
      setAviso({ tipo: 'error', texto: campos.nombre || mensaje })
      return false
    } finally {
      setCargando(false)
    }
  }

  const crear = async (e) => {
    e.preventDefault()
    const nombre = nueva.trim()
    if (!nombre) return setAviso({ tipo: 'error', texto: 'Escribe el nombre de la categoría' })
    if (await ejecutar(() => crearCategoria(nombre), `Categoría "${nombre}" creada`)) setNueva('')
  }

  const guardarEdicion = async (e) => {
    e.preventDefault()
    const nombre = editando.nombre.trim()
    if (!nombre) return setAviso({ tipo: 'error', texto: 'El nombre no puede quedar vacío' })
    if (await ejecutar(() => actualizarCategoria(editando.id, nombre), 'Categoría actualizada')) {
      setEditando({ id: null, nombre: '' })
    }
  }

  const eliminar = (c) => {
    if (!window.confirm(`¿Eliminar la categoría "${c.nombre}"?`)) return
    ejecutar(() => eliminarCategoria(c.id), `Categoría "${c.nombre}" eliminada`)
  }

  return (
    <section className="max-w-2xl">
      <h1 className="text-2xl font-bold">Categorías</h1>
      <p className="mt-1 text-gray-500">Las categorías con productos no se pueden eliminar</p>

      <form onSubmit={crear} className="mt-6 flex items-end gap-3">
        <Input
          label="Nueva categoría"
          name="nueva"
          value={nueva}
          onChange={(e) => setNueva(e.target.value)}
          maxLength={80}
          className="flex-1"
        />
        <Button type="submit" cargando={cargando && !editando.id}>
          Agregar
        </Button>
      </form>

      <div className="mt-4">
        <Alert tipo={aviso.tipo}>{aviso.texto}</Alert>
      </div>

      {!categorias ? (
        <div className="py-16 text-center">
          <Spinner />
        </div>
      ) : (
        <ul className="mt-4 divide-y divide-gray-100 rounded-xl bg-white ring-1 ring-gray-200">
          {categorias.map((c) => (
            <li key={c.id} className="flex items-center gap-3 px-4 py-3">
              <span className="text-xl" aria-hidden>
                {iconoCategoria(c.nombre)}
              </span>
              {editando.id === c.id ? (
                <form onSubmit={guardarEdicion} className="flex flex-1 items-center gap-2">
                  <input
                    autoFocus
                    value={editando.nombre}
                    onChange={(e) => setEditando({ ...editando, nombre: e.target.value })}
                    maxLength={80}
                    aria-label="Nombre de la categoría"
                    className="flex-1 rounded-lg border border-gray-300 px-3 py-1.5 text-sm outline-none focus:border-marca-600 focus:ring-2 focus:ring-marca-100"
                  />
                  <button type="submit" disabled={cargando} className="text-sm font-medium text-marca-700 hover:underline">
                    Guardar
                  </button>
                  <button type="button" onClick={() => setEditando({ id: null, nombre: '' })} className="text-sm text-gray-500 hover:underline">
                    Cancelar
                  </button>
                </form>
              ) : (
                <>
                  <span className="flex-1 text-sm font-medium">{c.nombre}</span>
                  <button onClick={() => setEditando({ id: c.id, nombre: c.nombre })} className="text-sm font-medium text-marca-700 hover:underline">
                    Editar
                  </button>
                  <button onClick={() => eliminar(c)} disabled={cargando} className="text-sm font-medium text-red-600 hover:underline">
                    Eliminar
                  </button>
                </>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
