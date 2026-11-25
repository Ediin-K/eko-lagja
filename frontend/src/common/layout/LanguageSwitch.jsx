import { useTranslation } from 'react-i18next'
import { SUPPORTED_LANGUAGES } from '../../i18n'
import styles from './LanguageSwitch.module.css'

export default function LanguageSwitch() {
  const { t, i18n } = useTranslation('common')
  const current = i18n.resolvedLanguage

  return (
    <div className={styles.switch} role="group" aria-label={t('language.label')}>
      <span
        className={styles.thumb}
        style={{ '--index': Math.max(0, SUPPORTED_LANGUAGES.indexOf(current)) }}
        aria-hidden="true"
      />
      {SUPPORTED_LANGUAGES.map((language) => (
        <button
          key={language}
          type="button"
          lang={language}
          className={styles.option}
          aria-label={t(`language.${language}.name`)}
          aria-pressed={current === language}
          onClick={() => i18n.changeLanguage(language)}
        >
          {t(`language.${language}.short`)}
        </button>
      ))}
    </div>
  )
}
