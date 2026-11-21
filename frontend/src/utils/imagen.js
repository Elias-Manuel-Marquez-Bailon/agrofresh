const TIPOS_PERMITIDOS = ['image/jpeg', 'image/png', 'image/webp']

/**
 * Reduce una imagen en el navegador antes de subirla:
 * máximo `ladoMax` px por lado, convertida a JPEG con calidad 0.8.
 * Una foto de celular de 4 MB queda en unos 100-200 KB.
 * Devuelve un Blob listo para enviar.
 */
export async function reducirImagen(archivo, ladoMax = 800, calidad = 0.8) {
  if (!TIPOS_PERMITIDOS.includes(archivo.type)) {
    throw new Error('Formato no permitido: usa JPG, PNG o WEBP')
  }

  const bitmap = await createImageBitmap(archivo)
  const escala = Math.min(1, ladoMax / Math.max(bitmap.width, bitmap.height))
  const ancho = Math.round(bitmap.width * escala)
  const alto = Math.round(bitmap.height * escala)

  const canvas = document.createElement('canvas')
  canvas.width = ancho
  canvas.height = alto
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#ffffff' // fondo blanco para PNG con transparencia (JPEG no la soporta)
  ctx.fillRect(0, 0, ancho, alto)
  ctx.drawImage(bitmap, 0, 0, ancho, alto)
  bitmap.close?.()

  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => (blob ? resolve(blob) : reject(new Error('No se pudo procesar la imagen'))),
      'image/jpeg',
      calidad,
    )
  })
}
