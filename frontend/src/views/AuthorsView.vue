<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api'

const authors = ref<any[]>([])

onMounted(async () => {
  const res = await api.get('/authors')
  authors.value = res.data
})
</script>

<template>
  <div>
    <h1>Authors</h1>
    <ul>
      <li v-for="a in authors" :key="a.id" style="margin-bottom:6px;">
        <RouterLink :to="`/authors/${a.id}`">
          <strong>{{ a.firstName }} {{ a.lastName }}</strong>
        </RouterLink>
        <span v-if="a.nationality"> — {{ a.nationality }}</span>
      </li>
    </ul>
  </div>
</template>
