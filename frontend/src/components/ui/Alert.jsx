const TIPOS = {
  error: 'bg-red-50 text-red-700 ring-red-200',
  exito: 'bg-marca-50 text-marca-800 ring-marca-100',
  info: 'bg-blue-50 text-blue-700 ring-blue-200',
}

export default function Alert({ tipo = 'error', children }) {
  if (!children) return null
  return (
    <div role="alert" className={`rounded-lg px-4 py-3 text-sm ring-1 ${TIPOS[tipo]}`}>
      {children}
    </div>
  )
}
