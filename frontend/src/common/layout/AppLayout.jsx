import { useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, Outlet, useLocation } from 'react-router-dom'
import Bell from '../bell/Bell'
import Icon from '../icons/Icon'
import BottomMenu from './BottomMenu'
import LanguageSwitch from './LanguageSwitch'
import ThemeSwitch from './ThemeSwitch'
import TopMenu from './TopMenu'
import styles from './AppLayout.module.css'

export default function AppLayout() {
  const { t } = useTranslation('common')
  const { pathname } = useLocation()
  const mainRef = useRef(null)
  const shownPath = useRef(pathname)

  useEffect(() => {
    if (shownPath.current === pathname) return
    shownPath.current = pathname
    window.scrollTo(0, 0)
    const target = mainRef.current?.querySelector('h1[tabindex]') ?? mainRef.current
    target?.focus({ preventScroll: true })
  }, [pathname])

  return (
    <div className={styles.app}>
      <a className={styles.skipLink} href="#main">
        {t('skipToContent')}
      </a>
      <header className={styles.header}>
        <div className={`wrap ${styles.bar}`}>
          <Link to="/" className={styles.brand}>
            <span className={styles.mark}>
              <Icon name="location_on" filled size={20} />
            </span>
            <span className={styles.brandName}>{t('appName')}</span>
          </Link>
          <TopMenu />
          <div className={styles.actions}>
            <LanguageSwitch />
            <ThemeSwitch />
            <Bell />
          </div>
        </div>
      </header>
      <main ref={mainRef} id="main" tabIndex={-1} className={styles.main}>
        <Outlet />
      </main>
      <BottomMenu />
    </div>
  )
}
