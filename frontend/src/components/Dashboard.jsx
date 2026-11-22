import { useState } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  LabelList,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { descargarCsv, normalizarErrorBlob } from '../api/dashboardService'
import { leerError } from '../utils/errores'
import { formatoCantidad, formatoPrecio } from '../utils/formato'
import { ESTADOS_PEDIDO } from './EstadoPedido'
import Alert from './ui/Alert'
import Button from './ui/Button'

// Una sola serie por gráfica, en el verde de la marca (marca-600). Ejes y rejilla discretos.
const VERDE = '#16a34a'
const GRIS_EJE = '#6b7280'
const GRIS_REJILLA = '#e5e7eb'
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

/** "2026-11" -> "nov 26" */
const etiquetaMes = (periodo) => {
  const [a, m] = periodo.split('-')
  return `${MESES[Number(m) - 1]} ${a.slice(2)}`
}

/** $12,500 -> "$12.5k" para el eje (el tooltip muestra el valor completo). */
const precioCorto = (v) => (v >= 1000 ? `$${formatoCantidad(v / 1000)}k` : `$${formatoCantidad(v)}`)

const ejes = { stroke: GRIS_EJE, fontSize: 12, tickLine: false, axisLine: false }

/** Tooltip con el mismo estilo de las tarjetas. filas: [[etiqueta, valor], ...] */
function CajaTooltip({ titulo, filas }) {
  return (
    <div className="rounded-lg bg-white px-3 py-2 text-sm shadow-lg ring-1 ring-gray-200">
      <p className="font-semibold">{titulo}</p>
      {filas.map(([k, v]) => (
        <p key={k} className="text-gray-600">
          {k}: <span className="font-medium text-gray-900">{v}</span>
        </p>
      ))}
    </div>
  )
}

export function Tarjeta({ titulo, valor, detalle }) {
  return (
    <div className="rounded-2xl bg-white p-5 ring-1 ring-gray-200">
      <p className="text-sm text-gray-500">{titulo}</p>
      <p className="mt-1 text-2xl font-bold">{valor}</p>
      {detalle && <p className="mt-1 text-xs text-gray-500">{detalle}</p>}
    </div>
  )
}

export function Panel({ titulo, subtitulo, children, className = '' }) {
  return (
    <div className={`rounded-2xl bg-white p-5 ring-1 ring-gray-200 ${className}`}>
      <h2 className="font-semibold">{titulo}</h2>
      {subtitulo && <p className="text-sm text-gray-500">{subtitulo}</p>}
      <div className="mt-4">{children}</div>
    </div>
  )
}

function Vacio({ texto = 'Aún no hay datos' }) {
  return <p className="py-12 text-center text-sm text-gray-500">{texto}</p>
}

