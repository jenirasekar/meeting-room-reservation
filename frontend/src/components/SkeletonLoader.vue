<script setup>
defineProps({
  variant: {
    type: String,
    default: 'text',
    validator: v => ['text', 'card', 'stats', 'list-item', 'avatar', 'title', 'paragraph'].includes(v),
  },
  count: { type: Number, default: 1 },
})
</script>

<template>
  <template v-if="variant === 'card'">
    <div v-for="i in count" :key="i" class="card space-y-4">
      <div class="skeleton h-48 w-full rounded-lg" />
      <div class="space-y-3">
        <div class="flex justify-between">
          <div class="skeleton h-6 w-2/3 rounded" />
          <div class="skeleton h-5 w-16 rounded-full" />
        </div>
        <div class="flex gap-4">
          <div class="skeleton h-4 w-24 rounded" />
          <div class="skeleton h-4 w-20 rounded" />
        </div>
        <div class="skeleton h-4 w-full rounded" />
        <div class="skeleton h-4 w-3/4 rounded" />
      </div>
    </div>
  </template>

  <template v-else-if="variant === 'stats'">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <div v-for="i in count" :key="i" class="card text-center space-y-3">
        <div class="skeleton h-10 w-16 rounded mx-auto" />
        <div class="skeleton h-4 w-24 rounded mx-auto" />
      </div>
    </div>
  </template>

  <template v-else-if="variant === 'list-item'">
    <div v-for="i in count" :key="i" class="card space-y-3">
      <div class="flex items-center gap-3">
        <div class="skeleton h-5 w-20 rounded-full" />
        <div class="skeleton h-6 w-1/2 rounded" />
      </div>
      <div class="grid grid-cols-3 gap-3">
        <div class="skeleton h-4 w-full rounded" />
        <div class="skeleton h-4 w-full rounded" />
        <div class="skeleton h-4 w-full rounded" />
      </div>
    </div>
  </template>

  <template v-else-if="variant === 'avatar'">
    <div class="skeleton w-10 h-10 rounded-full" />
  </template>

  <template v-else-if="variant === 'title'">
    <div class="skeleton h-8 w-64 rounded" />
    <div v-if="count > 1" class="skeleton h-4 w-48 rounded mt-2" />
  </template>

  <template v-else-if="variant === 'paragraph'">
    <div v-for="i in count" :key="i" class="skeleton h-4 rounded" :class="i === count ? 'w-2/3' : 'w-full mb-2'" />
  </template>

  <template v-else>
    <!-- default: text -->
    <div v-for="i in count" :key="i" class="skeleton h-4 w-full rounded mb-2" :style="{ width: (100 - (i - 1) * 15) + '%' }" />
  </template>
</template>
