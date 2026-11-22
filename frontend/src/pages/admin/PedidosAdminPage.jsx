import { todosLosPedidos } from '../../api/pedidoService'
import ListaPedidos from '../../components/ListaPedidos'

export default function PedidosAdminPage() {
  return (
    <ListaPedidos
      titulo="Todos los pedidos"
      subtitulo="Como administrador puedes cancelar pedidos pendientes o aceptados (si estaba aceptado, se repone el stock)."
      vista="admin"
      cargar={todosLosPedidos}
    />
  )
}
