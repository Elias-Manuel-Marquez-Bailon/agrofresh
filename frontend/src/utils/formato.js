const MONEDA = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })
const NUMERO = new Intl.NumberFormat('es-MX', { maximumFractionDigits: 2 })

/** 28.5 -> "$28.50" */
export const formatoPrecio = (valor) => MONEDA.format(Number(valor ?? 0))

/** 120.00 -> "120" · 2.5 -> "2.5" */
export const formatoCantidad = (valor) => NUMERO.format(Number(valor ?? 0))
