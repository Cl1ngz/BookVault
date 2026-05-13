<script setup lang="ts">
import {ref, onMounted} from 'vue'
import api from '@/api'


const authors = ref<any[]>([])

onMounted(async () => {
  const res = await api.get('/authors')
  authors.value = res.data
})
</script>

<template>
  <div class="authors-view">
    <h1>Authors</h1>
    <ul>
      <li v-for="a in authors" :key="a.id" class="list-item-spaced">
        <RouterLink :to="`/authors/${a.id}`">
          <strong>{{ a.firstName }} {{ a.lastName }}</strong>
        </RouterLink>
        <span v-if="a.nationality"> — {{ a.nationality }}</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.authors-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
}

.authors-view h1 { color: #fabd2f; }

.list-item-spaced {
  margin-bottom: 6px;
  color: #d5c4a1;
}
</style>

