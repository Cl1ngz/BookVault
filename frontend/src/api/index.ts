import axios from 'axios'

const api = axios.create({
    // In Docker: nginx proxies /api/ → backend container
    // In dev: direct to localhost:8080
    baseURL: import.meta.env.VITE_API_URL ?? '/api/v1'
})

export default api
