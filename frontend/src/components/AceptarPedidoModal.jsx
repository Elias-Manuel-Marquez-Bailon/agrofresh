import { useEffect, useState } from 'react'
import { misLotes } from '../api/loteService'
import { formatoCantidad, formatoDia } from '../utils/formato'
import Button from './ui/Button'
import Modal from './ui/Modal'

/** Al aceptar, el productor puede indicar (opcional) de qué lote cosechado sale cada producto. */
export default function AceptarPedidoModal({ pedido, onCerrar, onConfirmar, enviando }) {
  const [lotes, setLotes] = useState(null) // todos mis lotes cosechados
  const [seleccion, setSeleccion] = useState({}) // { productoId: loteId }

  useEffect(() => {
    if (!pedido) return
    misLotes()
      .then((l) => setLotes(l.filter((x) => x.fechaCosecha)))
      .catch(() => setLotes([]))
  }, [pedido])

  if (!pedido) return null

  const confirmar = () => {
    const elegidos = Object.fromEntries(Object.entries(seleccion).filter(([, v]) => v))
    onConfirmar(Object.keys(elegidos).length ? elegidos : undefined)
  }

  return (
    <Modal abierto titulo={`Aceptar pedido #${pedido.id}`} onCerrar={onCerrar}>
      <p className="text-sm text-gray-600">
        Se descontará el stock. Si quieres, indica de qué lote sale cada producto para la trazabilidad (opcional).
      </p>
      <div className="mt-4 space-y-3">
        {pedido.detalles.map((d) => {
          const opciones = (lotes ?? []).filter((l) => l.productoId === d.productoId)
          return (
            <div key={d.productoId}>
              <label className="mb-1 block text-sm font-medium">
                {d.nombreProducto}{' '}
                <span className="font-normal text-gray-500">
                  · {formatoCantidad(d.cantidad)} {d.unidad}
                </span>
              </label>
              <select
                value={seleccion[d.productoId] ?? ''}
                onChange={(e) => setSeleccion({ ...seleccion, [d.productoId]: e.target.value ? Number(e.target.value) : '' })}
                className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm"
                disabled={lotes === null}
              >
                <option value="">{opciones.length ? 'Sin lote' : 'No hay lotes cosechados de este producto'}</option>
                {opciones.map((l) => (
                  <option key={l.id} value={l.id}>
                    Cosecha {formatoDia(l.fechaCosecha)}
                    {l.parcela ? ` · ${l.parcela}` : ''}
                  </option>
                ))}
              </select>
            </div>
          )
        })}
      </div>
      <div className="mt-6 flex justify-end gap-2">
        <Button variante="secundario" onClick={onCerrar}>
          Volver
        </Button>
        <Button onClick={confirmar} cargando={enviando}>
          Aceptar pedido
        </Button>
      </div>
    </Modal>
  )
}
