import { useCallback, useEffect, useState } from 'react'
import { misProductos } from '../../api/catalogoService'
import { actualizarLote, crearLote, eliminarLote, misLotes } from '../../api/loteService'
import { leerError } from '../../utils/errores'
import { formatoDia, hoyISO, METODOS_CULTIVO } from '../../utils/formato'
import Alert from '../../components/ui/Alert'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Modal from '../../components/ui/Modal'
import Spinner from '../../components/ui/Spinner'

const VACIO = { productoId: '', fechaSiembra: '', fechaCosecha: '', parcela: '', metodoCultivo: '', notas: '' }

function LoteForm({ lote, productos, onGuardado }) {
  const [datos, setDatos] = useState(() =>
    lote
      ? {
          productoId: String(lote.productoId),
          fechaSiembra: lote.fechaSiembra ?? '',
          fechaCosecha: lote.fechaCosecha ?? '',
          parcela: lote.parcela ?? '',
          metodoCultivo: lote.metodoCultivo ?? '',
          notas: lote.notas ?? '',
        }
      : VACIO,
  )
  const [errores, setErrores] = useState({})
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  const cambiar = (e) => setDatos({ ...datos, [e.target.name]: e.target.value })

  const enviar = async (e) => {
    e.preventDefault()
    const errs = {}
    if (!datos.productoId) errs.productoId = 'Elige un producto'
    if (!datos.fechaSiembra) errs.fechaSiembra = 'La fecha de siembra es obligatoria'
    if (datos.fechaCosecha && datos.fechaSiembra && datos.fechaCosecha < datos.fechaSiembra) {
      errs.fechaCosecha = 'La cosecha no puede ser anterior a la siembra'
    }
    setErrores(errs)
    if (Object.keys(errs).length) return

    const cuerpo = {
      productoId: Number(datos.productoId),
      fechaSiembra: datos.fechaSiembra,
      fechaCosecha: datos.fechaCosecha || null,
      parcela: datos.parcela,
      metodoCultivo: datos.metodoCultivo || null,
      notas: datos.notas,
    }
    setEnviando(true)
    setError('')
    try {
      const guardado = lote ? await actualizarLote(lote.id, cuerpo) : await crearLote(cuerpo)
      onGuardado(guardado)
    } catch (err) {
      const { mensaje, campos } = leerError(err)
      setError(mensaje)
      setErrores(campos)
    } finally {
      setEnviando(false)
    }
  }

  const claseSelect = (err) =>
    `block w-full rounded-lg border bg-white px-3 py-2.5 text-sm ${err ? 'border-red-400' : 'border-gray-300'}`

  return (
    <form onSubmit={enviar} className="space-y-4">
      <div>
        <label htmlFor="productoId" className="mb-1 block text-sm font-medium text-gray-700">
          Producto
        </label>
        <select id="productoId" name="productoId" value={datos.productoId} onChange={cambiar} className={claseSelect(errores.productoId)}>
          <option value="">Selecciona…</option>
          {productos.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nombre}
            </option>
          ))}
        </select>
        {errores.productoId && <p className="mt-1 text-xs text-red-600">{errores.productoId}</p>}
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Input label="Fecha de siembra" type="date" name="fechaSiembra" max={hoyISO()} value={datos.fechaSiembra} onChange={cambiar} error={errores.fechaSiembra} />
        <Input label="Fecha de cosecha (opcional)" type="date" name="fechaCosecha" max={hoyISO()} value={datos.fechaCosecha} onChange={cambiar} error={errores.fechaCosecha} />
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Input label="Parcela" name="parcela" maxLength={100} value={datos.parcela} onChange={cambiar} error={errores.parcela} placeholder="Ej. Parcela norte" />
        <div>
          <label htmlFor="metodoCultivo" className="mb-1 block text-sm font-medium text-gray-700">
            Método de cultivo
          </label>
          <select id="metodoCultivo" name="metodoCultivo" value={datos.metodoCultivo} onChange={cambiar} className={claseSelect()}>
            <option value="">Sin especificar</option>
            {Object.entries(METODOS_CULTIVO).map(([valor, texto]) => (
              <option key={valor} value={valor}>
                {texto}
              </option>
            ))}
          </select>
        </div>
      </div>
      <div>
        <label htmlFor="notas" className="mb-1 block text-sm font-medium text-gray-700">
          Notas
        </label>
        <textarea id="notas" name="notas" rows={3} maxLength={2000} value={datos.notas} onChange={cambiar} className="block w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm" />
      </div>
      <Alert>{error}</Alert>
      <Button type="submit" className="w-full" cargando={enviando}>
        {lote ? 'Guardar cambios' : 'Registrar lote'}
      </Button>
    </form>
  )
}

