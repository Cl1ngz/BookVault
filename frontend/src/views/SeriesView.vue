<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api'

const series = ref<any[]>([])

onMounted(async () => {
  const res = await api.get('/series')
  series.value = res.data
})
</script>

<template>
  <div>
    <h1>Series</h1>
    <ul>
      <li v-for="s in series" :key="s.id" style="margin-bottom:6px;">
        <RouterLink :to="`/series/${s.id}`"><strong>{{ s.name }}</strong></RouterLink>
        — {{ s.volumeCount }} volumes
        <em>({{ s.author?.firstName }} {{ s.author?.lastName }})</em>
      </li>
    </ul>
  </div>
</template>
