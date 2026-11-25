import { useTranslation } from 'react-i18next'
import { cx } from '../../common/cx'
import { formatDayMonth } from '../../common/format'
import Icon from '../../common/icons/Icon'
import styles from './MyComplaintCard.module.css'

export default function ComplaintProgress({ complaint }) {
  const { t, i18n } = useTranslation('complaints')
  const language = i18n.resolvedLanguage
  const rejected = complaint.status === 'REJECTED'
  const steps = ['RECEIVED', 'IN_PROGRESS', rejected ? 'REJECTED' : 'RESOLVED']
  const current = steps.indexOf(complaint.status)
  const last = steps.length - 1
  const reachedAt = { RECEIVED: complaint.submittedAt }
  for (const change of complaint.statusChanges) reachedAt[change.toStatus] = change.changedAt

  return (
    <ol
      className={cx(styles.track, rejected && styles.trackRejected)}
      aria-label={t('home.progress')}
      style={{ '--progress': current / last }}
    >
      {steps.map((step, index) => {
        const done = index < current || (index === current && index === last)
        const reached = index <= current && reachedAt[step]
        return (
          <li
            key={step}
            className={cx(done && styles.done, index === current && styles.now, step === 'REJECTED' && styles.rejected)}
            aria-current={index === current ? 'step' : undefined}
          >
            <span className={styles.dot}>
              {done && <Icon name={step === 'REJECTED' ? 'close' : 'check'} size={14} />}
            </span>
            <span>{t(`status.${step}`)}</span>
            <span className={styles.date}>{reached ? formatDayMonth(reachedAt[step], language) : null}</span>
          </li>
        )
      })}
    </ol>
  )
}
