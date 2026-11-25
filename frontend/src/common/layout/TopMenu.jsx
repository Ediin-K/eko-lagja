import { useLayoutEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { NavLink, useLocation } from 'react-router-dom'
import { MENU_ITEMS } from '../modules'
import { useNotifications } from '../notifications/notificationsContext'
import { NewBadge, NewCountText } from '../ui/NewCount'
import styles from './Menu.module.css'

export default function TopMenu() {
  const { t } = useTranslation('common')
  const { unreadByModule } = useNotifications()
  const { pathname } = useLocation()
  const navRef = useRef(null)
  const pillRef = useRef(null)

  useLayoutEffect(() => {
    const nav = navRef.current
    const pill = pillRef.current
    if (!nav || !pill) return undefined
    function place() {
      const active = nav.querySelector('[aria-current="page"]')
      pill.style.setProperty('--x', `${active ? active.offsetLeft : 0}px`)
      pill.style.setProperty('--width', `${active ? active.offsetWidth : 0}px`)
      pill.style.opacity = active && active.offsetWidth > 0 ? '1' : '0'
    }
    place()
    const frame = requestAnimationFrame(() => {
      pill.dataset.ready = 'true'
    })
    const observer = new ResizeObserver(place)
    observer.observe(nav)
    return () => {
      cancelAnimationFrame(frame)
      observer.disconnect()
    }
  }, [pathname])

  return (
    <nav ref={navRef} className={styles.topMenu} aria-label={t('menu.label')}>
      <span ref={pillRef} className={styles.topIndicator} aria-hidden="true" />
      {MENU_ITEMS.map((item) => {
        const count = item.module ? unreadByModule[item.module] : 0
        return (
          <NavLink key={item.key} to={item.path} end={item.path === '/'} className={styles.topLink}>
            <span>{t(item.labelKey)}</span>
            <NewBadge count={count} className={styles.topBadge} />
            <NewCountText count={count} />
          </NavLink>
        )
      })}
    </nav>
  )
}
