import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

const files = import.meta.glob('./*/locales/*.json', { eager: true, import: 'default' })

export const SUPPORTED_LANGUAGES = ['sq', 'en']
const DEFAULT_LANGUAGE = 'sq'
const STORAGE_KEY = 'ekolagja-language'

const resources = {}
for (const [path, texts] of Object.entries(files)) {
  const [, namespace, , fileName] = path.split('/')
  const language = fileName.replace('.json', '')
  if (!SUPPORTED_LANGUAGES.includes(language)) continue
  resources[language] ??= {}
  resources[language][namespace] = texts
}

function savedLanguage() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    return SUPPORTED_LANGUAGES.includes(saved) ? saved : DEFAULT_LANGUAGE
  } catch {
    return DEFAULT_LANGUAGE
  }
}

i18n.on('languageChanged', (language) => {
  document.documentElement.lang = language
  try {
    localStorage.setItem(STORAGE_KEY, language)
  } catch {}
})

i18n.use(initReactI18next).init({
  resources,
  ns: Object.keys(resources[DEFAULT_LANGUAGE] ?? {}),
  lng: savedLanguage(),
  fallbackLng: DEFAULT_LANGUAGE,
  supportedLngs: SUPPORTED_LANGUAGES,
  interpolation: { escapeValue: false },
})

export default i18n
