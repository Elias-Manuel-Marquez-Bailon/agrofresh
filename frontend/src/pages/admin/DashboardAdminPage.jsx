import { Link } from 'react-router-dom'
import { BarrasHorizontales, ExportarCsv, GraficaMeses, Panel, PedidosPorEstado, Tarjeta } from '../../components/Dashboard'
import Alert from '../../components/ui/Alert'
import Spinner from '../../components/ui/Spinner'
import useDashboard from '../../hooks/useDashboard'
import { formatoCantidad, formatoPrecio } from '../../utils/formato'

const ROLES = { COMPRADOR: 'Compradores', PRODUCTOR: 'Productores', ADMIN: 'Administradores' }

export default function DashboardAdminPage() {
  const { datos, error } = useDashboard('admin')
  const usuarios = (rol) => datos?.usuariosPorRol.find((r) => r.rol === rol)?.cantidad ?? 0

  return (
    <section>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">Dashboard de la plataforma</h1>
          <p className="mt-1 text-gray-500">Las ventas incluyen pedidos aceptados, en camino y entregados.</p>
        </div>
        <Link to="/admin/pedidos" className="text-sm font-medium text-marca-700 hover:underline">
          Ver todos los pedidos →
        </Link>
      </div>
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
            <Tarjeta titulo="Ventas totales" valor={formatoPrecio(datos.tarjetas.monto)} detalle={`${datos.tarjetas.pedidosVenta} pedidos vendidos`} />
            <Tarjeta titulo="Pedidos" valor={datos.tarjetas.pedidosTotales} detalle={`Ticket promedio ${formatoPrecio(datos.tarjetas.ticketPromedio)}`} />
            <Tarjeta titulo={ROLES.PRODUCTOR} valor={usuarios('PRODUCTOR')} />
            <Tarjeta titulo={ROLES.COMPRADOR} valor={usuarios('COMPRADOR')} />
          </div>

          <div className="grid gap-6 lg:grid-cols-3">
            <Panel titulo="Ventas por mes" subtitulo="Últimos 6 meses" className="lg:col-span-2">
              <GraficaMeses datos={datos.porMes} />
            </Panel>
            <Panel titulo="Pedidos por estado">
              <PedidosPorEstado datos={datos.porEstado} />
            </Panel>
          </div>

          <div className="grid gap-6 lg:grid-cols-2">
            <Panel titulo="Ventas por categoría">
              <BarrasHorizontales
                vacio="Aún no hay ventas"
                datos={datos.porCategoria.map((c) => ({ nombre: c.nombre, valor: c.monto }))}
              />
            </Panel>
            <Panel titulo="Productores con más ventas">
              <BarrasHorizontales
                vacio="Aún no hay ventas"
                datos={datos.topProductores.map((p) => ({ nombre: p.nombre, valor: p.monto, extra: [['Pedidos', p.pedidos]] }))}
              />
            </Panel>
          </div>

          <Panel titulo="Productos más vendidos" subtitulo="Por monto vendido">
            <BarrasHorizontales
              vacio="Aún no hay ventas"
              datos={datos.topProductos.map((p) => ({
                nombre: p.nombre,
                valor: p.monto,
                extra: [['Cantidad', `${formatoCantidad(p.cantidad)} ${p.unidad}`]],
              }))}
            />
          </Panel>

          <ExportarCsv rol="admin" />
        </div>
      )}
    </section>
  )
}
