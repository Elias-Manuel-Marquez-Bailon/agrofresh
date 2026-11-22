import Bienvenida from '../../components/Bienvenida'

export default function CompradorHome() {
  return (
    <Bienvenida
      subtitulo="Productos frescos directo del productor"
      proximamente={[
        { titulo: 'Catálogo', desc: 'Busca productos por categoría', to: '/catalogo' },
        { titulo: 'Carrito', desc: 'Arma tu pedido con varios productos', to: '/carrito' },
        { titulo: 'Mis pedidos', desc: 'Sigue el estado de tus compras', to: '/comprador/pedidos' },
        { titulo: 'Mis compras', desc: 'Cuánto gastas, qué compras y a quién', to: '/comprador/dashboard' },
      ]}
    />
  )
}
