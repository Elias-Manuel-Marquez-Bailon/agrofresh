const MONEDA = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })
const NUMERO = new Intl.NumberFormat('es-MX', { maximumFractionDigits: 2 })

/** 28.5 -> "$28.50" */
export const formatoPrecio = (valor) => MONEDA.format(Number(valor ?? 0))

const FECHA = new Intl.DateTimeFormat('es-MX', { dateStyle: 'medium', timeStyle: 'short' })

/** "2026-11-22T10:30:00" -> "22 nov 2026, 10:30" */
export const formatoFecha = (valor) => (valor ? FECHA.format(new Date(valor)) : '')

const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

/**
 * "2026-10-01" -> "1 oct 2026". Se arma a mano porque new Date("2026-10-01") se interpreta en UTC
 * y en México mostraría el día anterior.
 */
export const formatoDia = (valor) => {
  if (!valor) return ''
  const [a, m, d] = valor.split('-').map(Number)
  return `${d} ${MESES[m - 1]} ${a}`
}

/** Fecha de hoy en formato "AAAA-MM-DD" (hora local), para el max de los input date. */
export const hoyISO = () => {
  const h = new Date()
  return `${h.getFullYear()}-${String(h.getMonth() + 1).padStart(2, '0')}-${String(h.getDate()).padStart(2, '0')}`
}

export const METODOS_CULTIVO = {
  ORGANICO: 'Orgánico',
  CONVENCIONAL: 'Convencional',
  HIDROPONICO: 'Hidropónico',
  OTRO: 'Otro',
}

/** 120.00 -> "120" · 2.5 -> "2.5" */
export const formatoCantidad = (valor) => NUMERO.format(Number(valor ?? 0))