export default function LotesPage() {
  const [productos, setProductos] = useState([])
  const [filtro, setFiltro] = useState('')
  const [lotes, setLotes] = useState(null)
  const [aviso, setAviso] = useState({ tipo: 'exito', texto: '' })
  const [modal, setModal] = useState({ abierto: false, lote: null })

  const cargar = useCallback(() => {
    misLotes(filtro)
      .then(setLotes)
      .catch((e) => setAviso({ tipo: 'error', texto: leerError(e).mensaje }))
  }, [filtro])

  useEffect(() => {
    cargar()
  }, [cargar])

  useEffect(() => {
    misProductos().then(setProductos).catch(() => setProductos([]))
  }, [])

  const cerrar = useCallback(() => setModal({ abierto: false, lote: null }), [])

  const alGuardar = () => {
    setAviso({ tipo: 'exito', texto: modal.lote ? 'Lote actualizado' : 'Lote registrado' })
    cerrar()
    cargar()
  }

  const eliminar = async (l) => {
    if (!window.confirm(`¿Eliminar el lote de ${l.nombreProducto} sembrado el ${formatoDia(l.fechaSiembra)}?`)) return
    try {
      await eliminarLote(l.id)
      setAviso({ tipo: 'exito', texto: 'Lote eliminado' })
      cargar()
    } catch (e) {
      setAviso({ tipo: 'error', texto: leerError(e).mensaje })
    }
  }

  return (
    <section>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Lotes</h1>
          <p className="mt-1 text-gray-500">Registra siembra y cosecha para que tus compradores conozcan el origen.</p>
        </div>
        <div className="flex gap-2">
          <select value={filtro} onChange={(e) => setFiltro(e.target.value)} className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm" aria-label="Filtrar por producto">
            <option value="">Todos los productos</option>
            {productos.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nombre}
              </option>
            ))}
          </select>
          <Button onClick={() => setModal({ abierto: true, lote: null })} disabled={productos.length === 0}>
            + Nuevo lote
          </Button>
        </div>
      </div>

      <div className="mt-4">
        <Alert tipo={aviso.tipo}>{aviso.texto}</Alert>
      </div>

      {!lotes ? (
        <div className="py-20 text-center">
          <Spinner />
        </div>
      ) : lotes.length === 0 ? (
        <div className="mt-6 rounded-xl bg-white py-16 text-center ring-1 ring-gray-200">
          <p className="text-4xl">🌱</p>
          <p className="mt-2 font-medium">Aún no hay lotes registrados</p>
          {productos.length === 0 && <p className="text-sm text-gray-500">Primero publica un producto en "Mis productos".</p>}
        </div>
      ) : (
        <div className="mt-6 overflow-x-auto rounded-xl bg-white ring-1 ring-gray-200">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-500">
              <tr>
                <th className="px-4 py-3">Producto</th>
                <th className="px-4 py-3">Siembra</th>
                <th className="px-4 py-3">Cosecha</th>
                <th className="px-4 py-3">Parcela</th>
                <th className="px-4 py-3">Método</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {lotes.map((l) => (
                <tr key={l.id}>
                  <td className="px-4 py-3 font-medium">{l.nombreProducto}</td>
                  <td className="px-4 py-3">{formatoDia(l.fechaSiembra)}</td>
                  <td className="px-4 py-3">
                    {l.fechaCosecha ? formatoDia(l.fechaCosecha) : <span className="rounded-full bg-amber-50 px-2 py-0.5 text-xs text-amber-700">En cultivo</span>}
                  </td>
                  <td className="px-4 py-3">{l.parcela ?? '—'}</td>
                  <td className="px-4 py-3">{METODOS_CULTIVO[l.metodoCultivo] ?? '—'}</td>
                  <td className="whitespace-nowrap px-4 py-3 text-right">
                    <button onClick={() => setModal({ abierto: true, lote: l })} className="rounded-lg px-2 py-1 text-marca-700 hover:bg-marca-50">
                      Editar
                    </button>
                    <button onClick={() => eliminar(l)} className="rounded-lg px-2 py-1 text-red-600 hover:bg-red-50">
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal abierto={modal.abierto} titulo={modal.lote ? 'Editar lote' : 'Nuevo lote'} onCerrar={cerrar}>
        <LoteForm key={modal.lote?.id ?? 'nuevo'} lote={modal.lote} productos={productos} onGuardado={alGuardar} />
      </Modal>
    </section>
  )
}
