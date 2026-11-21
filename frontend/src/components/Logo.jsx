export default function Logo({ grande = false }) {
  return (
    <span className={`inline-flex items-center gap-2 font-bold text-marca-700 ${grande ? 'text-3xl' : 'text-xl'}`}>
      <img src="/favicon.svg" alt="" className={grande ? 'h-10 w-10' : 'h-7 w-7'} />
      AgroFresh
    </span>
  )
}
