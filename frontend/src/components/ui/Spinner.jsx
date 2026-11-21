export default function Spinner({ chico = false, pantallaCompleta = false }) {
  const circulo = (
    <span
      className={`inline-block animate-spin rounded-full border-2 border-current border-t-transparent ${
        chico ? 'h-4 w-4' : 'h-8 w-8 text-marca-600'
      }`}
      aria-label="Cargando"
    />
  )
  if (!pantallaCompleta) return circulo
  return <div className="flex min-h-screen items-center justify-center">{circulo}</div>
}
