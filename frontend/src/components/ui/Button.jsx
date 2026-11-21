import Spinner from './Spinner'

const VARIANTES = {
  primario: 'bg-marca-600 text-white hover:bg-marca-700 disabled:bg-marca-500/60',
  secundario: 'border border-gray-300 bg-white text-gray-700 hover:bg-gray-50',
  peligro: 'bg-red-600 text-white hover:bg-red-700',
}

export default function Button({ variante = 'primario', cargando = false, className = '', children, ...props }) {
  return (
    <button
      className={`inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition-colors disabled:cursor-not-allowed ${VARIANTES[variante]} ${className}`}
      disabled={cargando || props.disabled}
      {...props}
    >
      {cargando && <Spinner chico />}
      {children}
    </button>
  )
}
