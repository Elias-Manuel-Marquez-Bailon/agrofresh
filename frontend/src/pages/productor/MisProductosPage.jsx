import { useCallback, useEffect, useState } from 'react'
import { cambiarEstadoProducto, listarCategorias, misProductos } from '../../api/catalogoService'
import { leerError } from '../../utils/errores'
import { formatoCantidad, formatoPrecio } from '../../utils/formato'
import ProductoImagen from '../../components/ProductoImagen'
import ProductoForm from './ProductoForm'
import Modal from '../../components/ui/Modal'
import Button from '../../components/ui/Button'
import Alert from '../../components/ui/Alert'
import Spinner from '../../components/ui/Spinner'

export default function MisProductosPage() {
  const [productos, setProductos] = useState(null)
  const [categorias, setCategorias] = useState([])
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState({ tipo: 'exito', texto: '' })
  const [modal, setModal] = useState({ abierto: false, producto: null })
  const [cambiando, setCambiando] = useState(null)

  const cargar = useCallback(() => {
    misProductos()
      .then(setProductos)
      .catch((e) => setError(leerError(e).mensaje))
  }, [])

  useEffect(() => {
    cargar()
    listarCategorias().then(setCategorias).catch(() => setCategorias([]))
  }, [cargar])

  const cerrarModal = useCallback(() => setModal({ abierto: false, producto: null }), [])

  const alGuardar = (guardado, advertencia) => {
    const editando = !!modal.producto
    cerrarModal()
    setAviso(
      advertencia
        ? { tipo: 'error', texto: advertencia }
        : { tipo: 'exito', texto: editando ? `"${guardado.nombre}" se actualizó` : `"${guardado.nombre}" se publicó en el catálogo` },
    )
    cargar()
  }

  const alternarEstado = async (p) => {
    setCambiando(p.id)
    setAviso({ tipo: 'exito', texto: '' })
    try {
      const actualizado = await cambiarEstadoProducto(p.id, !p.activo)
      setProductos((lista) => lista.map((x) => (x.id === p.id ? actualizado : x)))
    } catch (e) {
      setAviso({ tipo: 'error', texto: leerError(e).mensaje })
    } finally {
      setCambiando(null)
    }
  }

  return (
    <section>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Mis productos</h1>
          <p className="mt-1 text-gray-500">Publica tu cosecha y controla precio, stock y visibilidad</p>
        </div>
        <Button onClick={() => setModal({ abierto: true, producto: null })}>+ Nuevo producto</Button>
      </div>

      <div className="mt-6 space-y-4">
        <Alert>{error}</Alert>
        <Alert tipo={aviso.tipo}>{aviso.texto}</Alert>

        {!productos && !error ? (
          <div className="py-20 text-center">
            <Spinner />
          </div>
        ) : productos?.length === 0 ? (
          <div className="rounded-xl bg-white py-16 text-center ring-1 ring-gray-200">
            <p className="text-4xl">🌱</p>
            <p className="mt-2 font-medium">Aún no tienes productos</p>
            <p className="text-sm text-gray-500">Crea el primero para que aparezca en el catálogo</p>
          </div>
        ) : (
          productos && (
            <div className="overflow-x-auto rounded-xl bg-white ring-1 ring-gray-200">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-gray-200 bg-gray-50 text-xs uppercase text-gray-500">
                  <tr>
                    <th className="px-4 py-3">Producto</th>
                    <th className="px-4 py-3">Categoría</th>
                    <th className="px-4 py-3 text-right">Precio</th>
                    <th className="px-4 py-3 text-right">Stock</th>
                    <th className="px-4 py-3">Estado</th>
                    <th className="px-4 py-3 text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {productos.map((p) => (
                    <tr key={p.id} className={p.activo ? '' : 'bg-gray-50 text-gray-500'}>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-3">
                          <ProductoImagen producto={p} className="h-10 w-10 shrink-0 rounded-lg [&>span]:text-xl" />
                          <span className="font-medium">{p.nombre}</span>
                        </div>
                      </td>
                      <td className="px-4 py-3">{p.categoriaNombre}</td>
                      <td className="px-4 py-3 text-right whitespace-nowrap">
                        {formatoPrecio(p.precio)} <span className="text-xs text-gray-500">/ {p.unidad}</span>
                      </td>
                      <td className={`px-4 py-3 text-right ${Number(p.stock) <= 0 ? 'text-red-600' : ''}`}>
                        {formatoCantidad(p.stock)}
                      </td>
                      <td className="px-4 py-3">
                        <span
                          className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                            p.activo ? 'bg-marca-50 text-marca-700' : 'bg-gray-200 text-gray-600'
                          }`}
                        >
                          {p.activo ? 'Activo' : 'Inactivo'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right whitespace-nowrap">
                        <button onClick={() => setModal({ abierto: true, producto: p })} className="font-medium text-marca-700 hover:underline">
                          Editar
                        </button>
                        <button
                          onClick={() => alternarEstado(p)}
                          disabled={cambiando === p.id}
                          className="ml-4 font-medium text-gray-600 hover:underline disabled:opacity-50"
                        >
                          {p.activo ? 'Desactivar' : 'Activar'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        )}
      </div>

      <Modal abierto={modal.abierto} titulo={modal.producto ? 'Editar producto' : 'Nuevo producto'} onCerrar={cerrarModal}>
        <ProductoForm key={modal.producto?.id ?? 'nuevo'} producto={modal.producto} categorias={categorias} onGuardado={alGuardar} onCancelar={cerrarModal} />
      </Modal>
    </section>
  )
}
