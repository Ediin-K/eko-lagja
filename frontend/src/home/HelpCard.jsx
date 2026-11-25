import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { cx } from '../common/cx'
import { formatRelative } from '../common/format'
import { MODULES } from '../common/modules'
import card from '../common/ui/FeedCard.module.css'
import ModuleTag from '../common/ui/ModuleTag'
import styles from './HomeCards.module.css'

export default function HelpCard({ post, delay }) {
  const { t, i18n } = useTranslation('home')
  const language = i18n.resolvedLanguage

  return (
    <article className={cx(card.card, 'module-help', 'rise')} style={{ '--delay': delay }}>
      <div className={card.head}>
        <ModuleTag module="help">{t('help.tag')}</ModuleTag>
      </div>
      <h3 className={card.title}>{post.title}</h3>
      <p className={card.text}>{post.description}</p>
      <div className={card.foot}>
        <span className={styles.who}>
          <span className={styles.avatar} aria-hidden="true">
            {post.authorInitial}
          </span>
          <span>{t('help.where', { place: post.place, when: formatRelative(post.createdAt, language) })}</span>
        </span>
        <Link to={MODULES.help.path} className={cx(card.button, card.outline)}>
          {t('help.offer')}
        </Link>
      </div>
    </article>
  )
}
