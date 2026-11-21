import { useAuth } from '../context/AuthContext'

/** Encabezado + tarjetas de "próximamente" para las páginas de inicio por rol. */
export default function Bienvenida({ subtitulo, proximamente = [] }) {
  const { usuario } = useAuth()
  return (
    <section>
      <h1 className="text-2xl font-bold">Hola, {usuario?.nombre?.split(' ')[0]} 👋</h1>
      <p className="mt-1 text-gray-500">{subtitulo}</p>

      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {proximamente.map((p) => (
          <div key={p.titulo} className="rounded-xl bg-white p-5 ring-1 ring-gray-200">
            <p className="font-semibold">{p.titulo}</p>
            <p className="mt-1 text-sm text-gray-500">{p.desc}</p>
            <span className="mt-3 inline-block rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-600">Próximamente</span>
          </div>
        ))}
      </div>
    </section>
  )
}
