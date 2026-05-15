<template>
  <div class="max-w-md mx-auto px-4 py-12">
    <div class="text-center mb-8">
      <h2 class="text-2xl font-bold text-dark-100 mb-2">欢迎回来</h2>
      <p class="text-dark-400 text-sm">邮箱+密码登录</p>
    </div>

    <div v-if="registeredMsg" class="mb-4 p-3 rounded-lg bg-green-900/30 border border-green-700/50 text-green-400 text-sm text-center">
      {{ registeredMsg }}
    </div>

    <div class="card space-y-4">
      <div>
        <label class="block text-dark-300 text-sm mb-1">邮箱</label>
        <input v-model="email" type="email" class="input-field" placeholder="your@email.com" @blur="validateEmail" />
        <p v-if="emailError" class="text-red-400 text-xs mt-1">{{ emailError }}</p>
      </div>
      <div>
        <label class="block text-dark-300 text-sm mb-1">密码</label>
        <input v-model="password" type="password" class="input-field" placeholder="输入密码" @keyup.enter="login" />
      </div>
      <p v-if="loginError" class="text-red-400 text-sm text-center">{{ loginError }}</p>
      <button @click="login" :disabled="!email || !password" class="btn-primary w-full">登录</button>
      <p class="text-center text-dark-500 text-sm">
        还没有账号？<router-link to="/register" class="text-primary-400 hover:text-primary-300">去注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const email = ref('')
const password = ref('')
const emailError = ref('')
const loginError = ref('')
const registeredMsg = ref(route.query.registered === '1' ? '注册成功，请登录' : '')

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

function validateEmail() {
  if (!email.value) {
    emailError.value = '邮箱不能为空'
  } else if (!EMAIL_REGEX.test(email.value)) {
    emailError.value = '邮箱格式不正确'
  } else {
    emailError.value = ''
  }
}

async function login() {
  validateEmail()
  if (emailError.value) return
  if (!password.value) {
    loginError.value = '密码不能为空'
    return
  }

  try {
    loginError.value = ''
    const res = await authApi.login({ email: email.value, password: password.value })
    userStore.setToken(res.data.token)
    await userStore.fetchUser()
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    loginError.value = e.message || '登录失败'
  }
}
</script>
