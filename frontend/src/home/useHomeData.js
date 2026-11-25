import { useCallback, useEffect, useState } from 'react'
import {
  getCurrentResident,
  getLatestHelpPost,
  getLatestNotice,
  getMyLatestComplaint,
  getOpenPoll,
} from '../common/data/summaries'

export function useHomeData(language) {
  const [state, setState] = useState({ status: 'loading' })
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    let active = true
    Promise.all([
      getCurrentResident(language),
      getLatestNotice(language),
      getMyLatestComplaint(language),
      getOpenPoll(language),
      getLatestHelpPost(language),
    ])
      .then(([resident, notice, complaint, poll, helpPost]) => {
        if (active) setState({ status: 'ready', resident, notice, complaint, poll, helpPost })
      })
      .catch(() => {
        if (active) setState({ status: 'error' })
      })
    return () => {
      active = false
    }
  }, [language, attempt])

  const retry = useCallback(() => {
    setState({ status: 'loading' })
    setAttempt((count) => count + 1)
  }, [])

  const updatePoll = useCallback((poll) => {
    setState((current) => ({ ...current, poll }))
  }, [])

  return { ...state, retry, updatePoll }
}
