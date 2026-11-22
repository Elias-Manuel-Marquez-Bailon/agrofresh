import { useEffect, useState } from 'react'
import { obtenerTrazabilidad } from '../api/pedidoService'
import { leerError } from '../utils/errores'
import { formatoCantidad, formatoDia, formatoFecha, METODOS_CULTIVO } from '../utils/formato'
import Alert from './ui/Alert'
import Modal from './ui/Modal'
import Spinner from './ui/Spinner'

function Paso({ icono, titulo, detalle, pendiente }) {
  return (
    <li className="relative pb-5 pl-9 last:pb-0">
      <span className={`absolute left-0 top-0 flex h-7 w-7 items-center justify-center rounded-full text-sm ${pendiente ? 'bg-gray-100 grayscale' : 'bg-marca-50'}`}>
        {icono}
      </span>
      <p className={`text-sm font-medium ${pendiente ? 'text-gray-400' : ''}`}>{titulo}</p>
      <p className="text-xs text-gray-500">{detalle}</p>
    </li>
  )
}

/** Línea de tiempo siembra → cosecha → pedido → entrega de cada producto. */
export default function TrazabilidadModal({ pedidoId, onCerrar }) {
  const [estado, setEstado] = useState({ id: null, datos: null, error: '' })

  useEffect(() => {
    if (!pedidoId) return
    let vigente = true
    obtenerTrazabilidad(pedidoId)
      .then((datos) => vigente && setEstado({ id: pedidoId, datos, error: '' }))
      .catch((e) => vigente && setEstado({ id: pedidoId, datos: null, error: leerError(e).mensaje }))
    return () => {
      vigente = false
    }
  }, [pedidoId])

  if (!pedidoId) return null
  const listo = estado.id === pedidoId
  const t = listo ? estado.datos : null

  return (
    <Modal abierto titulo={`Trazabilidad del pedido #${pedidoId}`} onCerrar={onCerrar}>
      {listo && estado.error && <Alert>{estado.error}</Alert>}
      {!listo && (
        <div className="py-10 text-center">
          <Spinner />
        </div>
      )}
      {t && (
        <div className="space-y-5">
          <p className="text-sm text-gray-600">
            Productor: <span className="font-medium">{t.nombreFinca}</span>
            {t.ubicacion && ` · ${t.ubicacion}`}
          </p>
          {t.productos.map((p) => (
            <div key={p.productoId} className="rounded-xl p-4 ring-1 ring-gray-200">
              <p className="mb-3 font-semibold">
                {p.nombreProducto}{' '}
                <span className="text-sm font-normal text-gray-500">
                  · {formatoCantidad(p.cantidad)} {p.unidad}
                </span>
              </p>
              <ol>
                {p.lote ? (
                  <>
                    <Paso
                      icono="🌱"
                      titulo={`Siembra · ${formatoDia(p.lote.fechaSiembra)}`}
                      detalle={[p.lote.parcela, METODOS_CULTIVO[p.lote.metodoCultivo]].filter(Boolean).join(' · ')}
                    />
                    <Paso icono="🌾" titulo={`Cosecha · ${formatoDia(p.lote.fechaCosecha)}`} detalle={p.lote.notas ?? ''} />
                  </>
                ) : (
                  <Paso icono="🌱" titulo="Origen no registrado" detalle="El productor no indicó el lote de este producto" pendiente />
                )}
                <Paso icono="🧾" titulo={`Pedido · ${formatoFecha(t.fechaPedido)}`} detalle="" />
                <Paso
                  icono="🚚"
                  titulo={t.fechaEntrega ? `Entrega · ${formatoFecha(t.fechaEntrega)}` : 'Entrega pendiente'}
                  detalle={t.estado === 'CANCELADO' ? 'Pedido cancelado' : ''}
                  pendiente={!t.fechaEntrega}
                />
              </ol>
            </div>
          ))}
        </div>
      )}
    </Modal>
  )
}
