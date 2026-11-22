import { useState } from 'react'
import { calificarPedido } from '../api/calificacionService'
import { leerError } from '../utils/errores'
import Estrellas from './Estrellas'
import Alert from './ui/Alert'
import Button from './ui/Button'
import Modal from './ui/Modal'

const TEXTOS = ['', 'Malo', 'Regular', 'Bueno', 'Muy bueno', 'Excelente']

export default function CalificarModal({ pedido, onCerrar, onCalificado }) {
  const [puntuacion, setPuntuacion] = useState(0)
  const [comentario, setComentario] = useState('')
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  if (!pedido) return null

  const enviar = async (e) => {
    e.preventDefault()
    if (!puntuacion) {
      setError('Elige de 1 a 5 estrellas')
      return
    }
    setEnviando(true)
    setError('')
    try {
      const { mensaje } = await calificarPedido(pedido.id, puntuacion, comentario.trim())
      onCalificado(mensaje)
    } catch (err) {
      setError(leerError(err).mensaje)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal abierto titulo={`Calificar pedido #${pedido.id}`} onCerrar={onCerrar}>
      <form onSubmit={enviar} className="space-y-4">
        <p className="text-sm text-gray-600">¿Cómo fue tu experiencia con {pedido.nombreFinca}?</p>
        <div className="flex items-center gap-3">
          <Estrellas valor={puntuacion} onChange={setPuntuacion} tamanio="h-8 w-8" />
          <span className="text-sm font-medium text-gray-600">{TEXTOS[puntuacion]}</span>
        </div>
        <div>
          <label htmlFor="comentario" className="mb-1 block text-sm font-medium text-gray-700">
            Comentario (opcional, será público)
          </label>
          <textarea
            id="comentario"
            rows={3}
            maxLength={500}
            value={comentario}
            onChange={(e) => setComentario(e.target.value)}
            className="block w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm"
          />
          <p className="mt-1 text-right text-xs text-gray-400">{comentario.length}/500</p>
        </div>
        <Alert>{error}</Alert>
        <Button type="submit" className="w-full" cargando={enviando}>
          Enviar calificación
        </Button>
      </form>
    </Modal>
  )
}
