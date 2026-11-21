import { useEffect, useState } from 'react'
import {
  actualizarProducto,
  crearProducto,
  eliminarImagenProducto,
  subirImagenProducto,
} from '../../api/catalogoService'
import { leerError } from '../../utils/errores'
import { reducirImagen } from '../../utils/imagen'
import ProductoImagen from '../../components/ProductoImagen'
import Input from '../../components/ui/Input'
import Button from '../../components/ui/Button'
import Alert from '../../components/ui/Alert'

const UNIDADES = ['kg', 'pieza', 'caja', 'litro', 'manojo', 'docena', 'costal']

const vacio = { nombre: '', categoriaId: '', precio: '', unidad: 'kg', stock: '', descripcion: '' }

const desdeProducto = (p) =>
  p
    ? {
        nombre: p.nombre,
        categoriaId: String(p.categoriaId),
        precio: String(p.precio),
        unidad: p.unidad,
        stock: String(p.stock),
        descripcion: p.descripcion ?? '',
      }
    : vacio

/**
 * Formulario para crear o editar un producto (con imagen opcional).
 * El padre le pone un "key" por producto, así cada apertura empieza limpia.
 */
export default function ProductoForm({ producto, categorias, onGuardado, onCancelar }) {
  const editando = !!producto
  const [form, setForm] = useState(() => desdeProducto(producto))
  const [errores, setErrores] = useState({})
  const [mensaje, setMensaje] = useState('')
  const [cargando, setCargando] = useState(false)
  // Imagen: null = sin cambios · Blob = nueva · 'quitar' = eliminar la actual
  const [imagen, setImagen] = useState(null)
  const [vistaPrevia, setVistaPrevia] = useState(null)

  // Libera la URL temporal de la vista previa
  useEffect(() => () => vistaPrevia && URL.revokeObjectURL(vistaPrevia), [vistaPrevia])

  const cambiar = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrores({ ...errores, [e.target.name]: undefined })
  }

  const elegirImagen = async (e) => {
    const archivo = e.target.files?.[0]
    e.target.value = '' // permite volver a elegir el mismo archivo
    if (!archivo) return
    setErrores({ ...errores, imagen: undefined })
    try {
      const reducida = await reducirImagen(archivo)
      if (reducida.size > 2 * 1024 * 1024) throw new Error('La imagen sigue pesando más de 2 MB')
      setImagen(reducida)
      setVistaPrevia(URL.createObjectURL(reducida))
    } catch (err) {
      setErrores({ ...errores, imagen: err.message })
    }
  }

  const quitarImagen = () => {
    setImagen(editando && producto.imagenUrl ? 'quitar' : null)
    setVistaPrevia(null)
  }

  // Mismas reglas que ProductoDto en el backend
  const validar = () => {
    const e = {}
    const decimal = /^\d+(\.\d{1,2})?$/
    if (!form.nombre.trim()) e.nombre = 'El nombre es obligatorio'
    if (!form.categoriaId) e.categoriaId = 'La categoría es obligatoria'
    if (!decimal.test(form.precio)) e.precio = 'Precio inválido (mayor o igual a 0, máximo 2 decimales)'
    if (!decimal.test(form.stock)) e.stock = 'Stock inválido (mayor o igual a 0, máximo 2 decimales)'
    if (!form.unidad.trim()) e.unidad = 'La unidad es obligatoria'
    setErrores(e)
    return Object.keys(e).length === 0
  }

  const enviar = async (e) => {
    e.preventDefault()
    setMensaje('')
    if (!validar()) return

    const datos = {
      nombre: form.nombre.trim(),
      categoriaId: Number(form.categoriaId),
      precio: Number(form.precio),
      unidad: form.unidad.trim(),
      stock: Number(form.stock),
      descripcion: form.descripcion.trim() || null,
    }

    setCargando(true)
    try {
      let guardado = editando ? await actualizarProducto(producto.id, datos) : await crearProducto(datos)
      try {
        if (imagen instanceof Blob) guardado = await subirImagenProducto(guardado.id, imagen)
        else if (imagen === 'quitar') guardado = await eliminarImagenProducto(guardado.id)
      } catch (err) {
        // El producto sí se guardó; solo avisamos que la imagen falló
        onGuardado(guardado, `Producto guardado, pero la imagen no: ${leerError(err).mensaje}`)
        return
      }
      onGuardado(guardado)
    } catch (err) {
      const { mensaje, campos } = leerError(err)
      setMensaje(mensaje)
      setErrores(campos)
    } finally {
      setCargando(false)
    }
  }

  const mostrarActual = editando && producto.imagenUrl && imagen === null

  return (
    <form onSubmit={enviar} className="space-y-4" noValidate>
      <Alert>{mensaje}</Alert>

      {/* Imagen */}
      <div className="flex items-center gap-4">
        <div className="h-24 w-24 shrink-0 overflow-hidden rounded-xl ring-1 ring-gray-200">
          {vistaPrevia ? (
            <img src={vistaPrevia} alt="Vista previa" className="h-full w-full object-cover" />
          ) : (
            <ProductoImagen
              producto={{
                nombre: form.nombre,
                imagenUrl: mostrarActual ? producto.imagenUrl : null,
                categoriaNombre: categorias.find((c) => String(c.id) === form.categoriaId)?.nombre,
              }}
              className="h-full w-full"
            />
          )}
        </div>
        <div className="space-y-2">
          <label className="inline-block cursor-pointer rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium hover:bg-gray-50">
            {vistaPrevia || mostrarActual ? 'Cambiar imagen' : 'Elegir imagen'}
            <input type="file" accept="image/jpeg,image/png,image/webp" onChange={elegirImagen} className="sr-only" />
          </label>
          {(vistaPrevia || mostrarActual) && (
            <button type="button" onClick={quitarImagen} className="block text-sm text-red-600 hover:underline">
              Quitar imagen
            </button>
          )}
          <p className="text-xs text-gray-500">Opcional · JPG, PNG o WEBP. Se reduce automáticamente.</p>
          {errores.imagen && <p className="text-xs text-red-600">{errores.imagen}</p>}
        </div>
      </div>

      <Input label="Nombre" name="nombre" value={form.nombre} onChange={cambiar} error={errores.nombre} maxLength={150} />

      <div>
        <label htmlFor="categoriaId" className="mb-1 block text-sm font-medium text-gray-700">
          Categoría
        </label>
        <select
          id="categoriaId"
          name="categoriaId"
          value={form.categoriaId}
          onChange={cambiar}
          className={`block w-full rounded-lg border bg-white px-3 py-2.5 text-sm outline-none focus:ring-2 ${
            errores.categoriaId ? 'border-red-400 focus:ring-red-100' : 'border-gray-300 focus:border-marca-600 focus:ring-marca-100'
          }`}
        >
          <option value="">Selecciona una categoría</option>
          {categorias.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nombre}
            </option>
          ))}
        </select>
        {errores.categoriaId && <p className="mt-1 text-xs text-red-600">{errores.categoriaId}</p>}
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <Input label="Precio ($)" name="precio" inputMode="decimal" value={form.precio} onChange={cambiar} error={errores.precio} />
        <div>
          <Input label="Unidad" name="unidad" list="unidades" value={form.unidad} onChange={cambiar} error={errores.unidad} maxLength={20} />
          <datalist id="unidades">
            {UNIDADES.map((u) => (
              <option key={u} value={u} />
            ))}
          </datalist>
        </div>
        <Input label="Stock" name="stock" inputMode="decimal" value={form.stock} onChange={cambiar} error={errores.stock} />
      </div>

      <div>
        <label htmlFor="descripcion" className="mb-1 block text-sm font-medium text-gray-700">
          Descripción (opcional)
        </label>
        <textarea
          id="descripcion"
          name="descripcion"
          rows={3}
          maxLength={2000}
          value={form.descripcion}
          onChange={cambiar}
          className="block w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm outline-none focus:border-marca-600 focus:ring-2 focus:ring-marca-100"
        />
        {errores.descripcion && <p className="mt-1 text-xs text-red-600">{errores.descripcion}</p>}
      </div>

      <div className="flex justify-end gap-3 pt-2">
        <Button type="button" variante="secundario" onClick={onCancelar}>
          Cancelar
        </Button>
        <Button type="submit" cargando={cargando}>
          {editando ? 'Guardar cambios' : 'Crear producto'}
        </Button>
      </div>
    </form>
  )
}
