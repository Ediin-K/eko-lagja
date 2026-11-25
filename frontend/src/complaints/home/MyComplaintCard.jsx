import { useTranslation } from 'react-i18next'
import { cx } from '../../common/cx'
import { formatRelative } from '../../common/format'
import Icon from '../../common/icons/Icon'
import card from '../../common/ui/FeedCard.module.css'
import ModuleTag from '../../common/ui/ModuleTag'
import ComplaintProgress from './ComplaintProgress'
import styles from './MyComplaintCard.module.css'

export default function MyComplaintCard({ complaint, delay }) {
  const { t, i18n } = useTranslation('complaints')
  const language = i18n.resolvedLanguage

  return (
    <article className={cx(card.card, 'module-complaints', 'rise')} style={{ '--delay': delay }}>
      <div className={card.head}>
        <ModuleTag module="complaints">{t('home.tag')}</ModuleTag>
        <span className={card.when}>{t('home.sent', { when: formatRelative(complaint.submittedAt, language) })}</span>
      </div>
      <h3 className={card.title}>{complaint.title}</h3>
      <p className={styles.address}>
        <Icon name="location_on" size={17} />
        <span>{complaint.address}</span>
      </p>
      <ComplaintProgress complaint={complaint} />
      {complaint.officialResponse && (
        <p className={styles.response}>
          <Icon name="campaign" size={18} />
          <span>
            <span className="sr-only">{t('home.response')}</span>
            {complaint.officialResponse.text}
          </span>
        </p>
      )}
    </article>
  )
}
