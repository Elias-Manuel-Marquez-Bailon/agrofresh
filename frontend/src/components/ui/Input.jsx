import { useState } from 'react'

/**
 * Input con etiqueta y mensaje de error debajo.
 * Si type="password", muestra un botón con un ojo para ver u ocultar el texto.
 */
export default function Input({ label, error, id, type = 'text', className = '', ...props }) {
  const inputId = id || props.name
  const esPassword = type === 'password'
  const [visible, setVisible] = useState(false)

  return (
    <div className={className}>
      {label && (
        <label htmlFor={inputId} className="mb-1 block text-sm font-medium text-gray-700">
          {label}
        </label>
      )}
      <div className="relative">
        <input
          id={inputId}
          type={esPassword && visible ? 'text' : type}
          className={`block w-full rounded-lg border px-3 py-2.5 text-sm outline-none transition focus:ring-2 ${
            esPassword ? 'pr-10' : ''
          } ${
            error
              ? 'border-red-400 focus:border-red-500 focus:ring-red-100'
              : 'border-gray-300 focus:border-marca-600 focus:ring-marca-100'
          }`}
          aria-invalid={!!error}
          {...props}
        />
        {esPassword && (
          <button
            type="button"
            onClick={() => setVisible(!visible)}
            className="absolute inset-y-0 right-0 flex items-center px-3 text-gray-400 hover:text-gray-600"
            aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
            title={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
          >
            <svg className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="1.8" viewBox="0 0 24 24" strokeLinecap="round" strokeLinejoin="round">
              {visible ? (
                // Ojo tachado
                <>
                  <path d="M3 3l18 18" />
                  <path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" />
                  <path d="M9.9 5.1A9.8 9.8 0 0 1 12 5c5 0 8.5 4.5 9.5 7a13 13 0 0 1-2.4 3.6M6.6 6.6A13.3 13.3 0 0 0 2.5 12c1 2.5 4.5 7 9.5 7a9.6 9.6 0 0 0 5.4-1.6" />
                </>
              ) : (
                // Ojo abierto
                <>
                  <path d="M2.5 12c1-2.5 4.5-7 9.5-7s8.5 4.5 9.5 7c-1 2.5-4.5 7-9.5 7s-8.5-4.5-9.5-7z" />
                  <circle cx="12" cy="12" r="3" />
                </>
              )}
            </svg>
          </button>
        )}
      </div>
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  )
}
