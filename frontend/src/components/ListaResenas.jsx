import Estrellas from './Estrellas'

/** Promedio grande + lista de reseñas. datos: respuesta de /calificaciones/... */
export default function ListaResenas({ datos, titulo = 'Reseñas del productor', vacio = 'Aún no hay reseñas.' }) {
  if (!datos) return null
  return (
    <div className="rounded-2xl bg-white p-5 ring-1 ring-gray-200">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="font-semibold">{titulo}</h2>
        {datos.total > 0 && (
          <div className="flex items-center gap-2">
            <span className="text-2xl font-bold">{Number(datos.promedio).toFixed(1)}</span>
            <Estrellas valor={datos.promedio} />
            <span className="text-sm text-gray-500">({datos.total})</span>
          </div>
        )}
      </div>
      {datos.resenas.contenido.length === 0 ? (
        <p className="mt-3 text-sm text-gray-500">{vacio}</p>
      ) : (
        <ul className="mt-3 divide-y divide-gray-100">
          {datos.resenas.contenido.map((r) => (
            <li key={r.id} className="py-3">
              <div className="flex items-center gap-2">
                <Estrellas valor={r.puntuacion} tamanio="h-3.5 w-3.5" />
                <span className="text-sm font-medium">{r.compradorNombre}</span>
              </div>
              {r.comentario && <p className="mt-1 text-sm text-gray-700">{r.comentario}</p>}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
