// eslint-disable-next-line react-refresh/only-export-components
export const ESTADOS_PEDIDO = {
  PENDIENTE: { texto: 'Pendiente', clase: 'bg-amber-50 text-amber-700 ring-amber-200' },
  ACEPTADO: { texto: 'Aceptado', clase: 'bg-blue-50 text-blue-700 ring-blue-200' },
  EN_CAMINO: { texto: 'En camino', clase: 'bg-indigo-50 text-indigo-700 ring-indigo-200' },
  ENTREGADO: { texto: 'Entregado', clase: 'bg-marca-50 text-marca-700 ring-marca-100' },
  CANCELADO: { texto: 'Cancelado', clase: 'bg-gray-100 text-gray-600 ring-gray-200' },
}

/** Etiqueta de color con el estado del pedido. */
export default function EstadoPedido({ estado }) {
  const e = ESTADOS_PEDIDO[estado] ?? { texto: estado, clase: 'bg-gray-100 text-gray-600 ring-gray-200' }
  return <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1 ${e.clase}`}>{e.texto}</span>
}
