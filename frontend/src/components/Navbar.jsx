import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { rutaInicio } from '../utils/errores'
import Logo from './Logo'

// Menú por rol. Se irá llenando en las siguientes fases.
const CATALOGO = { to: '/catalogo', texto: 'Catálogo' }
const MENU = {
  ADMIN: [{ to: '/admin', texto: 'Inicio', end: true }, { to: '/admin/categorias', texto: 'Categorías' }, CATALOGO],
  PRODUCTOR: [{ to: '/productor', texto: 'Inicio', end: true }, { to: '/productor/productos', texto: 'Mis productos' }, CATALOGO],
  COMPRADOR: [{ to: '/comprador', texto: 'Inicio', end: true }, CATALOGO],
  INVITADO: [CATALOGO],
}

const ETIQUETA_ROL = { ADMIN: 'Administrador', PRODUCTOR: 'Productor', COMPRADOR: 'Comprador' }

export default function Navbar() {
  const { usuario, logout } = useAuth()
  const navigate = useNavigate()
  const [abierto, setAbierto] = useState(false)
  const items = usuario ? (MENU[usuario.rol] ?? []) : MENU.INVITADO

  const salir = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const claseLink = ({ isActive }) =>
    `block rounded-lg px-3 py-2 text-sm font-medium ${
      isActive ? 'bg-marca-50 text-marca-700' : 'text-gray-600 hover:bg-gray-100'
    }`

  return (
    <header className="bg-white border-b border-gray-200">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link to={usuario ? rutaInicio(usuario.rol) : '/catalogo'}>
          <Logo />
        </Link>

        {/* Escritorio */}
        <nav className="hidden md:flex items-center gap-1">
          {items.map((i) => (
            <NavLink key={i.to} to={i.to} end={i.end} className={claseLink}>
              {i.texto}
            </NavLink>
          ))}
        </nav>
        {!usuario ? (
          <div className="flex items-center gap-2">
            <Link to="/login" className="rounded-lg px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-100">
              Iniciar sesión
            </Link>
            <Link to="/registro" className="rounded-lg bg-marca-600 px-3 py-1.5 text-sm font-semibold text-white hover:bg-marca-700">
              Registrarse
            </Link>
          </div>
        ) : (
        <div className="hidden md:flex items-center gap-3">
          <div className="text-right leading-tight">
            <p className="text-sm font-medium">{usuario?.nombre}</p>
            <p className="text-xs text-gray-500">{ETIQUETA_ROL[usuario?.rol]}</p>
          </div>
          <button onClick={salir} className="rounded-lg border border-gray-300 px-3 py-1.5 text-sm hover:bg-gray-100">
            Cerrar sesión
          </button>
        </div>
        )}

        {/* Botón móvil (solo con sesión; el invitado ya ve sus botones) */}
        {usuario && (
        <button
          className="md:hidden rounded-lg p-2 text-gray-600 hover:bg-gray-100"
          onClick={() => setAbierto(!abierto)}
          aria-label="Abrir menú"
        >
          <svg className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" d={abierto ? 'M6 6l12 12M18 6L6 18' : 'M4 7h16M4 12h16M4 17h16'} />
          </svg>
        </button>
        )}
      </div>

      {/* Menú móvil */}
      {abierto && usuario && (
        <div className="md:hidden border-t border-gray-200 px-4 py-3 space-y-1">
          <p className="px-3 pb-2 text-sm">
            <span className="font-medium">{usuario?.nombre}</span>
            <span className="text-gray-500"> · {ETIQUETA_ROL[usuario?.rol]}</span>
          </p>
          {items.map((i) => (
            <NavLink key={i.to} to={i.to} end={i.end} className={claseLink} onClick={() => setAbierto(false)}>
              {i.texto}
            </NavLink>
          ))}
          <button onClick={salir} className="block w-full text-left rounded-lg px-3 py-2 text-sm text-red-600 hover:bg-red-50">
            Cerrar sesión
          </button>
        </div>
      )}
    </header>
  )
}
