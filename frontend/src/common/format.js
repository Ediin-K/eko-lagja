import i18n from '../i18n'

const INTL_LOCALES = { sq: 'sq-XK', en: 'en-GB' }
const MINUTE = 60 * 1000
const HOUR = 60 * MINUTE
const DAY = 24 * HOUR
const FALLBACK_KEYS = {
  minute: { past: 'minutesAgo', future: 'inMinutes' },
  hour: { past: 'hoursAgo', future: 'inHours' },
  day: { past: 'daysAgo', future: 'inDays' },
}
const intlSupport = new Map()

function intlLocale(language) {
  return INTL_LOCALES[language] ?? language
}

function hasIntlData(language) {
  if (!intlSupport.has(language)) {
    const locale = intlLocale(language)
    let supported
    try {
      supported =
        Intl.DateTimeFormat.supportedLocalesOf(locale).length > 0 &&
        Intl.RelativeTimeFormat.supportedLocalesOf(locale).length > 0 &&
        Intl.NumberFormat.supportedLocalesOf(locale).length > 0
    } catch {
      supported = false
    }
    intlSupport.set(language, supported)
  }
  return intlSupport.get(language)
}

function dateWords(key, language, options = {}) {
  return i18n.t(`common:dates.${key}`, { lng: language, ...options })
}

function dateList(key, language) {
  return i18n.t(`common:dates.${key}`, { lng: language, returnObjects: true })
}

function calendarDaysBetween(date, now) {
  const day = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  return Math.round((day - today) / DAY)
}

function relativeFallback(amount, unit, language) {
  if (amount === 0) return dateWords(unit === 'day' ? 'today' : 'now', language)
  if (unit === 'day' && amount === -1) return dateWords('yesterday', language)
  if (unit === 'day' && amount === 1) return dateWords('tomorrow', language)
  const keys = FALLBACK_KEYS[unit]
  return dateWords(amount < 0 ? keys.past : keys.future, language, { count: Math.abs(amount) })
}

export function capitalize(text) {
  return text ? text.charAt(0).toUpperCase() + text.slice(1) : text
}

export function formatRelative(value, language, now = new Date()) {
  const date = new Date(value)
  const diff = date.getTime() - now.getTime()
  let amount = Math.round(diff / MINUTE)
  let unit = 'minute'
  if (Math.abs(amount) >= 60) {
    amount = Math.round(diff / HOUR)
    unit = 'hour'
  }
  if (unit === 'hour' && Math.abs(amount) >= 24) {
    amount = calendarDaysBetween(date, now)
    unit = 'day'
  }
  if (unit === 'day' && Math.abs(amount) > 6) return formatDayMonth(date, language)
  if (hasIntlData(language)) {
    return new Intl.RelativeTimeFormat(intlLocale(language), { numeric: 'auto' }).format(amount, unit)
  }
  return relativeFallback(amount, unit, language)
}

export function formatDayMonth(value, language) {
  const date = new Date(value)
  if (hasIntlData(language)) {
    return new Intl.DateTimeFormat(intlLocale(language), { day: 'numeric', month: 'short' }).format(date)
  }
  return dateWords('dayMonth', language, {
    day: date.getDate(),
    month: dateList('monthsShort', language)[date.getMonth()],
  })
}

export function formatWeekdayDate(value, language) {
  const date = new Date(value)
  if (hasIntlData(language)) {
    return new Intl.DateTimeFormat(intlLocale(language), {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
    }).format(date)
  }
  return dateWords('weekdayDate', language, {
    weekday: dateList('weekdays', language)[date.getDay()],
    day: date.getDate(),
    month: dateList('months', language)[date.getMonth()],
  })
}

export function formatTime(value, language) {
  const date = new Date(value)
  if (hasIntlData(language)) {
    return new Intl.DateTimeFormat(intlLocale(language), {
      hour: '2-digit',
      minute: '2-digit',
      hourCycle: 'h23',
    }).format(date)
  }
  const pad = (number) => String(number).padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function formatNumber(value, language) {
  if (hasIntlData(language)) return new Intl.NumberFormat(intlLocale(language)).format(value)
  return String(Math.round(value)).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
}

export function formatPercent(value, language) {
  if (hasIntlData(language)) {
    return new Intl.NumberFormat(intlLocale(language), {
      style: 'percent',
      maximumFractionDigits: 0,
    }).format(value / 100)
  }
  return `${Math.round(value)}%`
}
