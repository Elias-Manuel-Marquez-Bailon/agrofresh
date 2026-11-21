import { useState } from 'react'
import { iconoCategoria } from '../utils/categorias'

/** Muestra la imagen del producto o, si no tiene (o falla), el ícono de su categoría. */
export default function ProductoImagen({ producto, className = '' }) {
  const [fallo, setFallo] = useState(false)
  // El backend ya devuelve la ruta completa: /api/productos/{id}/imagen?v=...
  const url = producto.imagenUrl

  if (!url || fallo) {
    return (
      <div className={`flex items-center justify-center bg-marca-50 ${className}`}>
        <span className="text-5xl" role="img" aria-label={producto.categoriaNombre}>
          {iconoCategoria(producto.categoriaNombre)}
        </span>
      </div>
    )
  }
  return (
    <img
      src={url}
      alt={producto.nombre}
      loading="lazy"
      onError={() => setFallo(true)}
      className={`object-cover ${className}`}
    />
  )
}
