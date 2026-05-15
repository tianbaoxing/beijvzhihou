<template>
  <div class="min-h-screen flex flex-col">
    <NavBar />
    <main class="flex-1">
      <router-view />
    </main>
    <footer class="py-6 text-center text-dark-500 text-sm border-t border-dark-800">
      <p>被拒之后 — 你不是一个人 💪</p>
    </footer>
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
import { useUserStore } from './stores/user'
import { onMounted } from 'vue'

const userStore = useUserStore()

onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchUser()
  }
})
</script>