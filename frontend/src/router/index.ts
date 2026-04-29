import {createRouter, createWebHistory} from 'vue-router'
import BooksView from '@/views/BooksView.vue'
import BookDetailView from '@/views/BookDetailView.vue'
import AuthorsView from '@/views/AuthorsView.vue'
import ReadersView from '@/views/ReadersView.vue'
import SeriesView from '@/views/SeriesView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'


export default createRouter({
    history: createWebHistory(),
    routes: [
        {path: '/', redirect: '/books'},
        {path: '/books', component: BooksView},
        {path: '/books/:id', component: BookDetailView},
        {path: '/authors', component: AuthorsView},
        {path: '/readers', component: ReadersView},
        {path: '/series', component: SeriesView},
        {path: '/login', component: LoginView},
        {path: '/register', component: RegisterView},
    ]
})
