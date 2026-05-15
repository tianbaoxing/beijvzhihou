<template>
  <div class="bg-dark-800/50 border border-dark-700/50 rounded-lg p-4 hover:border-dark-600/50 transition-colors">
    <div class="flex items-center gap-2 mb-3">
      <span class="text-lg">{{ getAiIcon(reply.aiProvider) }}</span>
      <span class="text-primary-400 font-medium text-sm">{{ getAiName(reply.aiProvider) }}</span>
      <span class="px-2 py-0.5 rounded-full text-xs" :class="getPerspectiveClass(reply.perspective)">
        {{ reply.perspective }}视角
      </span>
      <div class="ml-auto flex items-center gap-2">
        <span class="text-dark-600 text-xs">情绪分</span>
        <div class="flex items-center gap-1">
          <div 
            v-for="i in 10" 
            :key="i" 
            class="w-2 h-2 rounded-full transition-colors"
            :class="i <= Math.round(Number(reply.emotionScore)) ? getEmotionColor(reply.emotionScore) : 'bg-dark-700'"
          ></div>
        </div>
        <span class="text-dark-400 text-xs font-medium">{{ reply.emotionScore }}</span>
      </div>
    </div>
    <p class="text-dark-200 text-sm leading-relaxed whitespace-pre-wrap">{{ reply.content }}</p>
    <div v-if="reply.content.startsWith('【模拟】') || reply.content.startsWith('【降级】')" class="mt-2 flex items-center gap-1">
      <span class="text-yellow-500 text-xs">⚠️</span>
      <span class="text-dark-600 text-xs">当前为模拟回复，配置 API Key 后可获取真实 AI 回复</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  reply: { type: Object, required: true }
})

function getAiIcon(provider) {
  const icons = {
    'DeepSeek': '💙',
    'Kimi2': '🧠',
    'Qwen': '🌟'
  }
  return icons[provider] || '🤖'
}

function getAiName(provider) {
  const names = {
    'DeepSeek': 'DeepSeek',
    'Kimi2': 'Kimi',
    'Qwen': 'Qwen'
  }
  return names[provider] || provider
}

function getPerspectiveClass(perspective) {
  const classes = {
    '心情': 'bg-blue-600/20 text-blue-400',
    '历史': 'bg-purple-600/20 text-purple-400',
    '技术': 'bg-green-600/20 text-green-400'
  }
  return classes[perspective] || 'bg-gray-600/20 text-gray-400'
}

function getEmotionColor(score) {
  const numScore = Number(score)
  if (numScore >= 7) return 'bg-red-500'
  if (numScore >= 5) return 'bg-yellow-500'
  return 'bg-green-500'
}
</script>