<template>
  <div id="resume-app">
    <nav class="app-nav" v-if="showNav">
      <div class="nav-inner">
        <router-link to="/" class="nav-brand">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="nav-logo">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
          </svg>
          <span class="nav-title">简历分析平台</span>
        </router-link>

        <div class="nav-right">
          <div class="user-menu" v-if="isLoggedIn">
            <div class="user-avatar" @click="showMenu = !showMenu">
              <span class="avatar-text">{{ userInitial }}</span>
            </div>
            <span class="user-name">{{ nickname }}</span>
            <transition name="menu-fade">
              <div class="dropdown-menu" v-if="showMenu" @click.stop>
                <div class="dropdown-header">
                  <span class="dropdown-name">{{ nickname }}</span>
                  <span class="dropdown-username">@{{ username }}</span>
                </div>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item logout-item" @click="handleLogout">
                  <span>🚪</span>
                  <span>退出登录</span>
                </button>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </nav>

    <!-- Click overlay to close menu -->
    <div class="menu-overlay" v-if="showMenu" @click="showMenu = false"></div>

    <router-view />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from './stores/auth'

const route = useRoute()
const router = useRouter()
const { isLoggedIn, user, logout } = useAuth()
const showMenu = ref(false)

const showNav = computed(() => route.name !== 'Login')
const nickname = computed(() => user.value?.nickname || user.value?.username || '')
const username = computed(() => user.value?.username || '')
const userInitial = computed(() => nickname.value.charAt(0).toUpperCase() || 'U')

watch(() => route.path, () => { showMenu.value = false })

function handleLogout() {
  showMenu.value = false
  logout()
  router.push('/login')
}
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

:root {
  --primary-50: #eef2ff;
  --primary-100: #e0e7ff;
  --primary-200: #c7d2fe;
  --primary-300: #a5b4fc;
  --primary-400: #818cf8;
  --primary-500: #6366f1;
  --primary-600: #4f46e5;
  --primary-700: #4338ca;
  --primary-800: #3730a3;
  --primary-900: #312e81;

  --neutral-50: #f8fafc;
  --neutral-100: #f1f5f9;
  --neutral-200: #e2e8f0;
  --neutral-300: #cbd5e1;
  --neutral-400: #94a3b8;
  --neutral-500: #64748b;
  --neutral-600: #475569;
  --neutral-700: #334155;
  --neutral-800: #1e293b;
  --neutral-900: #0f172a;

  --success: #10b981;
  --warning: #f59e0b;
  --danger: #ef4444;
  --info: #6366f1;

  --glass-bg: rgba(255, 255, 255, 0.75);
  --glass-border: rgba(255, 255, 255, 0.3);
  --glass-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  --glass-blur: 16px;

  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.04);
  --shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.10), 0 4px 8px rgba(0, 0, 0, 0.04);
  --shadow-xl: 0 16px 48px rgba(0, 0, 0, 0.12), 0 8px 16px rgba(0, 0, 0, 0.06);

  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
  --radius-full: 9999px;

  --ease-out-expo: cubic-bezier(0.16, 1, 0.3, 1);
  --ease-out-quart: cubic-bezier(0.25, 1, 0.5, 1);
  --ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  color: var(--neutral-800);
  min-height: 100vh;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

body::before,
body::after {
  content: '';
  position: fixed;
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}

body::before {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, transparent 70%);
  top: -200px;
  right: -200px;
  animation: orbFloat 20s ease-in-out infinite alternate;
}

body::after {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.10) 0%, transparent 70%);
  bottom: -150px;
  left: -150px;
  animation: orbFloat 25s ease-in-out infinite alternate-reverse;
}

@keyframes orbFloat {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(80px, 60px) scale(1.15); }
}

#resume-app {
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: var(--neutral-300); border-radius: var(--radius-full); }
::-webkit-scrollbar-thumb:hover { background: var(--neutral-400); }

::selection { background: var(--primary-200); color: var(--primary-900); }
</style>

<style scoped>
/* Navigation Bar */
.app-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0 20px;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.nav-inner {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: #e2e8f0;
  font-weight: 700;
  font-size: 16px;
  transition: opacity 0.2s;
}

.nav-brand:hover {
  opacity: 0.8;
}

.nav-logo {
  color: var(--primary-400);
}

.nav-title {
  letter-spacing: 0.3px;
}

.nav-right {
  display: flex;
  align-items: center;
}

/* User Menu */
.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-500), var(--primary-400));
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 0 12px rgba(99, 102, 241, 0.3);
}

.avatar-text {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
}

/* Dropdown */
.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 180px;
  background: rgba(30, 41, 59, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-md);
  padding: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  z-index: 200;
}

.menu-fade-enter-active,
.menu-fade-leave-active {
  transition: all 0.2s ease;
}

.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.dropdown-header {
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dropdown-name {
  font-size: 14px;
  font-weight: 600;
  color: #e2e8f0;
}

.dropdown-username {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.dropdown-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.06);
  margin: 4px 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 14px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-item {
  color: rgba(255, 255, 255, 0.6);
}

.logout-item:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #f87171;
}

.menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 99;
}
</style>
