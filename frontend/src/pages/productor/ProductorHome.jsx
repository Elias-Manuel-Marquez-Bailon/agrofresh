import Bienvenida from '../../components/Bienvenida'

export default function ProductorHome() {
  return (
    <Bienvenida
      subtitulo="Gestiona tus productos y pedidos"
      proximamente={[
        { titulo: 'Mis productos', desc: 'Publica tu cosecha con precio y stock', to: '/productor/productos' },
        { titulo: 'Pedidos recibidos', desc: 'Acepta y envía los pedidos de tus compradores' },
        { titulo: 'Lotes', desc: 'Registra siembra y cosecha para la trazabilidad' },
      ]}
    />
  )
}
