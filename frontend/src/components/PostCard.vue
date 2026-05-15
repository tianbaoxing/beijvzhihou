<template>
  <div class="card hover:border-primary-700/50 transition-all duration-200 cursor-pointer" @click="$router.push(`/post/${post.id}`)">
    <div class="flex items-center gap-2 mb-3">
      <div class="w-8 h-8 rounded-full bg-primary-600/20 flex items-center justify-center text-sm">
        {{ post.nickname?.charAt(0) || '?' }}
      </div>
      <span class="text-dark-300 text-sm">{{ post.nickname || '匿名用户' }}</span>
      <span class="text-dark-600 text-xs ml-auto">{{ timeAgo(post.createdAt) }}</span>
    </div>
    <p class="text-dark-100 leading-relaxed mb-4 line-clamp-3">{{ post.content }}</p>
    <div class="flex items-center gap-4 text-dark-500 text-sm">
      <span class="flex items-center gap-1">
        <span :class="post.liked ? 'text-primary-400' : ''">♥</span> {{ post.likeCount }}
      </span>
      <span>👁 {{ post.viewCount }}</span>
      <span>🤖 {{ post.aiResponseCount }}</span>
    </div>
  </div>
</template>

<script setup>
import { timeAgo } from '../utils/format'

defineProps({
  post: { type: Object, required: true }
})
</script>
