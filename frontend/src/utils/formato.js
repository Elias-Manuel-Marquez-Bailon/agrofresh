const MONEDA = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })
const NUMERO = new Intl.NumberFormat('es-MX', { maximumFractionDigits: 2 })

/** 28.5 -> "$28.50" */
export const formatoPrecio = (valor) => MONEDA.format(Number(valor ?? 0))

const FECHA = new Intl.DateTimeFormat('es-MX', { dateStyle: 'medium', timeStyle: 'short' })

/** "2026-11-22T10:30:00" -> "22 nov 2026, 10:30" */
export const formatoFecha = (valor) => (valor ? FECHA.format(new Date(valor)) : '')

/** 120.00 -> "120" · 2.5 -> "2.5" */
export const formatoCantidad = (valor) => NUMERO.format(Number(valor ?? 0))
