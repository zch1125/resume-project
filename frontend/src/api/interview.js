import request from './index'

export function startInterview(resumeId, jobDescription) {
  return request.post('/api/interview/start', { resumeId, jobDescription })
}

export function chatInterview(sessionId, userAnswer) {
  return request.post('/api/interview/chat', { sessionId, userAnswer })
}
