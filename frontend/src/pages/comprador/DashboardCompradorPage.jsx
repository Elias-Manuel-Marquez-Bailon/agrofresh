import { BarrasHorizontales, ExportarCsv, GraficaMeses, Panel, PedidosPorEstado, Tarjeta } from '../../components/Dashboard'
import Alert from '../../components/ui/Alert'
import Spinner from '../../components/ui/Spinner'
import useDashboard from '../../hooks/useDashboard'
import { formatoCantidad, formatoPrecio } from '../../utils/formato'

export default function DashboardCompradorPage() {
  const { datos, error } = useDashboard('comprador')

  return (
    <section>
      <h1 className="text-2xl font-bold">Mis compras</h1>
      <p className="mt-1 text-gray-500">El gasto incluye pedidos aceptados, en camino y entregados.</p>
      <div className="mt-4">
        <Alert>{error}</Alert>
      </div>

      {!datos ? (
        !error && (
          <div className="py-20 text-center">
            <Spinner />
          </div>
        )
      ) : (
        <div className="mt-6 space-y-6">
          <div className="grid gap-4 sm:grid-cols-3">
            <Tarjeta titulo="Total gastado" valor={formatoPrecio(datos.tarjetas.monto)} detalle={`${datos.tarjetas.pedidosVenta} pedidos`} />
            <Tarjeta titulo="Pedidos realizados" valor={datos.tarjetas.pedidosTotales} detalle="Incluye pendientes y cancelados" />
            <Tarjeta titulo="Gasto promedio por pedido" valor={formatoPrecio(datos.tarjetas.ticketPromedio)} />
          </div>

          <div className="grid gap-6 lg:grid-cols-3">
            <Panel titulo="Gasto por mes" subtitulo="Últimos 6 meses" className="lg:col-span-2">
              <GraficaMeses datos={datos.porMes} etiqueta="Gasto" />
            </Panel>
            <Panel titulo="Mis pedidos por estado">
              <PedidosPorEstado datos={datos.porEstado} />
            </Panel>
          </div>

          <div className="grid gap-6 lg:grid-cols-2">
            <Panel titulo="Productos que más compro">
              <BarrasHorizontales
                etiquetaValor="Gasto"
                vacio="Aún no has comprado productos"
                datos={datos.topProductos.map((p) => ({
                  nombre: p.nombre,
                  valor: p.monto,
                  extra: [['Cantidad', `${formatoCantidad(p.cantidad)} ${p.unidad}`]],
                }))}
              />
            </Panel>
            <Panel titulo="Productores favoritos">
              <BarrasHorizontales
                etiquetaValor="Gasto"
                vacio="Aún no has comprado a ningún productor"
                datos={datos.topProductores.map((p) => ({
                  nombre: p.nombre,
                  valor: p.monto,
                  extra: [['Pedidos', p.pedidos]],
                }))}
              />
            </Panel>
          </div>

          <ExportarCsv rol="comprador" />
        </div>
      )}
    </section>
  )
}
