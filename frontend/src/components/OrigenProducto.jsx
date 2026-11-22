import { useEffect, useState } from 'react'
import { origenProducto } from '../api/loteService'
import { formatoDia, METODOS_CULTIVO } from '../utils/formato'

/** Sección "Origen" del detalle: últimos lotes cosechados. No muestra nada si no hay. */
export default function OrigenProducto({ productoId }) {
  const [lotes, setLotes] = useState([])

  useEffect(() => {
    let vigente = true
    origenProducto(productoId)
      .then((l) => vigente && setLotes(l))
      .catch(() => vigente && setLotes([]))
    return () => {
      vigente = false
    }
  }, [productoId])

  if (lotes.length === 0) return null
  return (
    <div className="mt-6 rounded-xl bg-marca-50 p-4">
      <p className="text-sm font-semibold text-marca-800">🌾 Origen</p>
      <ul className="mt-2 space-y-1 text-sm text-gray-700">
        {lotes.map((l) => (
          <li key={l.id}>
            Cosechado el {formatoDia(l.fechaCosecha)}
            {l.metodoCultivo && ` · ${METODOS_CULTIVO[l.metodoCultivo]}`}
            {l.parcela && ` · ${l.parcela}`}
            <span className="text-gray-500"> (sembrado el {formatoDia(l.fechaSiembra)})</span>
          </li>
        ))}
      </ul>
    </div>
  )
}
