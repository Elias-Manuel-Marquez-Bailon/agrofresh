import { Outlet } from 'react-router-dom'
import Logo from '../components/Logo'

/** Layout centrado para login y registro. */
export default function AuthLayout() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-marca-50 to-white px-4 py-10">
      <div className="mb-6">
        <Logo grande />
      </div>
      <div className="w-full max-w-md rounded-2xl bg-white p-6 sm:p-8 shadow-sm ring-1 ring-gray-200">
        <Outlet />
      </div>
      <p className="mt-6 text-xs text-gray-500">Del campo a tu mesa, con trazabilidad.</p>
    </div>
  )
}
