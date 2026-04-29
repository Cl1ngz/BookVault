<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api'

const books = ref([])
const genreFilter = ref('')

async function loadBooks() {
  const params = genreFilter.value ? { genre: genreFilter.value } : {}
  const res = await api.get('/book', { params })
  books.value = res.data
}

onMounted(loadBooks)
</script>

<template>
  <div>
    <h1>Books</h1>
    <input v-model="genreFilter" placeholder="Filter by genre..." @input="loadBooks" />
    <ul>
      <li v-for="book in books" :key="book.idKsiazki">
        <RouterLink :to="`/books/${book.idKsiazki}`">{{ book.tytul }}</RouterLink>
        — {{ book.author?.firstName }} {{ book.author?.lastName }}
        — {{ book.genres?.map((g: any) => g.nazwa).join(', ') }}
        — mood: {{ book.mood }}
      </li>
    </ul>
  </div>
</template>
