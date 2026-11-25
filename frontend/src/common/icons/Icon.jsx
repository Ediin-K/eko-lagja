import { ICONS } from './icons'

export default function Icon({ name, filled = false, size = 24, className }) {
  const path = (filled && ICONS[`${name}_fill1`]) || ICONS[name]
  return (
    <svg
      className={className}
      width={size}
      height={size}
      viewBox="0 -960 960 960"
      fill="currentColor"
      aria-hidden="true"
      focusable="false"
    >
      <path d={path} />
    </svg>
  )
}
