<script setup lang="ts">
import {ref, onMounted} from 'vue'
import api from '@/api'


const series = ref<any[]>([])

onMounted(async () => {
  const res = await api.get('/series')
  series.value = res.data
})
</script>

<template>
  <div class="series-view">
    <h1>Series</h1>
    <ul>
      <li v-for="s in series" :key="s.id" class="list-item-spaced">
        <RouterLink :to="`/series/${s.id}`"><strong>{{ s.name }}</strong></RouterLink>
        — {{ s.volumeCount }} volumes
        <em>({{ s.author?.firstName }} {{ s.author?.lastName }})</em>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.series-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
}

.series-view h1 { color: #fabd2f; }

.list-item-spaced {
  margin-bottom: 6px;
  color: #d5c4a1;
}
</style>

