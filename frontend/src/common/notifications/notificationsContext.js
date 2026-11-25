import { createContext, useContext } from 'react'

export const TYPE_MODULE = {
  NEW_NOTICE: 'notices',
  COMPLAINT_STATUS_CHANGED: 'complaints',
  POLL_OPENED: 'polls',
  POLL_CLOSED: 'polls',
  HELP_RESPONSE: 'help',
}

export const NotificationsContext = createContext(null)

export function useNotifications() {
  return useContext(NotificationsContext)
}
