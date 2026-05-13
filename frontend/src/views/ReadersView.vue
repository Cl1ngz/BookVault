<script setup lang="ts">
import {ref, onMounted} from 'vue'
import api from '@/api'

const readers = ref<any[]>([])

onMounted(async () => {
  const res = await api.get('/readers')
  readers.value = res.data
})
</script>

<template>
  <div class="readers-view">
    <h1>Readers</h1>
    <ul>
      <li v-for="r in readers" :key="r.id" class="reader-item">
        {{ r.username }} — {{ r.nationality }}
      </li>
    </ul>
  </div>
</template>

<style scoped>
.readers-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
}

.readers-view h1 { color: #fabd2f; }

.reader-item {
  margin-bottom: 6px;
  color: #d5c4a1;
}
</style>

