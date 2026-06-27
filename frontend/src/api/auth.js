import request from './index'

export function loginApi(data) {
  return request.post('/api/auth/login', data)
}

export function registerApi(data) {
  return request.post('/api/auth/register', data)
}

export function getMe() {
  return request.get('/api/auth/me')
}
