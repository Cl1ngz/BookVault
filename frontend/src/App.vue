<script setup lang="ts"></script>

<template>
  <nav>
    <RouterLink to="/books">Books</RouterLink> |
    <RouterLink to="/authors">Authors</RouterLink> |
    <RouterLink to="/readers">Readers</RouterLink> |
    <RouterLink to="/series">Series</RouterLink>
  </nav>
  <RouterView />
</template>
