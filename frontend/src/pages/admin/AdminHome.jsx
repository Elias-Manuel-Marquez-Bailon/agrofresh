import Bienvenida from '../../components/Bienvenida'

export default function AdminHome() {
  return (
    <Bienvenida
      subtitulo="Panel de administración"
      proximamente={[
        { titulo: 'Usuarios', desc: 'Aprobar, bloquear y consultar usuarios' },
        { titulo: 'Categorías', desc: 'Gestionar las categorías de productos', to: '/admin/categorias' },
        { titulo: 'Dashboard', desc: 'Ventas, pedidos y exportación a CSV', to: '/admin/dashboard' },
        { titulo: 'Pedidos', desc: 'Consulta y cancela pedidos de la plataforma', to: '/admin/pedidos' },
      ]}
    />
  )
}
