<template>
  <div class="max-w-3xl mx-auto px-4 py-6">
    <div class="card p-6 mb-6">
      <div class="flex items-center gap-4">
        <div class="w-20 h-20 rounded-full bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center text-2xl text-white font-bold">
          {{ user?.nickname?.charAt(0) || '?' }}
        </div>
        <div class="flex-1">
          <h2 class="text-xl font-bold text-dark-100">{{ user?.nickname || '用户' }}</h2>
          <p class="text-dark-500 text-sm">{{ user?.emailMasked }}</p>
          <p class="text-dark-600 text-xs mt-1">注册于 {{ formatDate(user?.createdAt) }}</p>
        </div>
        <button 
          @click="showEditModal = true" 
          class="text-primary-400 hover:text-primary-300 text-sm flex items-center gap-1 transition-colors"
        >
          ✏️ 编辑资料
        </button>
      </div>
    </div>

    <!-- 统计数据 -->
    <div class="grid grid-cols-3 gap-4 mb-6">
      <div class="card text-center p-4">
        <div class="text-2xl font-bold text-primary-400">{{ stats?.postCount || 0 }}</div>
        <div class="text-dark-500 text-sm">发布帖子</div>
      </div>
      <div class="card text-center p-4">
        <div class="text-2xl font-bold text-primary-400">{{ stats?.likeCount || 0 }}</div>
        <div class="text-dark-500 text-sm">获得点赞</div>
      </div>
      <div class="card text-center p-4">
        <div class="text-2xl font-bold text-primary-400">{{ stats?.aiReplyCount || 0 }}</div>
        <div class="text-dark-500 text-sm">AI 安慰</div>
      </div>
    </div>

    <!-- 我的帖子 -->
    <h3 class="text-dark-300 font-medium mb-4 flex items-center gap-2">
      <span>📝</span> 我的帖子
    </h3>
    <div class="space-y-3">
      <div v-for="post in myPosts" :key="post.id" class="card p-4 cursor-pointer hover:bg-dark-700/50 transition-colors" @click="$router.push(`/posts/${post.id}`)">
        <p class="text-dark-200 text-sm mb-2 line-clamp-2">{{ post.content }}</p>
        <div class="flex items-center justify-between text-xs text-dark-500">
          <span>{{ timeAgo(post.createdAt) }}</span>
          <div class="flex items-center gap-3">
            <span>♥ {{ post.likeCount }}</span>
            <span>🤖 {{ post.aiResponseCount }}</span>
          </div>
        </div>
      </div>
      <div v-if="myPosts.length === 0" class="text-center text-dark-500 py-8">
        <div class="text-4xl mb-2">📭</div>
        <p>还没有发布帖子</p>
        <button @click="$router.push('/')" class="text-primary-400 text-sm mt-2 hover:underline">去发帖</button>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <div v-if="showEditModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showEditModal = false">
      <div class="bg-dark-800 rounded-lg p-6 w-full max-w-md mx-4">
        <h3 class="text-lg font-medium text-dark-100 mb-4">编辑资料</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-dark-400 text-sm mb-1">昵称</label>
            <input 
              v-model="editForm.nickname" 
              type="text" 
              class="w-full bg-dark-700 border border-dark-600 rounded-lg px-4 py-2 text-dark-100 focus:border-primary-500 focus:outline-none"
              placeholder="请输入昵称"
            />
          </div>
          <div class="flex gap-2">
            <button 
              @click="showEditModal = false" 
              class="flex-1 bg-dark-700 hover:bg-dark-600 text-dark-300 py-2 rounded-lg transition-colors"
            >
              取消
            </button>
            <button 
              @click="updateProfile" 
              class="flex-1 bg-primary-600 hover:bg-primary-700 text-white py-2 rounded-lg transition-colors"
            >
              保存
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { postApi, authApi } from '../api'
import { timeAgo } from '../utils/format'

const user = ref(null)
const stats = ref(null)
const myPosts = ref([])
const showEditModal = ref(false)
const editForm = reactive({ nickname: '' })

const userStore = useUserStore()
const router = useRouter()

watch(() => userStore.isLoggedIn, (val) => {
  if (!val) router.push('/login?redirect=/profile')
})

async function fetchData() {
  try {
    const [userRes, postsRes] = await Promise.all([
      authApi.getMe(),
      postApi.list(1, 20, 'time')
    ])
    user.value = userRes.data
    editForm.nickname = user.value.nickname || ''
    myPosts.value = postsRes.data?.list?.filter(p => p.userId === user.value.id) || []
    
    // 计算统计数据
    stats.value = {
      postCount: myPosts.value.length,
      likeCount: myPosts.value.reduce((sum, p) => sum + (p.likeCount || 0), 0),
      aiReplyCount: myPosts.value.reduce((sum, p) => sum + (p.aiResponseCount || 0), 0)
    }
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

async function updateProfile() {
  try {
    const res = await authApi.updateProfile({ nickname: editForm.nickname })
    user.value = res.data
    await userStore.fetchUser()
    showEditModal.value = false
  } catch (e) {
    console.error('更新失败', e)
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(fetchData)
</script>