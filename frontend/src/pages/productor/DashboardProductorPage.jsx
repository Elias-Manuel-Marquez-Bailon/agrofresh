import { BarrasHorizontales, ExportarCsv, GraficaMeses, Panel, PedidosPorEstado, Tarjeta } from '../../components/Dashboard'
import Alert from '../../components/ui/Alert'
import Spinner from '../../components/ui/Spinner'
import useDashboard from '../../hooks/useDashboard'
import { formatoCantidad, formatoPrecio } from '../../utils/formato'

export default function DashboardProductorPage() {
  const { datos, error } = useDashboard('productor')

  return (
    <section>
      <h1 className="text-2xl font-bold">Mi dashboard</h1>
      <p className="mt-1 text-gray-500">Las ventas incluyen pedidos aceptados, en camino y entregados.</p>
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
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Tarjeta titulo="Ventas" valor={formatoPrecio(datos.tarjetas.monto)} detalle={`${datos.tarjetas.pedidosVenta} pedidos vendidos`} />
            <Tarjeta titulo="Pedidos recibidos" valor={datos.tarjetas.pedidosTotales} detalle="Incluye pendientes y cancelados" />
            <Tarjeta titulo="Ticket promedio" valor={formatoPrecio(datos.tarjetas.ticketPromedio)} />
            <Tarjeta
              titulo="Calificación"
              valor={datos.tarjetas.calificacionPromedio != null ? `⭐ ${datos.tarjetas.calificacionPromedio}` : '—'}
              detalle={`${datos.tarjetas.totalCalificaciones} reseña(s)`}
            />
          </div>

          <div className="grid gap-6 lg:grid-cols-3">
            <Panel titulo="Ventas por mes" subtitulo="Últimos 6 meses" className="lg:col-span-2">
              <GraficaMeses datos={datos.porMes} />
            </Panel>
            <Panel titulo="Pedidos por estado">
              <PedidosPorEstado datos={datos.porEstado} />
            </Panel>
          </div>

          <Panel titulo="Productos más vendidos" subtitulo="Por monto vendido">
            <BarrasHorizontales
              vacio="Aún no has vendido productos"
              datos={datos.topProductos.map((p) => ({
                nombre: p.nombre,
                valor: p.monto,
                extra: [['Cantidad', `${formatoCantidad(p.cantidad)} ${p.unidad}`]],
              }))}
            />
          </Panel>

          <ExportarCsv rol="productor" />
        </div>
      )}
    </section>
  )
}
