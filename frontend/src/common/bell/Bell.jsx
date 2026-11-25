import { useId, useLayoutEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useLocation } from 'react-router-dom'
import { cx } from '../cx'
import { capitalize, formatRelative } from '../format'
import Icon from '../icons/Icon'
import { MODULE_ORDER, MODULES } from '../modules'
import { TYPE_MODULE, useNotifications } from '../notifications/notificationsContext'
import styles from './Bell.module.css'

const PHONE_SCREEN = '(max-width: 639.98px)'

function byNewestFirst(a, b) {
  return new Date(b.createdAt) - new Date(a.createdAt)
}

export default function Bell() {
  const { t, i18n } = useTranslation('common')
  const language = i18n.resolvedLanguage
  const { status, items, unreadByModule, unreadTotal, markRead } = useNotifications()
  const [open, setOpen] = useState(false)
  const buttonRef = useRef(null)
  const panelRef = useRef(null)
  const leavingRef = useRef(false)
  const { pathname } = useLocation()
  const panelId = useId()
  const titleId = useId()
  const subtitleId = useId()

  useLayoutEffect(() => {
    if (!open) return undefined
    const root = document.documentElement
    function place() {
      const panel = panelRef.current
      const button = buttonRef.current
      if (!panel || !button) return
      if (window.matchMedia(PHONE_SCREEN).matches) {
        panel.style.top = ''
        panel.style.left = ''
        return
      }
      const rect = button.getBoundingClientRect()
      const width = panel.offsetWidth
      const left = Math.max(16, Math.min(rect.right - width, root.clientWidth - width - 16))
      panel.style.top = `${Math.round(rect.bottom + 8)}px`
      panel.style.left = `${Math.round(left)}px`
    }
    root.style.overflow = 'hidden'
    place()
    window.addEventListener('resize', place)
    return () => {
      root.style.overflow = ''
      window.removeEventListener('resize', place)
    }
  }, [open])

  function openPanel() {
    if (typeof panelRef.current?.showModal !== 'function') return
    panelRef.current.showModal()
    setOpen(true)
  }

  function closePanel() {
    panelRef.current?.close()
  }

  function handleClose() {
    setOpen(false)
    if (leavingRef.current) {
      leavingRef.current = false
      return
    }
    buttonRef.current?.focus()
  }

  function handlePanelClick(event) {
    if (event.target === panelRef.current) closePanel()
  }

  function openItem(item) {
    if (!item.readAt) markRead(item.id)
    leavingRef.current = MODULES[TYPE_MODULE[item.type]].path !== pathname
    closePanel()
  }

  function itemText(item) {
    return t(`notification.${item.type}`, {
      subject: item.subject,
      status: item.toStatus ? t(`complaints:status.${item.toStatus}`) : '',
    })
  }

  function itemWhen(item) {
    const when = formatRelative(item.createdAt, language)
    return item.type === 'NEW_NOTICE' ? t('byMunicipality', { when }) : capitalize(when)
  }

  const groups = MODULE_ORDER.map((module) => ({
    module,
    unread: unreadByModule[module],
    items: items.filter((item) => TYPE_MODULE[item.type] === module).sort(byNewestFirst),
  })).filter((group) => group.items.length > 0)

  return (
    <>
      <button
        ref={buttonRef}
        type="button"
        className={styles.bellButton}
        aria-label={t('bell.label', { count: unreadTotal })}
        aria-haspopup="dialog"
        aria-expanded={open}
        aria-controls={panelId}
        onClick={openPanel}
      >
        <Icon name="notifications" />
        {unreadTotal > 0 && <span className={styles.dot} aria-hidden="true" />}
      </button>

      <dialog
        ref={panelRef}
        id={panelId}
        className={styles.panel}
        aria-labelledby={titleId}
        aria-describedby={subtitleId}
        onClose={handleClose}
        onClick={handlePanelClick}
      >
        <div className={styles.head}>
          <h2 id={titleId} className={styles.title}>
            {t('bell.title')}
          </h2>
          <span id={subtitleId} className={styles.subtitle}>
            {t('newCount', { count: unreadTotal })}
          </span>
          <button type="button" className={styles.close} aria-label={t('bell.close')} onClick={closePanel}>
            <Icon name="close" />
          </button>
        </div>
        <div className={styles.body}>
          {status === 'error' && <p className={styles.message}>{t('bell.error')}</p>}
          {status === 'ready' && groups.length === 0 && <p className={styles.message}>{t('bell.empty')}</p>}
          {groups.map((group) => (
            <section key={group.module} aria-labelledby={`${panelId}-${group.module}`}>
              <h3 id={`${panelId}-${group.module}`} className={styles.groupHead}>
                <span className={cx(styles.groupIcon, `module-${group.module}`)}>
                  <Icon name={MODULES[group.module].icon} size={18} />
                </span>
                <span>{t(`modules.${group.module}`)}</span>
                <span className={styles.groupNew}>{t('newCount', { count: group.unread })}</span>
              </h3>
              <ul className={styles.list}>
                {group.items.map((item) => (
                  <li key={item.id}>
                    <Link
                      to={MODULES[group.module].path}
                      className={cx(styles.item, !item.readAt && styles.unread)}
                      onClick={() => openItem(item)}
                    >
                      <span className={styles.itemDot} aria-hidden="true" />
                      <span className={styles.itemText}>
                        <span className={styles.itemTitle}>
                          {!item.readAt && <span className="sr-only">{t('bell.newItem')}</span>}
                          {itemText(item)}
                        </span>
                        <span className={styles.itemWhen}>{itemWhen(item)}</span>
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>
            </section>
          ))}
        </div>
      </dialog>
    </>
  )
}
