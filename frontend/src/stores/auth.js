import { reactive, computed } from 'vue'

const TOKEN_KEY = 'resume_token'
const USER_KEY = 'resume_user'

const state = reactive({
  token: localStorage.getItem(TOKEN_KEY) || null,
  user: JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
})

export function useAuth() {
  const isLoggedIn = computed(() => !!state.token)
  const user = computed(() => state.user)

  function login(token, userInfo) {
    state.token = token
    state.user = userInfo
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
  }

  function logout() {
    state.token = null
    state.user = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  function getToken() {
    return state.token
  }

  return { isLoggedIn, user, login, logout, getToken }
}
