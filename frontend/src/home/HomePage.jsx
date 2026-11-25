import { useId } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import MyComplaintCard from '../complaints/home/MyComplaintCard'
import { cx } from '../common/cx'
import { USES_SAMPLE_DATA } from '../common/data/summaries'
import Icon from '../common/icons/Icon'
import PageBand from '../common/layout/PageBand'
import { MODULES } from '../common/modules'
import card from '../common/ui/FeedCard.module.css'
import HelpCard from './HelpCard'
import HomeHero from './HomeHero'
import NoticeCard from './NoticeCard'
import PollCard from './PollCard'
import ShortcutTiles from './ShortcutTiles'
import { useHomeData } from './useHomeData'
import styles from './HomePage.module.css'

function FeedLoading() {
  const { t } = useTranslation('home')
  return (
    <div className={styles.feed}>
      <p role="status" className="sr-only">
        {t('feed.loading')}
      </p>
      <div className={styles.skeleton} aria-hidden="true" />
      <div className={styles.skeleton} aria-hidden="true" />
    </div>
  )
}

function FeedError({ onRetry }) {
  const { t } = useTranslation('home')
  return (
    <div className={cx(card.card, styles.message)} role="alert">
      <span className={cx(styles.messageIcon, styles.errorIcon)}>
        <Icon name="error" />
      </span>
      <p className={card.text}>{t('feed.error')}</p>
      <button type="button" className={cx(card.button, card.outline)} onClick={onRetry}>
        {t('feed.retry')}
      </button>
    </div>
  )
}

function FeedEmpty() {
  const { t } = useTranslation('home')
  return (
    <div className={cx(card.card, styles.message)}>
      <span className={styles.messageIcon}>
        <Icon name="wb_sunny" />
      </span>
      <p className={styles.messageTitle}>{t('empty.title')}</p>
      <p className={card.text}>{t('empty.body')}</p>
      <Link to={MODULES.notices.path} className={cx(card.button, card.outline)}>
        {t('empty.action')}
      </Link>
    </div>
  )
}

export default function HomePage() {
  const { t, i18n } = useTranslation('home')
  const { status, resident, notice, complaint, poll, helpPost, retry, updatePoll } = useHomeData(
    i18n.resolvedLanguage,
  )
  const feedTitleId = useId()
  const hasItems = Boolean(notice || complaint || poll || helpPost)

  return (
    <>
      <title>{t('common:pageTitle', { page: t('common:menu.home') })}</title>
      <PageBand>
        <HomeHero resident={resident} />
      </PageBand>
      <div className="wrap">
        <ShortcutTiles />
        <section aria-labelledby={feedTitleId} className={styles.section}>
          <div className={cx(styles.sectionHead, 'rise')} style={{ '--delay': 3 }}>
            <h2 id={feedTitleId} className={styles.sectionTitle}>
              {t('feed.title')}
            </h2>
            {USES_SAMPLE_DATA && <span className={styles.sample}>{t('feed.sample')}</span>}
          </div>
          {status === 'loading' && <FeedLoading />}
          {status === 'error' && <FeedError onRetry={retry} />}
          {status === 'ready' && !hasItems && <FeedEmpty />}
          {status === 'ready' && hasItems && (
            <div className={styles.feed}>
              {notice && <NoticeCard notice={notice} delay={4} />}
              {complaint && <MyComplaintCard complaint={complaint} delay={5} />}
              {poll && <PollCard poll={poll} onPollChange={updatePoll} delay={6} />}
              {helpPost && <HelpCard post={helpPost} delay={7} />}
            </div>
          )}
        </section>
      </div>
    </>
  )
}
