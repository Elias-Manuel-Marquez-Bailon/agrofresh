import { useEffect, useState } from 'react'
import { misResenas } from '../../api/calificacionService'
import Bienvenida from '../../components/Bienvenida'
import ListaResenas from '../../components/ListaResenas'

export default function ProductorHome() {
  const [resenas, setResenas] = useState(null)

  useEffect(() => {
    misResenas().then(setResenas).catch(() => setResenas(null))
  }, [])

  return (
    <>
      <Bienvenida
        subtitulo="Gestiona tus productos y pedidos"
        proximamente={[
          { titulo: 'Mis productos', desc: 'Publica tu cosecha con precio y stock', to: '/productor/productos' },
          { titulo: 'Pedidos recibidos', desc: 'Acepta y envía los pedidos de tus compradores', to: '/productor/pedidos' },
          { titulo: 'Lotes', desc: 'Registra siembra y cosecha para la trazabilidad', to: '/productor/lotes' },
        ]}
      />
      <div className="mt-8">
        <ListaResenas datos={resenas} titulo="Mis calificaciones" vacio="Aún no tienes reseñas. Aparecerán cuando tus compradores califiquen pedidos entregados." />
      </div>
    </>
  )
}
