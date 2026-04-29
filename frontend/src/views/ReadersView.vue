<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api'

const readers = ref<any[]>([])

onMounted(async () => {
  const res = await api.get('/readers')
  readers.value = res.data
})
</script>

<template>
  <div>
    <h1>Readers</h1>
    <ul>
      <li v-for="r in readers" :key="r.id">
        {{ r.username }} — {{ r.nationality }}
      </li>
    </ul>
  </div>
</template>
