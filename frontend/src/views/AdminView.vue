<template>
  <div class="max-w-4xl mx-auto px-4 py-6">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-dark-100">管理后台</h2>
      <button @click="refreshData" class="text-dark-400 hover:text-primary-400 text-sm flex items-center gap-1 transition-colors">
        🔄 刷新
      </button>
    </div>

    <div class="grid grid-cols-4 gap-4 mb-6">
      <div class="card p-4 cursor-pointer hover:ring-1 hover:ring-primary-500 transition-all" @click="switchTab('posts')">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-primary-600/20 flex items-center justify-center">📊</div>
          <div>
            <div class="text-2xl font-bold text-primary-400">{{ stats?.totalPosts || 0 }}</div>
            <div class="text-dark-500 text-xs">总帖子数</div>
          </div>
        </div>
      </div>
      <div class="card p-4 cursor-pointer hover:ring-1 hover:ring-blue-500 transition-all" @click="switchTab('users')">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-blue-600/20 flex items-center justify-center">👥</div>
          <div>
            <div class="text-2xl font-bold text-blue-400">{{ stats?.totalUsers || 0 }}</div>
            <div class="text-dark-500 text-xs">总用户数</div>
          </div>
        </div>
      </div>
      <div class="card p-4 cursor-pointer hover:ring-1 hover:ring-green-500 transition-all" @click="switchTab('posts')">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-green-600/20 flex items-center justify-center">📝</div>
          <div>
            <div class="text-2xl font-bold text-green-400">{{ stats?.todayPosts || 0 }}</div>
            <div class="text-dark-500 text-xs">今日新增</div>
          </div>
        </div>
      </div>
      <div class="card p-4 cursor-pointer hover:ring-1 hover:ring-yellow-500 transition-all" @click="switchTab('pending')">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-yellow-600/20 flex items-center justify-center">⏳</div>
          <div>
            <div class="text-2xl font-bold text-yellow-400">{{ pendingPosts.length }}</div>
            <div class="text-dark-500 text-xs">待审核</div>
          </div>
        </div>
      </div>
    </div>

    <div class="flex gap-2 mb-4">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        @click="activeTab = tab.key"
        class="px-4 py-2 rounded-lg text-sm transition-colors"
        :class="activeTab === tab.key ? 'bg-primary-600 text-white' : 'bg-dark-800 text-dark-400 hover:bg-dark-700'"
      >
        {{ tab.label }}
        <span v-if="tab.badge" class="ml-1 px-1.5 py-0.5 bg-red-600 rounded-full text-xs">{{ tab.badge }}</span>
      </button>
    </div>

    <div v-if="activeTab === 'pending'" class="space-y-4">
      <div v-for="post in pendingPosts" :key="post.id" class="card">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <span class="text-dark-500 text-xs">{{ timeAgo(post.createdAt) }}</span>
            </div>
            <p class="text-dark-100 mb-3">{{ post.content }}</p>
          </div>
          <div class="flex flex-col gap-2">
            <button @click="review(post.id, 'pass')" class="bg-green-600 hover:bg-green-700 text-white text-sm py-2 px-4 rounded-lg transition-colors whitespace-nowrap">
              ✅ 通过
            </button>
            <button @click="review(post.id, 'reject')" class="bg-red-600 hover:bg-red-700 text-white text-sm py-2 px-4 rounded-lg transition-colors whitespace-nowrap">
              ❌ 拒绝
            </button>
          </div>
        </div>
      </div>
      <div v-if="pendingPosts.length === 0" class="card text-center py-12">
        <div class="text-4xl mb-3">🎉</div>
        <p class="text-dark-400">没有待审核内容</p>
      </div>
    </div>

    <div v-if="activeTab === 'posts'" class="space-y-4">
      <div v-for="post in allPosts" :key="post.id" class="card">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <span 
                class="px-2 py-0.5 rounded-full text-xs"
                :class="postStatusClass(post.status)"
              >{{ postStatusText(post.status) }}</span>
              <span class="text-dark-500 text-xs">{{ timeAgo(post.createdAt) }}</span>
            </div>
            <p class="text-dark-100 mb-3 line-clamp-2">{{ post.content }}</p>
            <div class="flex items-center gap-4 text-xs text-dark-500">
              <span>♥ {{ post.likeCount }}</span>
              <span>👁 {{ post.viewCount }}</span>
              <span>🤖 {{ post.aiResponseCount }}</span>
              <span>💬 {{ post.commentCount }}</span>
            </div>
          </div>
          <button @click="viewPost(post.id)" class="text-primary-400 hover:text-primary-300 text-sm whitespace-nowrap">查看 →</button>
        </div>
      </div>
      <div v-if="allPosts.length === 0" class="card text-center py-12">
        <div class="text-4xl mb-3">📭</div>
        <p class="text-dark-400">暂无帖子</p>
      </div>
      <div v-if="postsPage < postsTotalPage" class="text-center py-4">
        <button @click="loadMorePosts" class="px-6 py-2 bg-dark-800 hover:bg-dark-700 text-dark-300 rounded-lg text-sm transition-colors">
          加载更多
        </button>
      </div>
    </div>

    <div v-if="activeTab === 'users'" class="space-y-4">
      <div v-for="user in users" :key="user.id" class="card flex items-center justify-between p-4">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-primary-600/20 flex items-center justify-center text-dark-300 font-medium">
            {{ user.nickname?.charAt(0) || '?' }}
          </div>
          <div>
            <div class="text-dark-100 font-medium">{{ user.nickname }}</div>
            <div class="text-dark-500 text-xs">{{ user.emailMasked }}</div>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-dark-500 text-xs">{{ timeAgo(user.createdAt) }}</span>
          <span 
            class="px-2 py-0.5 rounded-full text-xs"
            :class="user.role === 'ADMIN' ? 'bg-purple-600/20 text-purple-400' : 'bg-dark-700 text-dark-400'"
          >
            {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </span>
        </div>
      </div>
      <div v-if="users.length === 0" class="card text-center py-12">
        <div class="text-4xl mb-3">👥</div>
        <p class="text-dark-400">暂无用户</p>
      </div>
      <div v-if="usersPage < usersTotalPage" class="text-center py-4">
        <button @click="loadMoreUsers" class="px-6 py-2 bg-dark-800 hover:bg-dark-700 text-dark-300 rounded-lg text-sm transition-colors">
          加载更多
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { adminApi } from '../api'
import { timeAgo } from '../utils/format'

const pendingPosts = ref([])
const allPosts = ref([])
const users = ref([])
const stats = ref(null)
const activeTab = ref('pending')
const postsPage = ref(1)
const postsTotalPage = ref(1)
const usersPage = ref(1)
const usersTotalPage = ref(1)

const tabs = computed(() => [
  { key: 'pending', label: '待审核', badge: pendingPosts.value.length > 0 ? pendingPosts.value.length : null },
  { key: 'posts', label: '帖子列表', badge: null },
  { key: 'users', label: '用户管理', badge: null }
])

function switchTab(tab) {
  activeTab.value = tab
}

function postStatusClass(status) {
  switch (status) {
    case 1: return 'bg-green-600/20 text-green-400'
    case 0: return 'bg-yellow-600/20 text-yellow-400'
    case -1: return 'bg-red-600/20 text-red-400'
    default: return 'bg-dark-700 text-dark-400'
  }
}

function postStatusText(status) {
  switch (status) {
    case 1: return '已通过'
    case 0: return '待审核'
    case -1: return '已拒绝'
    default: return '未知'
  }
}

async function fetchData() {
  try {
    const [pendingRes, statsRes] = await Promise.all([
      adminApi.pendingPosts(),
      adminApi.stats()
    ])
    pendingPosts.value = pendingRes.data || []
    stats.value = statsRes.data
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

async function fetchPosts() {
  try {
    const res = await adminApi.listPosts(postsPage.value, 20)
    const pageData = res.data
    if (postsPage.value === 1) {
      allPosts.value = pageData.records || []
    } else {
      allPosts.value = [...allPosts.value, ...(pageData.records || [])]
    }
    postsTotalPage.value = pageData.pages || 1
  } catch (e) {
    console.error('加载帖子失败', e)
  }
}

async function fetchUsers() {
  try {
    const res = await adminApi.listUsers(usersPage.value, 20)
    const pageData = res.data
    if (usersPage.value === 1) {
      users.value = pageData.records || []
    } else {
      users.value = [...users.value, ...(pageData.records || [])]
    }
    usersTotalPage.value = pageData.pages || 1
  } catch (e) {
    console.error('加载用户失败', e)
  }
}

function loadMorePosts() {
  postsPage.value++
  fetchPosts()
}

function loadMoreUsers() {
  usersPage.value++
  fetchUsers()
}

async function review(id, result) {
  try {
    await adminApi.reviewPost(id, result)
    pendingPosts.value = pendingPosts.value.filter(p => p.id !== id)
    if (activeTab.value === 'posts') {
      postsPage.value = 1
      fetchPosts()
    }
  } catch (e) {
    alert(e.message || '审核操作失败')
  }
}

function viewPost(id) {
  window.open(`/post/${id}`, '_blank')
}

function refreshData() {
  fetchData()
  if (activeTab.value === 'posts') {
    postsPage.value = 1
    fetchPosts()
  } else if (activeTab.value === 'users') {
    usersPage.value = 1
    fetchUsers()
  }
}

onMounted(() => {
  fetchData()
  fetchPosts()
  fetchUsers()
})
</script>
