import Bienvenida from '../../components/Bienvenida'

export default function CompradorHome() {
  return (
    <Bienvenida
      subtitulo="Productos frescos directo del productor"
      proximamente={[
        { titulo: 'Catálogo', desc: 'Busca productos por categoría', to: '/catalogo' },
        { titulo: 'Carrito', desc: 'Arma tu pedido con varios productos' },
        { titulo: 'Mis pedidos', desc: 'Sigue el estado y califica tus compras' },
      ]}
    />
  )
}
