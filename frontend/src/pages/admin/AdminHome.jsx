import Bienvenida from '../../components/Bienvenida'

export default function AdminHome() {
  return (
    <Bienvenida
      subtitulo="Panel de administración"
      proximamente={[
        { titulo: 'Usuarios', desc: 'Aprobar, bloquear y consultar usuarios' },
        { titulo: 'Categorías', desc: 'Gestionar las categorías de productos' },
        { titulo: 'Dashboard', desc: 'Ventas, pedidos y exportación a CSV' },
      ]}
    />
  )
}
