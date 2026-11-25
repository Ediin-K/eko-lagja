import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import Icon from '../common/icons/Icon'
import styles from './HomeHero.module.css'

function greetingKey(hour) {
  if (hour < 12) return 'greeting.morning'
  if (hour < 18) return 'greeting.afternoon'
  return 'greeting.evening'
}

export default function HomeHero({ resident }) {
  const { t } = useTranslation('home')
  const [hour] = useState(() => new Date().getHours())
  const greeting = t(greetingKey(hour))

  return (
    <>
      <span className={styles.pin} aria-hidden="true" />
      <div className={`wrap ${styles.hero}`}>
        <div className={`${styles.hello} rise`}>
          <h1 className={styles.greeting} tabIndex={-1}>
            {resident ? t('greeting.withName', { greeting, name: resident.firstName }) : greeting}
          </h1>
          {resident && (
            <span className={styles.neighbourhood}>
              <Icon name="location_on" filled size={18} />
              {t('neighbourhood', { name: resident.neighbourhood })}
            </span>
          )}
        </div>
        <Link to="/complaints/new" className={`${styles.report} rise`} style={{ '--delay': 1 }}>
          <span className={styles.plus}>
            <Icon name="add" />
          </span>
          <span className={styles.reportText}>
            <strong>{t('report.title')}</strong>
            <small>{t('report.examples')}</small>
          </span>
          <Icon name="chevron_right" className={styles.go} />
        </Link>
      </div>
    </>
  )
}
