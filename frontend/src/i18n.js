import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

// Each module keeps its own texts in src/<module>/locales/<language>.json.
// Vite finds these files when it builds the app, so adding a module never
// requires editing this file.
const files = import.meta.glob('./*/locales/*.json', { eager: true, import: 'default' })

const SUPPORTED_LANGUAGES = ['sq', 'en']
const DEFAULT_LANGUAGE = 'sq'
const STORAGE_KEY = 'ekolagja-language'

// './complaints/locales/sq.json' becomes resources.sq.complaints
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

// Keep <html lang> in step with the chosen language (screen readers use it)
// and remember the choice for the next visit.
i18n.on('languageChanged', (language) => {
  document.documentElement.lang = language
  try {
    localStorage.setItem(STORAGE_KEY, language)
  } catch {
    // Storage can be blocked (private mode); the choice then lasts for this visit only.
  }
})

i18n.use(initReactI18next).init({
  resources,
  ns: Object.keys(resources[DEFAULT_LANGUAGE] ?? {}),
  lng: savedLanguage(),
  fallbackLng: DEFAULT_LANGUAGE,
  supportedLngs: SUPPORTED_LANGUAGES,
  interpolation: { escapeValue: false }, // React already escapes text
})

export default i18n
