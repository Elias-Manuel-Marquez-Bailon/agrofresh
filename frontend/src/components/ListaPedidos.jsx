import { useCallback, useEffect, useState } from 'react'
import { cambiarEstadoPedido } from '../api/pedidoService'
import { leerError } from '../utils/errores'
import { formatoCantidad, formatoFecha, formatoPrecio } from '../utils/formato'
import EstadoPedido, { ESTADOS_PEDIDO } from './EstadoPedido'
import AceptarPedidoModal from './AceptarPedidoModal'
import CalificarModal from './CalificarModal'
import Estrellas from './Estrellas'
import TrazabilidadModal from './TrazabilidadModal'
import Alert from './ui/Alert'
import Button from './ui/Button'
import Spinner from './ui/Spinner'

const TAMANIO = 10

/**
 * Acciones disponibles según quién mira el pedido (mismas reglas que el backend):
 *  - comprador: cancelar si está PENDIENTE
 *  - productor: aceptar / en camino / entregado, y cancelar si está PENDIENTE o ACEPTADO
 */
function accionesDe(vista, estado) {
  if (vista === 'comprador') {
    return estado === 'PENDIENTE' ? [{ estado: 'CANCELADO', texto: 'Cancelar pedido', variante: 'peligro' }] : []
  }
  switch (estado) {
    case 'PENDIENTE':
      return [
        { estado: 'ACEPTADO', texto: 'Aceptar', variante: 'primario' },
        { estado: 'CANCELADO', texto: 'Rechazar', variante: 'peligro' },
      ]
    case 'ACEPTADO':
      return [
        { estado: 'EN_CAMINO', texto: 'Marcar en camino', variante: 'primario' },
        { estado: 'CANCELADO', texto: 'Cancelar', variante: 'peligro' },
      ]
    case 'EN_CAMINO':
      return [{ estado: 'ENTREGADO', texto: 'Marcar entregado', variante: 'primario' }]
    default:
      return []
  }
}

const CONFIRMACION = {
  CANCELADO: '¿Seguro que quieres cancelar este pedido? Esta acción no se puede deshacer.',
}

