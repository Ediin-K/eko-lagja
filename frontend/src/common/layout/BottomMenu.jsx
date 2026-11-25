import { useTranslation } from 'react-i18next'
import { NavLink, useLocation } from 'react-router-dom'
import { cx } from '../cx'
import Icon from '../icons/Icon'
import { activeMenuIndex, MENU_ITEMS } from '../modules'
import { useNotifications } from '../notifications/notificationsContext'
import { NewBadge, NewCountText } from '../ui/NewCount'
import styles from './Menu.module.css'

export default function BottomMenu() {
  const { t } = useTranslation('common')
  const { unreadByModule } = useNotifications()
  const { pathname } = useLocation()
  const activeIndex = activeMenuIndex(pathname)

  return (
    <nav className={styles.bottomMenu} aria-label={t('menu.label')}>
      <span
        className={cx(styles.tabIndicator, activeIndex < 0 && styles.indicatorHidden)}
        style={{ '--index': Math.max(activeIndex, 0) }}
        aria-hidden="true"
      />
      {MENU_ITEMS.map((item) => {
        const count = item.module ? unreadByModule[item.module] : 0
        return (
          <NavLink key={item.key} to={item.path} end={item.path === '/'} className={styles.tab}>
            {({ isActive }) => (
              <>
                <span className={styles.tabIcon}>
                  <Icon name={item.icon} filled={isActive} />
                  <NewBadge count={count} className={styles.tabBadge} />
                </span>
                <span>{t(item.labelKey)}</span>
                <NewCountText count={count} />
              </>
            )}
          </NavLink>
        )
      })}
    </nav>
  )
}
