import { useTranslation } from 'react-i18next'
import { cx } from '../cx'
import styles from './NewCount.module.css'

export function NewBadge({ count, className }) {
  if (!count) return null
  return (
    <span key={count} className={cx(styles.badge, className)} aria-hidden="true">
      {count}
    </span>
  )
}

export function NewCountText({ count }) {
  const { t } = useTranslation('common')
  if (!count) return null
  return <span className="sr-only">{t('newCountSuffix', { count })}</span>
}