/** Lista paginada de pedidos con filtro por estado. vista: "comprador" | "productor". */
export default function ListaPedidos({ titulo, subtitulo, vista, cargar, mensajeInicial = '' }) {
  const [estado, setEstado] = useState('')
  const [pagina, setPagina] = useState(0)
  const [resultado, setResultado] = useState(null)
  const [error, setError] = useState('')
  const [exito, setExito] = useState(mensajeInicial)
  const [procesando, setProcesando] = useState(null) // id del pedido que se está actualizando
  const [aceptando, setAceptando] = useState(null) // pedido en el modal de aceptar (elegir lotes)
  const [trazabilidad, setTrazabilidad] = useState(null) // id del pedido
  const [calificando, setCalificando] = useState(null) // pedido

  const recargar = useCallback(() => {
    return cargar({ estado, pagina, tamanio: TAMANIO })
      .then((r) => {
        setResultado(r)
        setError('')
      })
      .catch((e) => setError(leerError(e).mensaje))
  }, [cargar, estado, pagina])

  useEffect(() => {
    recargar()
  }, [recargar])

  const cambiar = async (pedido, nuevo, lotes) => {
    if (CONFIRMACION[nuevo] && !window.confirm(CONFIRMACION[nuevo])) return
    setProcesando(pedido.id)
    setExito('')
    setError('')
    try {
      const { mensaje } = await cambiarEstadoPedido(pedido.id, nuevo, lotes)
      setExito(`Pedido #${pedido.id}: ${mensaje.toLowerCase()}`)
      setAceptando(null)
      await recargar()
    } catch (e) {
      setError(leerError(e).mensaje)
      setAceptando(null)
    } finally {
      setProcesando(null)
    }
  }

  // "Aceptar" abre el modal para elegir lotes; el resto cambia directo.
  const alPulsar = (pedido, nuevo) => (nuevo === 'ACEPTADO' ? setAceptando(pedido) : cambiar(pedido, nuevo))

  const alCalificar = (mensaje) => {
    setCalificando(null)
    setExito(mensaje)
    recargar()
  }

  return (
    <section>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">{titulo}</h1>
          <p className="mt-1 text-gray-500">{subtitulo}</p>
        </div>
        <select
          value={estado}
          onChange={(e) => {
            setEstado(e.target.value)
            setPagina(0)
          }}
          className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm"
          aria-label="Filtrar por estado"
        >
          <option value="">Todos los estados</option>
          {Object.entries(ESTADOS_PEDIDO).map(([valor, { texto }]) => (
            <option key={valor} value={valor}>
              {texto}
            </option>
          ))}
        </select>
      </div>

      <div className="mt-4 space-y-3">
        <Alert tipo="exito">{exito}</Alert>
        <Alert>{error}</Alert>
      </div>

      {!resultado ? (
        !error && (
          <div className="py-20 text-center">
            <Spinner />
          </div>
        )
      ) : resultado.contenido.length === 0 ? (
        <div className="py-20 text-center text-gray-500">
          <p className="text-4xl">📦</p>
          <p className="mt-2">No hay pedidos {estado && `con estado "${ESTADOS_PEDIDO[estado].texto}"`}</p>
        </div>
      ) : (
        <div className="mt-6 space-y-4">
          {resultado.contenido.map((p) => (
            <article key={p.id} className="rounded-2xl bg-white p-5 ring-1 ring-gray-200">
              <div className="flex flex-wrap items-start justify-between gap-2">
                <div>
                  <p className="font-semibold">
                    Pedido #{p.id} <span className="ml-2 align-middle"><EstadoPedido estado={p.estado} /></span>
                  </p>
                  <p className="mt-1 text-sm text-gray-500">
                    {formatoFecha(p.createdAt)} ·{' '}
                    {vista === 'comprador' ? `Productor: ${p.nombreFinca}` : `Comprador: ${p.compradorNombre}`}
                  </p>
                  <p className="text-sm text-gray-500">Entrega: {p.direccionEntrega}</p>
                </div>
                <p className="text-lg font-bold">{formatoPrecio(p.total)}</p>
              </div>

              <ul className="mt-4 divide-y divide-gray-100 border-t border-gray-100 text-sm">
                {p.detalles.map((d) => (
                  <li key={d.productoId} className="flex justify-between gap-4 py-2">
                    <span>
                      {d.nombreProducto}{' '}
                      <span className="text-gray-500">
                        · {formatoCantidad(d.cantidad)} {d.unidad} × {formatoPrecio(d.precioUnitario)}
                      </span>
                    </span>
                    <span className="font-medium">{formatoPrecio(d.subtotal)}</span>
                  </li>
                ))}
              </ul>

              {p.calificacion && (
                <div className="mt-3 rounded-lg bg-gray-50 px-3 py-2 text-sm">
                  <Estrellas valor={p.calificacion.puntuacion} />
                  {p.calificacion.comentario && <p className="mt-1 text-gray-600">“{p.calificacion.comentario}”</p>}
                </div>
              )}

              <div className="mt-4 flex flex-wrap items-center justify-end gap-2">
                {p.estado !== 'CANCELADO' && (
                  <Button variante="secundario" className="py-2" onClick={() => setTrazabilidad(p.id)}>
                    Ver trazabilidad
                  </Button>
                )}
                {vista === 'comprador' && p.estado === 'ENTREGADO' && !p.calificacion && (
                  <Button className="py-2" onClick={() => setCalificando(p)}>
                    ⭐ Calificar
                  </Button>
                )}
                {accionesDe(vista, p.estado).map((a) => (
                  <Button
                    key={a.estado}
                    variante={a.variante}
                    className="py-2"
                    disabled={procesando !== null}
                    cargando={procesando === p.id && a.estado !== 'ACEPTADO'}
                    onClick={() => alPulsar(p, a.estado)}
                  >
                    {a.texto}
                  </Button>
                ))}
              </div>
            </article>
          ))}
        </div>
      )}

      {resultado && resultado.totalPaginas > 1 && (
        <div className="mt-6 flex items-center justify-center gap-3 text-sm">
          <Button variante="secundario" className="py-1.5" disabled={pagina === 0} onClick={() => setPagina(pagina - 1)}>
            Anterior
          </Button>
          <span>
            Página {pagina + 1} de {resultado.totalPaginas}
          </span>
          <Button
            variante="secundario"
            className="py-1.5"
            disabled={pagina + 1 >= resultado.totalPaginas}
            onClick={() => setPagina(pagina + 1)}
          >
            Siguiente
          </Button>
        </div>
      )}

      <AceptarPedidoModal
        key={aceptando?.id ?? 'aceptar'}
        pedido={aceptando}
        enviando={procesando !== null}
        onCerrar={() => setAceptando(null)}
        onConfirmar={(lotes) => cambiar(aceptando, 'ACEPTADO', lotes)}
      />
      <TrazabilidadModal pedidoId={trazabilidad} onCerrar={() => setTrazabilidad(null)} />
      <CalificarModal key={calificando?.id ?? 'calificar'} pedido={calificando} onCerrar={() => setCalificando(null)} onCalificado={alCalificar} />
    </section>
  )
}
