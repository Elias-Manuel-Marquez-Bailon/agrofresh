import { useLocation } from 'react-router-dom'
import { misPedidos } from '../../api/pedidoService'
import ListaPedidos from '../../components/ListaPedidos'

export default function MisPedidosPage() {
  // El carrito manda aquí un mensaje al confirmar la compra.
  const { state } = useLocation()
  return (
    <ListaPedidos
      titulo="Mis pedidos"
      subtitulo="Sigue el estado de tus compras. Puedes cancelar mientras estén pendientes."
      vista="comprador"
      cargar={misPedidos}
      mensajeInicial={state?.mensaje ?? ''}
    />
  )
}
