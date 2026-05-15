<template>
  <nav class="sticky top-0 z-50 bg-dark-900/80 backdrop-blur-md border-b border-dark-800">
    <div class="max-w-3xl mx-auto px-4 py-3 flex items-center justify-between">
      <router-link to="/" class="flex items-center gap-2 text-lg font-semibold text-dark-100 hover:text-primary-400 transition-colors">
        <span class="text-2xl">💪</span>
        <span>被拒之后</span>
      </router-link>
      <div class="flex items-center gap-2">
        <router-link
          :to="userStore.isLoggedIn ? '/post/new' : '/login?redirect=/post/new'"
          class="flex items-center gap-1 text-dark-400 hover:text-primary-400 transition-colors text-sm px-2 py-1"
        >
          <span>✏️</span>
          <span>发帖</span>
        </router-link>
        <template v-if="userStore.isLoggedIn">
          <router-link
            to="/profile"
            class="flex items-center gap-1 text-dark-400 hover:text-primary-400 transition-colors text-sm px-2 py-1"
          >
            <span>👤</span>
            <span>个人中心</span>
          </router-link>
          <router-link
            to="/admin"
            v-if="userStore.userInfo?.role === 'ADMIN'"
            class="flex items-center gap-1 text-dark-400 hover:text-primary-400 transition-colors text-sm px-2 py-1"
          >
            <span>⚙️</span>
            <span>管理后台</span>
          </router-link>
          <div class="w-px h-4 bg-dark-700 mx-2"></div>
          <span class="text-dark-500 text-sm">{{ userStore.userInfo?.nickname || '用户' }}</span>
          <button @click="userStore.logout()" class="btn-secondary text-sm py-1 px-3">退出</button>
        </template>
        <template v-else>
          <router-link to="/login" class="text-dark-400 hover:text-primary-400 transition-colors text-sm px-2 py-1">登录</router-link>
          <router-link to="/register" class="btn-primary text-sm py-1 px-3">注册</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
</script>
