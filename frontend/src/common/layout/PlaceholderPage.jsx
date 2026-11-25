import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import Icon from '../icons/Icon'
import PageBand from './PageBand'
import styles from './PlaceholderPage.module.css'

export default function PlaceholderPage({ titleKey, bodyKey = 'common:placeholder.body', icon = 'construction' }) {
  const { t } = useTranslation()
  const title = t(titleKey)

  return (
    <>
      <title>{t('common:pageTitle', { page: title })}</title>
      <PageBand>
        <div className="wrap">
          <h1 className={`${styles.title} rise`} tabIndex={-1}>
            {title}
          </h1>
        </div>
      </PageBand>
      <div className={`wrap ${styles.content}`}>
        <div className={`${styles.card} rise`} style={{ '--delay': 1 }}>
          <span className={styles.icon}>
            <Icon name={icon} />
          </span>
          <p className={styles.body}>{t(bodyKey)}</p>
          <Link className={styles.back} to="/">
            {t('common:placeholder.back')}
          </Link>
        </div>
      </div>
    </>
  )
}
