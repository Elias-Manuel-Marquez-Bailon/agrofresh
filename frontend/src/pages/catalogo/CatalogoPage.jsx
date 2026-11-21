import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { buscarProductos, listarCategorias } from '../../api/catalogoService'
import { leerError } from '../../utils/errores'
import { formatoCantidad, formatoPrecio } from '../../utils/formato'
import ProductoImagen from '../../components/ProductoImagen'
import Alert from '../../components/ui/Alert'
import Spinner from '../../components/ui/Spinner'

const TAMANIO = 12

/** Catálogo público: búsqueda, filtro por categoría y paginación (guardados en la URL). */
export default function CatalogoPage() {
  const [params, setParams] = useSearchParams()
  const nombre = params.get('nombre') ?? ''
  const categoriaId = params.get('categoria') ?? ''
  const pagina = Number(params.get('pagina') ?? 0)

  const [texto, setTexto] = useState(nombre)
  const [categorias, setCategorias] = useState([])
  const [resultado, setResultado] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  const actualizarFiltros = (cambios) => {
    const nuevos = new URLSearchParams(params)
    Object.entries(cambios).forEach(([k, v]) => (v === '' || v == null ? nuevos.delete(k) : nuevos.set(k, v)))
    setParams(nuevos, { replace: true })
  }

  useEffect(() => {
    listarCategorias().then(setCategorias).catch(() => setCategorias([]))
  }, [])

  // Espera 400 ms después de escribir para no consultar en cada tecla
  useEffect(() => {
    if (texto === nombre) return
    const t = setTimeout(() => actualizarFiltros({ nombre: texto.trim(), pagina: '' }), 400)
    return () => clearTimeout(t)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [texto])

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError('')
    buscarProductos({ nombre, categoriaId, pagina, tamanio: TAMANIO })
      .then((r) => vigente && setResultado(r))
      .catch((e) => vigente && setError(leerError(e).mensaje))
      .finally(() => vigente && setCargando(false))
    return () => {
      vigente = false
    }
  }, [nombre, categoriaId, pagina])

  return (
    <section>
      <h1 className="text-2xl font-bold">Catálogo</h1>
      <p className="mt-1 text-gray-500">Productos frescos directo de los productores</p>

      <div className="mt-6 flex flex-col gap-3 sm:flex-row">
        <input
          type="search"
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          placeholder="Buscar producto…"
          aria-label="Buscar producto"
          className="w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm outline-none focus:border-marca-600 focus:ring-2 focus:ring-marca-100 sm:flex-1"
        />
        <select
          value={categoriaId}
          onChange={(e) => actualizarFiltros({ categoria: e.target.value, pagina: '' })}
          aria-label="Filtrar por categoría"
          className="rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm outline-none focus:border-marca-600 focus:ring-2 focus:ring-marca-100 sm:w-60"
        >
          <option value="">Todas las categorías</option>
          {categorias.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nombre}
            </option>
          ))}
        </select>
      </div>

      <div className="mt-6">
        <Alert>{error}</Alert>
        {cargando && !resultado ? (
          <div className="py-20 text-center">
            <Spinner />
          </div>
        ) : resultado?.contenido.length === 0 ? (
          <div className="rounded-xl bg-white py-16 text-center ring-1 ring-gray-200">
            <p className="text-4xl">🧺</p>
            <p className="mt-2 font-medium">No encontramos productos</p>
            <p className="text-sm text-gray-500">Prueba con otra búsqueda o categoría</p>
          </div>
        ) : (
          <div className={`grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 ${cargando ? 'opacity-60' : ''}`}>
            {resultado?.contenido.map((p) => (
              <Link
                key={p.id}
                to={`/catalogo/${p.id}`}
                className="group overflow-hidden rounded-xl bg-white ring-1 ring-gray-200 transition hover:shadow-md hover:ring-marca-500"
              >
                <ProductoImagen producto={p} className="h-44 w-full" />
                <div className="p-4">
                  <p className="text-xs font-medium text-marca-700">{p.categoriaNombre}</p>
                  <p className="mt-0.5 font-semibold group-hover:text-marca-700">{p.nombre}</p>
                  <p className="text-xs text-gray-500">{p.nombreFinca}</p>
                  <div className="mt-3 flex items-end justify-between">
                    <p className="text-lg font-bold">
                      {formatoPrecio(p.precio)}
                      <span className="text-xs font-normal text-gray-500"> / {p.unidad}</span>
                    </p>
                    {Number(p.stock) > 0 ? (
                      <span className="text-xs text-gray-500">{formatoCantidad(p.stock)} disp.</span>
                    ) : (
                      <span className="rounded-full bg-red-50 px-2 py-0.5 text-xs text-red-600">Agotado</span>
                    )}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>

      {resultado && resultado.totalPaginas > 1 && (
        <nav className="mt-8 flex items-center justify-center gap-3" aria-label="Paginación">
          <button
            disabled={pagina === 0}
            onClick={() => actualizarFiltros({ pagina: pagina - 1 || '' })}
            className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm hover:bg-gray-50 disabled:opacity-40"
          >
            Anterior
          </button>
          <span className="text-sm text-gray-600">
            Página {pagina + 1} de {resultado.totalPaginas}
          </span>
          <button
            disabled={pagina + 1 >= resultado.totalPaginas}
            onClick={() => actualizarFiltros({ pagina: pagina + 1 })}
            className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm hover:bg-gray-50 disabled:opacity-40"
          >
            Siguiente
          </button>
        </nav>
      )}
    </section>
  )
}
