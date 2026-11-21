// Ícono por categoría cuando el producto no tiene imagen.
const ICONOS = {
  frutas: '🍎',
  verduras: '🥕',
  'hortalizas de hoja': '🥬',
  'granos y cereales': '🌾',
  legumbres: '🫘',
  'tubérculos': '🥔',
  'hierbas aromáticas': '🌿',
  'lácteos': '🧀',
  huevos: '🥚',
  'miel y derivados': '🍯',
}

export function iconoCategoria(nombre) {
  return ICONOS[nombre?.toLowerCase()] ?? '🧺'
}
