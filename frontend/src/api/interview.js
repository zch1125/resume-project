import request from './index'

export function startInterview(resumeId, jobDescription, imageData, imageType) {
  return request.post('/api/interview/start', { resumeId, jobDescription, imageData, imageType })
}

export function chatInterview(sessionId, userAnswer) {
  return request.post('/api/interview/chat', { sessionId, userAnswer })
}

export function getInterviewHistory(sessionId) {
  return request.get('/api/interview/history', { params: { sessionId } })
}