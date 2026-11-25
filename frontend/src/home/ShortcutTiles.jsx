import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { cx } from '../common/cx'
import Icon from '../common/icons/Icon'
import { MODULE_ORDER, MODULES } from '../common/modules'
import { useNotifications } from '../common/notifications/notificationsContext'
import { NewBadge, NewCountText } from '../common/ui/NewCount'
import styles from './ShortcutTiles.module.css'

export default function ShortcutTiles() {
  const { t } = useTranslation('home')
  const { unreadByModule } = useNotifications()

  return (
    <nav className={`${styles.tiles} rise`} style={{ '--delay': 2 }} aria-label={t('shortcuts')}>
      {MODULE_ORDER.map((module) => (
        <Link key={module} to={MODULES[module].path} className={cx(styles.tile, `module-${module}`)}>
          <span className={styles.icon}>
            <Icon name={MODULES[module].icon} />
          </span>
          <span>{t(`common:modules.${module}`)}</span>
          <NewBadge count={unreadByModule[module]} className={styles.count} />
          <NewCountText count={unreadByModule[module]} />
        </Link>
      ))}
    </nav>
  )
}
