import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { obtenerProducto } from '../../api/catalogoService'
import { useAuth } from '../../context/AuthContext'
import { leerError } from '../../utils/errores'
import { formatoCantidad, formatoPrecio } from '../../utils/formato'
import { resenasProductor } from '../../api/calificacionService'
import AgregarAlCarrito from '../../components/AgregarAlCarrito'
import { ResumenEstrellas } from '../../components/Estrellas'
import ListaResenas from '../../components/ListaResenas'
import OrigenProducto from '../../components/OrigenProducto'
import ProductoImagen from '../../components/ProductoImagen'
import Spinner from '../../components/ui/Spinner'

export default function ProductoDetallePage() {
  const { id } = useParams()
  const { usuario } = useAuth()
  const location = useLocation()
  // Se guarda el id junto con el resultado: si no coincide con la URL, aún está cargando
  const [estado, setEstado] = useState({ id: null, producto: null, error: '' })

  useEffect(() => {
    let vigente = true
    obtenerProducto(id)
      .then((producto) => vigente && setEstado({ id, producto, error: '' }))
      .catch((e) => vigente && setEstado({ id, producto: null, error: leerError(e).mensaje }))
    return () => {
      vigente = false
    }
  }, [id])

  const listo = estado.id === id
  const producto = listo ? estado.producto : null
  const error = listo ? estado.error : ''

  if (error) {
    return (
      <div className="py-20 text-center">
        <p className="text-4xl">🧺</p>
        <p className="mt-2 font-medium">{error}</p>
        <Link to="/catalogo" className="mt-4 inline-block font-semibold text-marca-700 hover:underline">
          Volver al catálogo
        </Link>
      </div>
    )
  }
  if (!producto) {
    return (
      <div className="py-20 text-center">
        <Spinner />
      </div>
    )
  }

  const agotado = Number(producto.stock) <= 0

  return (
    <section>
      <Link to="/catalogo" className="text-sm font-medium text-marca-700 hover:underline">
        ← Volver al catálogo
      </Link>

      <div className="mt-4 grid gap-8 overflow-hidden rounded-2xl bg-white p-4 ring-1 ring-gray-200 md:grid-cols-2 md:p-6">
        <ProductoImagen producto={producto} className="aspect-square w-full rounded-xl" />

        <div className="flex flex-col">
          <p className="text-sm font-medium text-marca-700">{producto.categoriaNombre}</p>
          <h1 className="mt-1 text-2xl font-bold">{producto.nombre}</h1>
          <p className="mt-1 flex flex-wrap items-center gap-2 text-sm text-gray-500">
            Productor: {producto.nombreFinca}
            <ResumenEstrellas promedio={producto.calificacionPromedio} total={producto.totalCalificaciones} />
          </p>

          <p className="mt-6 text-3xl font-bold">
            {formatoPrecio(producto.precio)}
            <span className="text-base font-normal text-gray-500"> / {producto.unidad}</span>
          </p>
          <p className={`mt-1 text-sm ${agotado ? 'text-red-600' : 'text-gray-600'}`}>
            {agotado ? 'Agotado' : `${formatoCantidad(producto.stock)} ${producto.unidad} disponibles`}
          </p>

          {producto.descripcion && <p className="mt-6 whitespace-pre-line text-gray-700">{producto.descripcion}</p>}

          <OrigenProducto productoId={producto.id} />

          <div className="mt-auto pt-8">
            {!usuario ? (
              <Link
                to="/login"
                state={{ desde: location.pathname }}
                className="inline-flex w-full items-center justify-center rounded-lg bg-marca-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-marca-700"
              >
                Inicia sesión para comprar
              </Link>
            ) : usuario.rol === 'COMPRADOR' ? (
              <AgregarAlCarrito producto={producto} agotado={agotado} />
            ) : (
              <p className="text-sm text-gray-500">Solo los compradores pueden hacer pedidos.</p>
            )}
          </div>
        </div>
      </div>

      <div className="mt-6">
        <ResenasDelProductor productorId={producto.productorId} />
      </div>
    </section>
  )
}

function ResenasDelProductor({ productorId }) {
  const [datos, setDatos] = useState(null)
  useEffect(() => {
    let vigente = true
    resenasProductor(productorId)
      .then((d) => vigente && setDatos(d))
      .catch(() => vigente && setDatos(null))
    return () => {
      vigente = false
    }
  }, [productorId])
  return <ListaResenas datos={datos} />
}
