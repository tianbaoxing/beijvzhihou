<template>
  <div class="max-w-3xl mx-auto px-4 py-6">
    <button @click="$router.back()" class="text-dark-500 hover:text-primary-400 text-sm mb-4 inline-flex items-center gap-1 transition-colors">
      ← 返回
    </button>

    <div v-if="post" class="space-y-6">
      <!-- 帖子内容 -->
      <div class="card">
        <div class="flex items-center gap-2 mb-3">
          <div class="w-10 h-10 rounded-full bg-primary-600/20 flex items-center justify-center">
            {{ post.nickname?.charAt(0) || '?' }}
          </div>
          <div>
            <div class="text-dark-200 font-medium">{{ post.nickname || '匿名用户' }}</div>
            <div class="text-dark-500 text-xs">{{ timeAgo(post.createdAt) }}</div>
          </div>
        </div>
        <p class="text-dark-100 leading-relaxed text-lg mb-4">{{ post.content }}</p>
        <div class="flex items-center gap-4 text-sm">
          <button @click="toggleLike" class="flex items-center gap-1 transition-colors" :class="post.liked ? 'text-primary-400' : 'text-dark-500 hover:text-primary-400'">
            ♥ {{ post.likeCount }}
          </button>
          <span class="text-dark-500">👁 {{ post.viewCount }}</span>
        </div>
      </div>

      <!-- AI 回复区域 - 醒目设计 -->
      <div class="ai-replies-section">
        <div class="ai-replies-header">
          <div class="flex items-center gap-2">
            <span class="ai-icon-pulse">🤖</span>
            <h3 class="text-lg font-bold bg-gradient-to-r from-primary-400 to-blue-400 bg-clip-text text-transparent">
              AI 安慰
            </h3>
            <span v-if="post.aiReplies?.length" class="text-dark-600 text-xs ml-1">{{ post.aiReplies.length }} 条回复</span>
          </div>
        </div>

        <!-- AI 已回复 -->
        <div v-if="post.aiReplies?.length" class="space-y-3 mt-4">
          <div v-for="reply in post.aiReplies" :key="reply.id" class="ai-reply-card">
            <AiReplyCard :reply="reply" />
          </div>
        </div>

        <!-- AI 正在回复 - 加载动画 -->
        <div v-if="aiLoading" class="ai-loading-card mt-4">
          <div class="flex items-center gap-3">
            <div class="ai-loading-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <span class="text-primary-400 text-sm font-medium animate-pulse">AI 正在赶来安慰你…</span>
          </div>
        </div>

        <!-- 无 AI 回复且不在加载中 -->
        <div v-if="!post.aiReplies?.length && !aiLoading" class="ai-empty-hint mt-4">
          <p class="text-dark-500 text-sm text-center py-4">AI 还没有回复，稍后再来看看 💫</p>
        </div>
      </div>

      <!-- 用户评论区 -->
      <div class="comment-section">
        <div class="comment-section-header">
          <div class="flex items-center gap-2">
            <span>💬</span>
            <h3 class="text-lg font-bold text-dark-100">用户评论</h3>
            <span v-if="comments.length" class="text-dark-600 text-xs ml-1">{{ commentTotal }} 条</span>
          </div>
        </div>

        <!-- 评论输入框（仅登录用户可见） -->
        <div v-if="userStore.isLoggedIn" class="comment-input-area mt-4">
          <div class="flex gap-2">
            <textarea
              v-model="commentContent"
              placeholder="说点什么安慰 TA..."
              maxlength="200"
              rows="2"
              class="flex-1 bg-dark-800 border border-dark-600 rounded-lg px-3 py-2 text-dark-100 text-sm placeholder-dark-500 focus:outline-none focus:border-primary-500 resize-none"
            ></textarea>
            <button
              @click="submitComment"
              :disabled="!commentContent.trim() || commentSubmitting"
              class="px-4 py-2 bg-primary-600 text-white text-sm rounded-lg hover:bg-primary-500 disabled:opacity-40 disabled:cursor-not-allowed transition-colors self-end"
            >
              {{ commentSubmitting ? '发送中...' : '发送' }}
            </button>
          </div>
          <div class="text-right text-dark-600 text-xs mt-1">{{ commentContent.length }}/200</div>
        </div>

        <!-- 未登录提示 -->
        <div v-else class="mt-4 text-center py-3 text-dark-500 text-sm">
          <router-link :to="`/login?redirect=/post/${route.params.id}`" class="text-primary-400 hover:underline">登录</router-link>
          后可以评论
        </div>

        <!-- 评论列表 -->
        <div v-if="comments.length" class="mt-4 space-y-4">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="flex items-start gap-2">
              <div class="w-8 h-8 rounded-full bg-dark-700 flex items-center justify-center text-dark-400 text-xs flex-shrink-0">
                {{ comment.nickname?.charAt(0) || '?' }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2">
                  <span class="text-dark-200 text-sm font-medium">{{ comment.nickname || '匿名用户' }}</span>
                  <span class="text-dark-600 text-xs">{{ timeAgo(comment.createdAt) }}</span>
                </div>
                <p class="text-dark-300 text-sm mt-1 break-words">{{ comment.content }}</p>

                <!-- 回复按钮 -->
                <button
                  v-if="userStore.isLoggedIn"
                  @click="replyTo = replyTo === comment.id ? null : comment.id"
                  class="text-dark-500 hover:text-primary-400 text-xs mt-1 transition-colors"
                >
                  回复
                </button>

                <!-- 回复输入框 -->
                <div v-if="replyTo === comment.id" class="mt-2 flex gap-2">
                  <input
                    v-model="replyContent"
                    placeholder="回复..."
                    maxlength="200"
                    class="flex-1 bg-dark-800 border border-dark-600 rounded px-3 py-1.5 text-dark-100 text-sm placeholder-dark-500 focus:outline-none focus:border-primary-500"
                    @keyup.enter="submitReply(comment.id)"
                  />
                  <button
                    @click="submitReply(comment.id)"
                    :disabled="!replyContent.trim() || commentSubmitting"
                    class="px-3 py-1.5 bg-primary-600 text-white text-xs rounded hover:bg-primary-500 disabled:opacity-40 transition-colors"
                  >
                    回复
                  </button>
                </div>

                <!-- 子评论 -->
                <div v-if="comment.replies?.length" class="mt-2 ml-2 pl-3 border-l border-dark-700 space-y-3">
                  <div v-for="reply in comment.replies" :key="reply.id" class="flex items-start gap-2">
                    <div class="w-6 h-6 rounded-full bg-dark-700 flex items-center justify-center text-dark-500 text-xs flex-shrink-0">
                      {{ reply.nickname?.charAt(0) || '?' }}
                    </div>
                    <div>
                      <div class="flex items-center gap-2">
                        <span class="text-dark-300 text-xs font-medium">{{ reply.nickname || '匿名用户' }}</span>
                        <span class="text-dark-600 text-xs">{{ timeAgo(reply.createdAt) }}</span>
                      </div>
                      <p class="text-dark-400 text-xs mt-0.5 break-words">{{ reply.content }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 无评论 -->
        <div v-else class="mt-4 text-center text-dark-500 text-sm py-4">
          还没有评论，来做第一个安慰 TA 的人吧 💛
        </div>
      </div>
    </div>

    <div v-else class="text-center text-dark-500 py-12">加载中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { postApi, commentApi, getBrowserFingerprint } from '../api'
import { useUserStore } from '../stores/user'
import { timeAgo } from '../utils/format'
import AiReplyCard from '../components/AiReplyCard.vue'

const route = useRoute()
const userStore = useUserStore()
const post = ref(null)
const aiLoading = ref(false)
const comments = ref([])
const commentTotal = ref(0)
const commentContent = ref('')
const replyContent = ref('')
const replyTo = ref(null)
const commentSubmitting = ref(false)
let pollTimer = null

async function fetchPost() {
  try {
    const fp = getBrowserFingerprint()
    const res = await postApi.detail(route.params.id, fp)
    post.value = res.data
  } catch (e) {
    console.error('加载帖子失败', e)
  }
}

async function fetchComments() {
  try {
    const res = await commentApi.list(route.params.id)
    comments.value = res.data.list || []
    commentTotal.value = res.data.total || 0
  } catch (e) {
    console.error('加载评论失败', e)
  }
}

async function toggleLike() {
  try {
    const fp = getBrowserFingerprint()
    await postApi.toggleLike(post.value.id, fp)
    fetchPost()
  } catch (e) {
    console.error('点赞失败', e)
  }
}

async function submitComment() {
  if (!commentContent.value.trim() || commentSubmitting.value) return
  commentSubmitting.value = true
  try {
    await commentApi.create(route.params.id, {
      content: commentContent.value.trim(),
      parentId: null
    })
    commentContent.value = ''
    await fetchComments()
    await fetchPost()
  } catch (e) {
    console.error('评论失败', e)
  } finally {
    commentSubmitting.value = false
  }
}

async function submitReply(parentId) {
  if (!replyContent.value.trim() || commentSubmitting.value) return
  commentSubmitting.value = true
  try {
    await commentApi.create(route.params.id, {
      content: replyContent.value.trim(),
      parentId: parentId
    })
    replyContent.value = ''
    replyTo.value = null
    await fetchComments()
    await fetchPost()
  } catch (e) {
    console.error('回复失败', e)
  } finally {
    commentSubmitting.value = false
  }
}

function startPolling() {
  if (!post.value) return
  const hasNoReplies = !post.value.aiReplies || post.value.aiReplies.length === 0
  if (hasNoReplies) {
    aiLoading.value = true
    pollTimer = setInterval(async () => {
      await fetchPost()
      if (post.value?.aiReplies?.length > 0) {
        aiLoading.value = false
        clearInterval(pollTimer)
        pollTimer = null
      }
    }, 3000)
    setTimeout(() => {
      if (pollTimer) {
        aiLoading.value = false
        clearInterval(pollTimer)
        pollTimer = null
      }
    }, 60000)
  }
}

onMounted(async () => {
  await fetchPost()
  await fetchComments()
  startPolling()
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})
</script>

<style scoped>
.ai-replies-section {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(59, 130, 246, 0.08) 100%);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 1rem;
  padding: 1.25rem;
}

.ai-replies-header {
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(99, 102, 241, 0.15);
}

.ai-icon-pulse {
  animation: iconPulse 2s ease-in-out infinite;
  font-size: 1.25rem;
}

@keyframes iconPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.ai-reply-card {
  animation: replySlideIn 0.5s ease-out;
}

@keyframes replySlideIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-loading-card {
  background: rgba(99, 102, 241, 0.06);
  border: 1px dashed rgba(99, 102, 241, 0.3);
  border-radius: 0.75rem;
  padding: 1.25rem;
}

.ai-loading-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.ai-loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #818cf8;
  animation: dotBounce 1.4s ease-in-out infinite;
}

.ai-loading-dots span:nth-child(1) { animation-delay: 0s; }
.ai-loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.ai-loading-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.ai-empty-hint {
  background: rgba(99, 102, 241, 0.04);
  border-radius: 0.75rem;
}

.comment-section {
  background: rgba(30, 30, 40, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 1rem;
  padding: 1.25rem;
}

.comment-section-header {
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.comment-input-area textarea {
  transition: border-color 0.2s;
}

.comment-item {
  animation: replySlideIn 0.3s ease-out;
}
</style>
