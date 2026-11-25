import { useTranslation } from 'react-i18next'
import { cx } from '../common/cx'
import { capitalize, formatRelative, formatTime, formatWeekdayDate } from '../common/format'
import Icon from '../common/icons/Icon'
import card from '../common/ui/FeedCard.module.css'
import ModuleTag from '../common/ui/ModuleTag'
import styles from './HomeCards.module.css'

export default function NoticeCard({ notice, delay }) {
  const { t, i18n } = useTranslation('home')
  const language = i18n.resolvedLanguage

  return (
    <article className={cx(card.card, 'module-notices', 'rise')} style={{ '--delay': delay }}>
      <div className={card.head}>
        <div className={card.tags}>
          <ModuleTag module="notices">{t('notice.tag')}</ModuleTag>
          {notice.urgent && <span className={cx(card.tag, card.urgent)}>{t('notice.urgent')}</span>}
        </div>
        <span className={card.when}>
          {t('common:byMunicipality', { when: formatRelative(notice.publishedAt, language) })}
        </span>
      </div>
      <h3 className={card.title}>{notice.title}</h3>
      <p className={card.text}>{notice.content}</p>
      {notice.startsAt && notice.endsAt && (
        <p className={styles.timebox}>
          <Icon name="schedule" size={20} />
          <span>{capitalize(formatWeekdayDate(notice.startsAt, language))}</span>
          <span className={styles.hours}>
            {t('common:timeRange', {
              start: formatTime(notice.startsAt, language),
              end: formatTime(notice.endsAt, language),
            })}
          </span>
        </p>
      )}
    </article>
  )
}
