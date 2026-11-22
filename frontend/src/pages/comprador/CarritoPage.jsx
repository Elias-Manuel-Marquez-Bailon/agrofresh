import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { crearPedido } from '../../api/pedidoService'
import { useCarrito } from '../../context/CarritoContext'
import { leerError } from '../../utils/errores'
import { formatoCantidad, formatoPrecio } from '../../utils/formato'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'

export default function CarritoPage() {
  const { items, total, cambiarCantidad, quitar, vaciar } = useCarrito()
  const navigate = useNavigate()
  const [direccion, setDireccion] = useState('')
  const [errores, setErrores] = useState({})
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  // Agrupado por productor: cada grupo se convierte en un pedido.
  const grupos = Object.values(
    items.reduce((acc, i) => {
      const k = i.productorId ?? i.nombreFinca
      acc[k] ??= { nombreFinca: i.nombreFinca, items: [] }
      acc[k].items.push(i)
      return acc
    }, {}),
  )

  const cantidadInvalida = items.some((i) => !(i.cantidad > 0) || i.cantidad > i.stock)

  const confirmar = async (e) => {
    e.preventDefault()
    setError('')
    if (!direccion.trim()) {
      setErrores({ direccionEntrega: 'La dirección de entrega es obligatoria' })
      return
    }
    setErrores({})
    setEnviando(true)
    try {
      const { mensaje } = await crearPedido(
        direccion.trim(),
        items.map((i) => ({ productoId: i.productoId, cantidad: i.cantidad })),
      )
      vaciar()
      navigate('/comprador/pedidos', { state: { mensaje } })
    } catch (err) {
      const { mensaje, campos } = leerError(err)
      setError(mensaje)
      setErrores(campos)
    } finally {
      setEnviando(false)
    }
  }

  if (items.length === 0) {
    return (
      <div className="py-20 text-center">
        <p className="text-4xl">🛒</p>
        <p className="mt-2 font-medium">Tu carrito está vacío</p>
        <Link to="/catalogo" className="mt-4 inline-block font-semibold text-marca-700 hover:underline">
          Ir al catálogo
        </Link>
      </div>
    )
  }

  return (
    <section>
      <h1 className="text-2xl font-bold">Mi carrito</h1>
      {grupos.length > 1 && (
        <p className="mt-1 text-sm text-gray-500">
          Tienes productos de {grupos.length} productores: se generará un pedido para cada uno.
        </p>
      )}

      <div className="mt-6 grid gap-6 lg:grid-cols-3">
        <div className="space-y-4 lg:col-span-2">
          {grupos.map((g) => (
            <div key={g.nombreFinca} className="rounded-2xl bg-white p-5 ring-1 ring-gray-200">
              <p className="text-sm font-semibold text-marca-700">{g.nombreFinca}</p>
              <ul className="mt-3 divide-y divide-gray-100">
                {g.items.map((i) => {
                  const excede = i.cantidad > i.stock
                  return (
                    <li key={i.productoId} className="flex flex-wrap items-center gap-3 py-3">
                      <div className="min-w-0 flex-1">
                        <Link to={`/catalogo/${i.productoId}`} className="font-medium hover:underline">
                          {i.nombre}
                        </Link>
                        <p className="text-sm text-gray-500">
                          {formatoPrecio(i.precio)} / {i.unidad}
                        </p>
                        {excede && (
                          <p className="text-xs text-red-600">
                            Solo hay {formatoCantidad(i.stock)} {i.unidad} disponibles
                          </p>
                        )}
                      </div>
                      <input
                        type="number"
                        min="0.01"
                        step="0.01"
                        max={i.stock}
                        value={i.cantidad}
                        onChange={(e) => cambiarCantidad(i.productoId, Number(e.target.value))}
                        className={`w-24 rounded-lg border px-2 py-1.5 text-sm ${excede ? 'border-red-400' : 'border-gray-300'}`}
                        aria-label={`Cantidad de ${i.nombre}`}
                      />
                      <span className="w-24 text-right font-medium">{formatoPrecio(i.precio * i.cantidad)}</span>
                      <button
                        onClick={() => quitar(i.productoId)}
                        className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-600"
                        aria-label={`Quitar ${i.nombre}`}
                        title="Quitar"
                      >
                        <svg className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                          <path strokeLinecap="round" d="M6 6l12 12M18 6L6 18" />
                        </svg>
                      </button>
                    </li>
                  )
                })}
              </ul>
            </div>
          ))}
        </div>

        <form onSubmit={confirmar} className="h-fit space-y-4 rounded-2xl bg-white p-5 ring-1 ring-gray-200">
          <div className="flex justify-between text-lg font-bold">
            <span>Total</span>
            <span>{formatoPrecio(total)}</span>
          </div>
          <Input
            label="Dirección de entrega"
            name="direccionEntrega"
            value={direccion}
            maxLength={255}
            onChange={(e) => setDireccion(e.target.value)}
            error={errores.direccionEntrega}
            placeholder="Calle, número, colonia, ciudad"
          />
          <Alert>{error}</Alert>
          <Button type="submit" className="w-full" cargando={enviando} disabled={cantidadInvalida}>
            Confirmar pedido
          </Button>
          <p className="text-xs text-gray-500">
            El productor debe aceptar tu pedido. Puedes cancelarlo mientras siga pendiente.
          </p>
        </form>
      </div>
    </section>
  )
}
