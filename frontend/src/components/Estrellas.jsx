import { useState } from 'react'

const RUTA =
  'M12 2.5l2.9 6 6.6.9-4.8 4.6 1.2 6.5L12 17.4l-5.9 3.1 1.2-6.5L2.5 9.4l6.6-.9L12 2.5z'

function Estrella({ relleno, className }) {
  return (
    <svg viewBox="0 0 24 24" className={className} aria-hidden="true">
      <path d={RUTA} className={relleno ? 'fill-amber-400' : 'fill-gray-200'} />
    </svg>
  )
}

/**
 * Muestra de 1 a 5 estrellas. Con onChange se vuelve un selector (para calificar).
 * valor puede tener decimales al mostrar (4.6 rellena 5 estrellas al redondear).
 */
export default function Estrellas({ valor = 0, onChange, tamanio = 'h-4 w-4' }) {
  const [hover, setHover] = useState(0)
  const mostrado = hover || Math.round(valor)

  if (!onChange) {
    return (
      <span className="inline-flex" aria-label={`${valor} de 5 estrellas`}>
        {[1, 2, 3, 4, 5].map((n) => (
          <Estrella key={n} relleno={n <= mostrado} className={tamanio} />
        ))}
      </span>
    )
  }

  return (
    <span className="inline-flex gap-1" onMouseLeave={() => setHover(0)}>
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          onClick={() => onChange(n)}
          onMouseEnter={() => setHover(n)}
          aria-label={`${n} estrella${n > 1 ? 's' : ''}`}
          className="rounded transition hover:scale-110"
        >
          <Estrella relleno={n <= mostrado} className={tamanio} />
        </button>
      ))}
    </span>
  )
}

/** "⭐ 4.6 (12)" compacto para tarjetas. No muestra nada si aún no hay calificaciones. */
export function ResumenEstrellas({ promedio, total }) {
  if (!total) return <span className="text-xs text-gray-400">Sin calificaciones</span>
  return (
    <span className="inline-flex items-center gap-1 text-xs text-gray-600">
      <Estrella relleno className="h-3.5 w-3.5" />
      <span className="font-semibold">{Number(promedio).toFixed(1)}</span>
      <span className="text-gray-400">({total})</span>
    </span>
  )
}
