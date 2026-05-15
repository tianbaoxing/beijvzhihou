<template>
  <div class="max-w-md mx-auto px-4 py-12">
    <div class="text-center mb-8">
      <h2 class="text-2xl font-bold text-dark-100 mb-2">加入我们</h2>
      <p class="text-dark-400 text-sm">注册后可以发帖，获得AI安慰</p>
    </div>

    <div class="card space-y-4">
      <div>
        <label class="block text-dark-300 text-sm mb-1">邮箱</label>
        <input v-model="email" type="email" class="input-field" placeholder="your@email.com" @blur="validateEmail" />
        <p v-if="emailError" class="text-red-400 text-xs mt-1">{{ emailError }}</p>
      </div>
      <div>
        <label class="block text-dark-300 text-sm mb-1">验证码</label>
        <div class="flex gap-2">
          <input v-model="code" type="text" class="input-field flex-1" placeholder="6位验证码" maxlength="6" />
          <button @click="sendCode" :disabled="countdown > 0 || !isEmailValid" class="btn-secondary text-sm whitespace-nowrap">
            {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
          </button>
        </div>
        <p v-if="codeError" class="text-red-400 text-xs mt-1">{{ codeError }}</p>
      </div>
      <div>
        <label class="block text-dark-300 text-sm mb-1">密码</label>
        <input v-model="password" type="password" class="input-field" placeholder="至少8位，包含字母和数字" @blur="validatePassword" />
        <p v-if="passwordError" class="text-red-400 text-xs mt-1">{{ passwordError }}</p>
      </div>
      <div>
        <label class="block text-dark-300 text-sm mb-1">确认密码</label>
        <input v-model="confirmPassword" type="password" class="input-field" placeholder="再次输入密码" @blur="validateConfirmPassword" />
        <p v-if="confirmPasswordError" class="text-red-400 text-xs mt-1">{{ confirmPasswordError }}</p>
      </div>
      <div>
        <label class="block text-dark-300 text-sm mb-1">昵称（选填）</label>
        <input v-model="nickname" type="text" class="input-field" placeholder="不填会自动生成" />
      </div>
      <p v-if="serverError" class="text-red-400 text-sm text-center">{{ serverError }}</p>
      <button @click="register" :disabled="!canSubmit" class="btn-primary w-full">注册</button>
      <p class="text-center text-dark-500 text-sm">
        已有账号？<router-link to="/login" class="text-primary-400 hover:text-primary-300">去登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'

const router = useRouter()
const email = ref('')
const code = ref('')
const password = ref('')
const confirmPassword = ref('')
const nickname = ref('')
const countdown = ref(0)
const emailError = ref('')
const codeError = ref('')
const passwordError = ref('')
const confirmPasswordError = ref('')
const serverError = ref('')
let timer = null

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
const PASSWORD_REGEX = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/

const isEmailValid = computed(() => EMAIL_REGEX.test(email.value))

const canSubmit = computed(() =>
  isEmailValid.value &&
  code.value.trim().length === 6 &&
  PASSWORD_REGEX.test(password.value) &&
  password.value === confirmPassword.value
)

function validateEmail() {
  if (!email.value) {
    emailError.value = '邮箱不能为空'
  } else if (!EMAIL_REGEX.test(email.value)) {
    emailError.value = '邮箱格式不正确'
  } else {
    emailError.value = ''
  }
}

function validatePassword() {
  if (!password.value) {
    passwordError.value = '密码不能为空'
  } else if (password.value.length < 8) {
    passwordError.value = '密码长度必须至少8位'
  } else if (!/(?=.*[A-Za-z])/.test(password.value)) {
    passwordError.value = '密码必须包含字母'
  } else if (!/(?=.*\d)/.test(password.value)) {
    passwordError.value = '密码必须包含数字'
  } else {
    passwordError.value = ''
  }
  if (confirmPassword.value) validateConfirmPassword()
}

function validateConfirmPassword() {
  if (confirmPassword.value && confirmPassword.value !== password.value) {
    confirmPasswordError.value = '两次输入的密码不一致'
  } else {
    confirmPasswordError.value = ''
  }
}

async function sendCode() {
  if (!isEmailValid.value || countdown.value > 0) return
  emailError.value = ''
  try {
    await authApi.sendCode(email.value, 'register')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    serverError.value = e.message || '发送验证码失败'
  }
}

async function register() {
  validateEmail()
  validatePassword()
  validateConfirmPassword()
  if (emailError.value || passwordError.value || confirmPasswordError.value) return

  try {
    serverError.value = ''
    codeError.value = ''
    const data = {
      email: email.value,
      code: code.value,
      password: password.value
    }
    if (nickname.value.trim()) data.nickname = nickname.value.trim()
    await authApi.register(data)
    router.push({ name: 'Login', query: { registered: '1' } })
  } catch (e) {
    const msg = e.message || '注册失败'
    if (msg.includes('验证码')) {
      codeError.value = msg
    } else if (msg.includes('邮箱已注册')) {
      serverError.value = msg
    } else {
      serverError.value = msg
    }
  }
}
</script>
