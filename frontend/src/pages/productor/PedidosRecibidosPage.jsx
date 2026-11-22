import { pedidosRecibidos } from '../../api/pedidoService'
import ListaPedidos from '../../components/ListaPedidos'

export default function PedidosRecibidosPage() {
  return (
    <ListaPedidos
      titulo="Pedidos recibidos"
      subtitulo="Al aceptar un pedido se descuenta el stock; si lo cancelas después, se repone."
      vista="productor"
      cargar={pedidosRecibidos}
    />
  )
}
