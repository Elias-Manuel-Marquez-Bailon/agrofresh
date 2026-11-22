import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { useAuth } from './AuthContext'

const CarritoContext = createContext(null)

// Cada comprador tiene su propio carrito en localStorage (si cambia de cuenta, no se mezclan).
const claveDe = (usuario) => (usuario?.rol === 'COMPRADOR' ? `agrofresh_carrito_${usuario.id}` : null)

function leer(clave) {
  if (!clave) return []
  try {
    const items = JSON.parse(localStorage.getItem(clave))
    return Array.isArray(items) ? items : []
  } catch {
    return []
  }
}

const redondear = (n) => Math.round(n * 100) / 100

/** Carrito de un usuario. Se monta de nuevo (con key) cuando cambia el usuario. */
function CarritoDeUsuario({ clave, children }) {
  // item: { productoId, nombre, precio, unidad, stock, productorId, nombreFinca, cantidad }
  const [items, setItems] = useState(() => leer(clave))

  useEffect(() => {
    if (clave) localStorage.setItem(clave, JSON.stringify(items))
  }, [clave, items])

  /** Agrega o suma cantidad; nunca pasa del stock conocido. */
  const agregar = useCallback((producto, cantidad) => {
    setItems((actuales) => {
      const existente = actuales.find((i) => i.productoId === producto.id)
      const stock = Number(producto.stock)
      const nueva = redondear(Math.min((existente?.cantidad ?? 0) + cantidad, stock))
      const datos = {
        productoId: producto.id,
        nombre: producto.nombre,
        precio: Number(producto.precio),
        unidad: producto.unidad,
        stock,
        productorId: producto.productorId,
        nombreFinca: producto.nombreFinca,
        cantidad: nueva,
      }
      return existente
        ? actuales.map((i) => (i.productoId === producto.id ? datos : i))
        : [...actuales, datos]
    })
  }, [])

  const cambiarCantidad = useCallback((productoId, cantidad) => {
    setItems((actuales) =>
      actuales.map((i) => (i.productoId === productoId ? { ...i, cantidad: redondear(cantidad) } : i)),
    )
  }, [])

  const quitar = useCallback((productoId) => {
    setItems((actuales) => actuales.filter((i) => i.productoId !== productoId))
  }, [])

  const vaciar = useCallback(() => setItems([]), [])

  const value = useMemo(() => {
    const total = redondear(items.reduce((s, i) => s + i.precio * i.cantidad, 0))
    return { items, cantidadItems: items.length, total, agregar, cambiarCantidad, quitar, vaciar }
  }, [items, agregar, cambiarCantidad, quitar, vaciar])

  return <CarritoContext.Provider value={value}>{children}</CarritoContext.Provider>
}

export function CarritoProvider({ children }) {
  const { usuario } = useAuth()
  const clave = claveDe(usuario)
  return (
    <CarritoDeUsuario key={clave ?? 'sin-carrito'} clave={clave}>
      {children}
    </CarritoDeUsuario>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useCarrito() {
  const ctx = useContext(CarritoContext)
  if (!ctx) throw new Error('useCarrito debe usarse dentro de <CarritoProvider>')
  return ctx
}
