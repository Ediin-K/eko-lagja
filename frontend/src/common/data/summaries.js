import { sample, sampleTexts } from './sampleData'

export const USES_SAMPLE_DATA = true

const SUBJECTS = {
  NEW_NOTICE: (texts, id) => texts.notices[id]?.title,
  COMPLAINT_STATUS_CHANGED: (texts, id) => texts.complaints[id]?.title,
  POLL_OPENED: (texts, id) => texts.polls[id]?.question,
  POLL_CLOSED: (texts, id) => texts.polls[id]?.question,
  HELP_RESPONSE: (texts, id) => texts.helpPosts[id]?.title,
}

function pollWithTexts(poll, language) {
  const texts = sampleTexts(language).polls[poll.id]
  return {
    ...poll,
    question: texts.question,
    options: poll.options.map((option) => ({ ...option, text: texts.options[option.id] })),
  }
}

export async function getCurrentResident(language) {
  return { ...sampleTexts(language).resident }
}

export async function getNotifications(language) {
  const texts = sampleTexts(language)
  return sample.notifications.map((notification) => ({
    ...notification,
    subject: SUBJECTS[notification.type]?.(texts, notification.itemId) ?? '',
  }))
}

export async function markNotificationRead(notificationId) {
  const notification = sample.notifications.find((item) => item.id === notificationId)
  if (notification && !notification.readAt) notification.readAt = new Date().toISOString()
}

export async function getLatestNotice(language) {
  const notice = sample.notices[0]
  if (!notice) return null
  return { ...notice, ...sampleTexts(language).notices[notice.id] }
}

export async function getMyLatestComplaint(language) {
  const complaint = sample.complaints[0]
  if (!complaint) return null
  const texts = sampleTexts(language).complaints[complaint.id]
  return {
    ...complaint,
    title: texts.title,
    address: texts.address,
    statusChanges: complaint.statusChanges.map((change) => ({ ...change })),
    officialResponse: complaint.officialResponse && {
      ...complaint.officialResponse,
      text: texts.response,
    },
  }
}

export async function getOpenPoll(language) {
  const poll = sample.polls[0]
  return poll ? pollWithTexts(poll, language) : null
}

export async function voteInPoll(pollId, optionId, language) {
  const poll = sample.polls.find((item) => item.id === pollId)
  const option = poll?.options.find((item) => item.id === optionId)
  if (!option || poll.myVote != null) throw new Error('VOTE_NOT_ALLOWED')
  option.votes += 1
  poll.myVote = optionId
  return pollWithTexts(poll, language)
}

export async function getLatestHelpPost(language) {
  const post = sample.helpPosts[0]
  if (!post) return null
  return { ...post, ...sampleTexts(language).helpPosts[post.id] }
}
