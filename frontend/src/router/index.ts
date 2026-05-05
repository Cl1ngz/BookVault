import {createRouter, createWebHistory} from 'vue-router'
import BooksView from '@/views/BooksView.vue'
import BookDetailView from '@/views/BookDetailView.vue'
import AuthorsView from '@/views/AuthorsView.vue'
import AuthorDetailView from '@/views/AuthorDetailView.vue'
import ReadersView from '@/views/ReadersView.vue'
import SeriesView from '@/views/SeriesView.vue'
import SeriesDetailView from '@/views/SeriesDetailView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import ModeratorView from '@/views/ModeratorView.vue'
import MyShelfView from '@/views/MyShelfView.vue'
import JournalView from '@/views/JournalView.vue'
import ReadingDashboardView from '@/views/ReadingDashboardView.vue'

const requireAuth = () => {
    const user = JSON.parse(localStorage.getItem('user') || 'null')
    if (!user?.token) return '/login'
}

export default createRouter({
    history: createWebHistory(),
    routes: [
        {path: '/', redirect: '/books'},
        {path: '/books', component: BooksView},
        {path: '/books/:id', component: BookDetailView},
        {path: '/authors', component: AuthorsView},
        {path: '/authors/:id', component: AuthorDetailView},
        {path: '/readers', component: ReadersView},
        {path: '/series', component: SeriesView},
        {path: '/series/:id', component: SeriesDetailView},
        {path: '/login', component: LoginView},
        {path: '/register', component: RegisterView},
        {
            path: '/moderator',
            component: ModeratorView,
            beforeEnter: () => {
                const user = JSON.parse(localStorage.getItem('user') || 'null')
                if (!user || user.role !== 'MODERATOR') return '/books'
            }
        },
        {path: '/my-shelf', component: MyShelfView, beforeEnter: requireAuth},
        {path: '/journal', component: JournalView, beforeEnter: requireAuth},
        {path: '/dashboard', component: ReadingDashboardView, beforeEnter: requireAuth},
    ]
})
