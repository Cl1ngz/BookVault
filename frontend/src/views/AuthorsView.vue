<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api'

const authors = ref<any[]>([])

onMounted(async () => {
  const res = await api.get('/author')
  authors.value = res.data
})
</script>

<template>
  <div>
    <h1>Authors</h1>
    <ul>
      <li v-for="a in authors" :key="a.id">
        {{ a.firstName }} {{ a.lastName }} — {{ a.nationality }}
      </li>
    </ul>
  </div>
</template>