/** Línea de montos por mes (últimos 6 meses). etiqueta: "Ventas" o "Gasto". */
export function GraficaMeses({ datos, etiqueta = 'Ventas' }) {
  const hayDatos = datos?.some((d) => Number(d.monto) > 0)
  if (!hayDatos) return <Vacio texto="Sin movimientos en los últimos 6 meses" />
  const filas = datos.map((d) => ({ ...d, monto: Number(d.monto), mes: etiquetaMes(d.periodo) }))
  return (
    <div className="h-64">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={filas} margin={{ top: 8, right: 16, bottom: 0, left: 0 }}>
          <CartesianGrid vertical={false} stroke={GRIS_REJILLA} />
          <XAxis dataKey="mes" {...ejes} />
          <YAxis tickFormatter={precioCorto} width={56} {...ejes} />
          <Tooltip
            cursor={{ stroke: GRIS_EJE, strokeDasharray: '3 3' }}
            content={({ active, payload }) =>
              active && payload?.length ? (
                <CajaTooltip
                  titulo={payload[0].payload.mes}
                  filas={[
                    [etiqueta, formatoPrecio(payload[0].payload.monto)],
                    ['Pedidos', payload[0].payload.pedidos],
                  ]}
                />
              ) : null
            }
          />
          <Line
            type="monotone"
            dataKey="monto"
            stroke={VERDE}
            strokeWidth={2}
            dot={{ r: 4, fill: VERDE, stroke: '#fff', strokeWidth: 2 }}
            activeDot={{ r: 6, stroke: '#fff', strokeWidth: 2 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}

/**
 * Barras horizontales con el nombre a la izquierda y el valor al final de la barra.
 * datos: [{ nombre, valor, extra? }]; formato: cómo mostrar el valor.
 */
export function BarrasHorizontales({ datos, formato = formatoPrecio, etiquetaValor = 'Monto', vacio }) {
  if (!datos?.length || datos.every((d) => !Number(d.valor))) return <Vacio texto={vacio} />
  const filas = datos.map((d) => ({ ...d, valor: Number(d.valor) }))
  return (
    <div style={{ height: Math.max(filas.length * 44, 120) }}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={filas} layout="vertical" margin={{ top: 0, right: 72, bottom: 0, left: 0 }}>
          <XAxis type="number" hide />
          <YAxis type="category" dataKey="nombre" width={130} {...ejes} tick={{ fill: '#374151', fontSize: 13 }} />
          <Tooltip
            cursor={{ fill: '#f3f4f6' }}
            content={({ active, payload }) =>
              active && payload?.length ? (
                <CajaTooltip
                  titulo={payload[0].payload.nombre}
                  filas={[[etiquetaValor, formato(payload[0].payload.valor)], ...(payload[0].payload.extra ?? [])]}
                />
              ) : null
            }
          />
          <Bar dataKey="valor" fill={VERDE} radius={[0, 4, 4, 0]} barSize={20}>
            <LabelList dataKey="valor" position="right" formatter={formato} fill="#374151" fontSize={12} />
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}

/** Conteo de pedidos por estado: barra por estado con su etiqueta (no depende solo del color). */
export function PedidosPorEstado({ datos }) {
  const total = datos?.reduce((s, e) => s + e.cantidad, 0) ?? 0
  if (!total) return <Vacio texto="Aún no hay pedidos" />
  return (
    <ul className="space-y-3">
      {datos.map((e) => {
        const pct = (e.cantidad / total) * 100
        return (
          <li key={e.estado} title={`${formatoPrecio(e.monto)} en ${e.cantidad} pedido(s)`}>
            <div className="flex justify-between text-sm">
              <span>{ESTADOS_PEDIDO[e.estado]?.texto ?? e.estado}</span>
              <span className="font-medium">
                {e.cantidad} <span className="font-normal text-gray-500">· {Math.round(pct)}%</span>
              </span>
            </div>
            <div className="mt-1 h-2 rounded-full bg-gray-100">
              <div
                className={`h-2 rounded-full ${e.estado === 'CANCELADO' ? 'bg-gray-400' : 'bg-marca-600'}`}
                style={{ width: `${pct}%` }}
              />
            </div>
          </li>
        )
      })}
    </ul>
  )
}

/** Formulario de exportación: estado + rango de fechas opcionales. */
export function ExportarCsv({ rol }) {
  const [filtros, setFiltros] = useState({ estado: '', desde: '', hasta: '' })
  const [descargando, setDescargando] = useState(false)
  const [error, setError] = useState('')

  const cambiar = (e) => setFiltros({ ...filtros, [e.target.name]: e.target.value })

  const exportar = async () => {
    setError('')
    if (filtros.desde && filtros.hasta && filtros.desde > filtros.hasta) {
      setError('La fecha "desde" no puede ser posterior a "hasta"')
      return
    }
    setDescargando(true)
    try {
      await descargarCsv(rol, filtros)
    } catch (e) {
      setError(leerError(await normalizarErrorBlob(e)).mensaje)
    } finally {
      setDescargando(false)
    }
  }

  const claseCampo = 'mt-1 w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm'
  return (
    <Panel titulo="Exportar pedidos a CSV" subtitulo="Todos los filtros son opcionales. Se abre bien en Excel.">
      <div className="grid gap-3 sm:grid-cols-4 sm:items-end">
        <label className="text-sm">
          Estado
          <select name="estado" value={filtros.estado} onChange={cambiar} className={claseCampo}>
            <option value="">Todos</option>
            {Object.entries(ESTADOS_PEDIDO).map(([valor, { texto }]) => (
              <option key={valor} value={valor}>
                {texto}
              </option>
            ))}
          </select>
        </label>
        <label className="text-sm">
          Desde
          <input type="date" name="desde" value={filtros.desde} onChange={cambiar} className={claseCampo} />
        </label>
        <label className="text-sm">
          Hasta
          <input type="date" name="hasta" value={filtros.hasta} onChange={cambiar} className={claseCampo} />
        </label>
        <Button onClick={exportar} cargando={descargando} className="py-2">
          ⬇ Descargar CSV
        </Button>
      </div>
      <div className="mt-3">
        <Alert>{error}</Alert>
      </div>
    </Panel>
  )
}
