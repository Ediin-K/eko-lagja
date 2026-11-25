const STORAGE_KEY = 'ekolagja-theme'
const BAR_COLORS = { light: '#1E7A4C', dark: '#123322' }

export function currentTheme() {
  return document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light'
}

export function applyTheme(theme) {
  document.documentElement.dataset.theme = theme
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', BAR_COLORS[theme])
  try {
    localStorage.setItem(STORAGE_KEY, theme)
  } catch {}
}
