import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center px-4 text-center">
      <p className="text-5xl font-bold text-marca-700">404</p>
      <h1 className="mt-3 text-xl font-semibold">Página no encontrada</h1>
      <Link to="/" className="mt-6 font-semibold text-marca-700 hover:underline">
        Ir al inicio
      </Link>
    </div>
  )
}
