import { useEffect, useId, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { cx } from '../common/cx'
import { voteInPoll } from '../common/data/summaries'
import { formatNumber, formatPercent, formatRelative } from '../common/format'
import Icon from '../common/icons/Icon'
import card from '../common/ui/FeedCard.module.css'
import ModuleTag from '../common/ui/ModuleTag'
import styles from './HomeCards.module.css'

export default function PollCard({ poll, onPollChange, delay }) {
  const { t, i18n } = useTranslation('home')
  const language = i18n.resolvedLanguage
  const [selected, setSelected] = useState(null)
  const [problem, setProblem] = useState(null)
  const [saving, setSaving] = useState(false)
  const [justVoted, setJustVoted] = useState(false)
  const thanksRef = useRef(null)
  const questionId = useId()
  const problemId = useId()
  const groupName = useId()
  const voted = poll.myVote != null
  const total = poll.options.reduce((sum, option) => sum + option.votes, 0)

  useEffect(() => {
    if (justVoted) thanksRef.current?.focus()
  }, [justVoted])

  async function vote() {
    if (selected == null) {
      setProblem('chooseFirst')
      return
    }
    setProblem(null)
    setSaving(true)
    try {
      const updated = await voteInPoll(poll.id, selected, language)
      setJustVoted(true)
      onPollChange(updated)
    } catch {
      setProblem('failed')
    } finally {
      setSaving(false)
    }
  }

  function choose(optionId) {
    setSelected(optionId)
    setProblem(null)
  }

  return (
    <article className={cx(card.card, 'module-polls', 'rise')} style={{ '--delay': delay }}>
      <div className={card.head}>
        <ModuleTag module="polls">{t('poll.tag')}</ModuleTag>
        <span className={card.when}>{t('poll.closes', { when: formatRelative(poll.closesAt, language) })}</span>
      </div>
      <h3 id={questionId} className={card.title}>
        {poll.question}
      </h3>
      <fieldset
        className={styles.options}
        aria-labelledby={questionId}
        aria-describedby={problem ? problemId : undefined}
      >
        {poll.options.map((option) => {
          const percent = total ? Math.round((option.votes * 100) / total) : 0
          const checked = voted ? poll.myVote === option.id : selected === option.id
          return (
            <label key={option.id} className={cx(styles.option, voted && styles.optionVoted)}>
              <input
                type="radio"
                name={groupName}
                value={option.id}
                checked={checked}
                disabled={voted}
                onChange={() => choose(option.id)}
              />
              {voted && (
                <span
                  className={cx(styles.fill, justVoted && styles.fillGrow)}
                  style={{ width: `${percent}%` }}
                  aria-hidden="true"
                />
              )}
              <span className={styles.radio} aria-hidden="true" />
              <span className={styles.optionText}>{option.text}</span>
              {voted && <span className={styles.percent}>{formatPercent(percent, language)}</span>}
            </label>
          )
        })}
      </fieldset>
      <p id={problemId} className={styles.problem} role="alert" hidden={!problem}>
        {problem && t(`poll.${problem}`)}
      </p>
      <div className={card.foot}>
        <span className={card.when}>
          {t('poll.voters', { count: total, number: formatNumber(total, language) })}
        </span>
        {voted ? (
          <p ref={thanksRef} tabIndex={-1} className={styles.thanks}>
            <Icon name="check" size={20} />
            {t('poll.thanks')}
          </p>
        ) : (
          <button type="button" className={cx(card.button, card.solid)} onClick={vote} disabled={saving}>
            {t('poll.vote')}
          </button>
        )}
      </div>
    </article>
  )
}
