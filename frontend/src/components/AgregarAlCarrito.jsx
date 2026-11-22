import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useCarrito } from '../context/CarritoContext'
import { formatoCantidad } from '../utils/formato'
import Button from './ui/Button'

/** Selector de cantidad + botón para el detalle del producto (solo compradores). */
export default function AgregarAlCarrito({ producto, agotado }) {
  const { items, agregar } = useCarrito()
  const [cantidad, setCantidad] = useState(1)
  const [agregado, setAgregado] = useState(false)

  const stock = Number(producto.stock)
  const enCarrito = items.find((i) => i.productoId === producto.id)?.cantidad ?? 0
  const restante = Math.max(stock - enCarrito, 0)
  const valida = cantidad > 0 && cantidad <= restante

  if (agotado) {
    return (
      <button disabled className="w-full cursor-not-allowed rounded-lg bg-gray-100 px-4 py-2.5 text-sm font-semibold text-gray-500">
        Agotado
      </button>
    )
  }

  const alAgregar = () => {
    agregar(producto, cantidad)
    setAgregado(true)
    setCantidad(1)
  }

  return (
    <div className="space-y-3">
      <div className="flex items-end gap-3">
        <div>
          <label htmlFor="cantidad" className="mb-1 block text-sm font-medium text-gray-700">
            Cantidad ({producto.unidad})
          </label>
          <input
            id="cantidad"
            type="number"
            min="0.01"
            step="0.01"
            max={restante}
            value={cantidad}
            onChange={(e) => {
              setCantidad(Number(e.target.value))
              setAgregado(false)
            }}
            className="w-28 rounded-lg border border-gray-300 px-3 py-2.5 text-sm outline-none focus:border-marca-600 focus:ring-2 focus:ring-marca-100"
          />
        </div>
        <Button className="flex-1" onClick={alAgregar} disabled={!valida}>
          Agregar al carrito
        </Button>
      </div>
      {enCarrito > 0 && (
        <p className="text-sm text-gray-600">
          {agregado ? '✓ Agregado. ' : ''}Llevas {formatoCantidad(enCarrito)} {producto.unidad} en el carrito.{' '}
          <Link to="/carrito" className="font-semibold text-marca-700 hover:underline">
            Ver carrito
          </Link>
        </p>
      )}
      {restante <= 0 && <p className="text-sm text-amber-700">Ya tienes en el carrito todo el stock disponible.</p>}
    </div>
  )
}
