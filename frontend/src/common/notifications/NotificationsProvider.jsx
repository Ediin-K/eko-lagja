import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { getNotifications, markNotificationRead } from '../data/summaries'
import { MODULE_ORDER } from '../modules'
import { NotificationsContext, TYPE_MODULE } from './notificationsContext'

export default function NotificationsProvider({ children }) {
  const { i18n } = useTranslation()
  const language = i18n.resolvedLanguage
  const [state, setState] = useState({ status: 'loading', items: [] })

  useEffect(() => {
    let active = true
    getNotifications(language)
      .then((items) => {
        if (active) setState({ status: 'ready', items })
      })
      .catch(() => {
        if (active) setState({ status: 'error', items: [] })
      })
    return () => {
      active = false
    }
  }, [language])

  const markRead = useCallback((notificationId) => {
    markNotificationRead(notificationId)
      .then(() => {
        setState((current) => ({
          ...current,
          items: current.items.map((item) =>
            item.id === notificationId && !item.readAt
              ? { ...item, readAt: new Date().toISOString() }
              : item,
          ),
        }))
      })
      .catch(() => {})
  }, [])

  const value = useMemo(() => {
    const unreadByModule = Object.fromEntries(MODULE_ORDER.map((module) => [module, 0]))
    for (const item of state.items) {
      const module = TYPE_MODULE[item.type]
      if (module && !item.readAt) unreadByModule[module] += 1
    }
    const unreadTotal = Object.values(unreadByModule).reduce((sum, count) => sum + count, 0)
    return { status: state.status, items: state.items, unreadByModule, unreadTotal, markRead }
  }, [state, markRead])

  return <NotificationsContext value={value}>{children}</NotificationsContext>
}
