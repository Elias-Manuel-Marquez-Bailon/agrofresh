import { Outlet } from 'react-router-dom'
import Navbar from '../components/Navbar'

/** Layout de las páginas privadas: navbar arriba y el contenido de cada página. */
export default function MainLayout() {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-1 mx-auto w-full max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
