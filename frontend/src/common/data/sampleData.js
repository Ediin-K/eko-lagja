import en from './sample/en.json'
import sq from './sample/sq.json'

const TEXTS = { sq, en }
const MINUTE = 60 * 1000
const HOUR = 60 * MINUTE
const DAY = 24 * HOUR
const loadedAt = Date.now()

function ago(milliseconds) {
  return new Date(loadedAt - milliseconds).toISOString()
}

function later(milliseconds) {
  return new Date(loadedAt + milliseconds).toISOString()
}

function nextFridayAt(hour) {
  const date = new Date(loadedAt)
  date.setDate(date.getDate() + ((5 - date.getDay() + 7) % 7 || 7))
  date.setHours(hour, 0, 0, 0)
  return date.toISOString()
}

export function sampleTexts(language) {
  return TEXTS[language] ?? TEXTS.sq
}

export const sample = {
  notices: [
    {
      id: 41,
      urgent: true,
      publishedAt: ago(3 * HOUR),
      startsAt: nextFridayAt(9),
      endsAt: nextFridayAt(13),
    },
  ],
  complaints: [
    {
      id: 17,
      status: 'IN_PROGRESS',
      submittedAt: ago(2 * DAY),
      statusChanges: [
        { fromStatus: 'RECEIVED', toStatus: 'IN_PROGRESS', changedAt: ago(DAY + 2 * HOUR) },
      ],
      officialResponse: { respondedAt: ago(DAY + HOUR) },
    },
  ],
  polls: [
    {
      id: 8,
      closesAt: later(5 * DAY),
      options: [
        { id: 81, votes: 98 },
        { id: 82, votes: 71 },
        { id: 83, votes: 45 },
      ],
      myVote: null,
    },
  ],
  helpPosts: [{ id: 23, createdAt: ago(2 * HOUR) }],
  notifications: [
    { id: 105, type: 'NEW_NOTICE', itemId: 41, createdAt: ago(3 * HOUR), readAt: null },
    { id: 104, type: 'NEW_NOTICE', itemId: 42, createdAt: ago(DAY + 4 * HOUR), readAt: null },
    {
      id: 103,
      type: 'COMPLAINT_STATUS_CHANGED',
      itemId: 17,
      toStatus: 'IN_PROGRESS',
      createdAt: ago(DAY + 2 * HOUR),
      readAt: null,
    },
    { id: 102, type: 'POLL_OPENED', itemId: 8, createdAt: ago(2 * DAY), readAt: null },
    { id: 101, type: 'HELP_RESPONSE', itemId: 19, createdAt: ago(3 * DAY), readAt: ago(2 * DAY) },
  ],
}
