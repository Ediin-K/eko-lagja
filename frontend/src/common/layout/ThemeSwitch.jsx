import { useRef, useState } from 'react'
import { flushSync } from 'react-dom'
import { useTranslation } from 'react-i18next'
import Icon from '../icons/Icon'
import { applyTheme, currentTheme } from '../theme'
import styles from './ThemeSwitch.module.css'

const REVEAL_MS = 360

export default function ThemeSwitch() {
  const { t } = useTranslation('common')
  const [theme, setTheme] = useState(currentTheme)
  const buttonRef = useRef(null)

  function toggle() {
    const next = theme === 'dark' ? 'light' : 'dark'
    const switchTheme = () => {
      applyTheme(next)
      flushSync(() => setTheme(next))
    }
    const lessMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (typeof document.startViewTransition !== 'function' || lessMotion) {
      switchTheme()
      return
    }
    const rect = buttonRef.current.getBoundingClientRect()
    const x = rect.left + rect.width / 2
    const y = rect.top + rect.height / 2
    const radius = Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y))
    const transition = document.startViewTransition(switchTheme)
    transition.ready
      .then(() => {
        document.documentElement.animate(
          { clipPath: [`circle(0px at ${x}px ${y}px)`, `circle(${radius}px at ${x}px ${y}px)`] },
          { duration: REVEAL_MS, easing: 'cubic-bezier(0.23, 1, 0.32, 1)', pseudoElement: '::view-transition-new(root)' },
        )
      })
      .catch(() => {})
  }

  return (
    <button
      ref={buttonRef}
      type="button"
      className={styles.button}
      aria-label={t('theme.dark')}
      aria-pressed={theme === 'dark'}
      onClick={toggle}
    >
      <span key={theme} className={styles.icon}>
        <Icon name={theme === 'dark' ? 'light_mode' : 'dark_mode'} />
      </span>
    </button>
  )
}
