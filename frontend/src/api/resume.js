import request from './index'

export function uploadResume(file, sessionId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('sessionId', sessionId)
  return request.post('/api/resume/upload', formData)
}

export function parseResume(resumeId) {
  return request.post(`/api/resume/parse/${resumeId}`)
}

export function getResumeAdvice(resumeId, jobTitle) {
  return request.post('/api/resume/advice', { resumeId, jobTitle })
}

export function chatAdvice(suggestionId, userMessage) {
  return request.post('/api/resume/advice/chat', { suggestionId, userMessage })
}

export function getResumeById(resumeId) {
  return request.get(`/api/resume/${resumeId}`)
}