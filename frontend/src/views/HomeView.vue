<template>
  <div class="max-w-3xl mx-auto px-4 py-6">
    <!-- 顶部标语 -->
    <div class="text-center mb-8">
      <h1 class="text-3xl font-bold text-dark-100 mb-2">被拒之后</h1>
      <p class="text-dark-400">你不是一个人。每一次拒绝，都是离对的机会更近一步。</p>
    </div>

    <!-- 发帖区域 -->
    <div v-if="userStore.isLoggedIn" class="card mb-6">
      <textarea
        v-model="newPost"
        class="input-field resize-none mb-3"
        rows="3"
        placeholder="说说你的经历，AI会来安慰你..."
        maxlength="500"
      ></textarea>
      <div class="flex items-center justify-between">
        <span class="text-dark-500 text-xs">{{ newPost.length }}/500</span>
        <button @click="submitPost" :disabled="!newPost.trim() || submitting" class="btn-primary text-sm">
          {{ submitting ? '发布中...' : '发布' }}
        </button>
      </div>
    </div>
    <div v-else class="card mb-6 text-center cursor-pointer hover:border-primary-400/50 transition-colors" @click="goLogin">
      <p class="text-dark-400 mb-3">登录后可以发帖，获得AI的安慰和鼓励</p>
      <span class="btn-primary text-sm inline-block">去登录</span>
    </div>

    <!-- 排序切换 -->
    <div class="flex items-center gap-3 mb-4">
      <button
        @click="sort = 'time'"
        :class="sort === 'time' ? 'text-primary-400' : 'text-dark-500'"
        class="text-sm hover:text-primary-400 transition-colors"
      >最新</button>
      <button
        @click="sort = 'hot'"
        :class="sort === 'hot' ? 'text-primary-400' : 'text-dark-500'"
        class="text-sm hover:text-primary-400 transition-colors"
      >最热</button>
    </div>

    <!-- 帖子列表 -->
    <div class="space-y-4">
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
      <div v-if="loading" class="text-center text-dark-500 py-8">加载中...</div>
      <div v-if="!loading && posts.length === 0" class="text-center text-dark-500 py-8">还没有帖子，来做第一个分享的人吧 ✨</div>
      <div v-if="hasMore && !loading" class="text-center">
        <button @click="loadMore" class="btn-secondary text-sm">加载更多</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { postApi } from '../api'
import { useUserStore } from '../stores/user'
import PostCard from '../components/PostCard.vue'

const router = useRouter()
const userStore = useUserStore()
const posts = ref([])
const page = ref(1)
const sort = ref('time')
const loading = ref(false)
const hasMore = ref(true)
const newPost = ref('')
const submitting = ref(false)

async function fetchPosts() {
  loading.value = true
  try {
    const res = await postApi.list(page.value, 20, sort.value)
    const list = res.data?.list || []
    if (page.value === 1) {
      posts.value = list
    } else {
      posts.value.push(...list)
    }
    hasMore.value = list.length >= 20
  } catch (e) {
    console.error('加载帖子失败', e)
  } finally {
    loading.value = false
  }
}

function loadMore() {
  page.value++
  fetchPosts()
}

function goLogin() {
  router.push({ name: 'Login', query: { redirect: '/post/new' } })
}

async function submitPost() {
  if (!newPost.value.trim() || submitting.value) return
  submitting.value = true
  try {
    const res = await postApi.create({ content: newPost.value.trim() })
    const postId = res.data?.id
    newPost.value = ''
    if (postId) {
      router.push(`/post/${postId}`)
    } else {
      page.value = 1
      fetchPosts()
    }
  } catch (e) {
    console.error('发布失败', e)
  } finally {
    submitting.value = false
  }
}

watch(sort, () => {
  page.value = 1
  fetchPosts()
})

onMounted(() => {
  fetchPosts()
  if (userStore.isLoggedIn) {
    userStore.fetchUser()
  }
})
</script>
