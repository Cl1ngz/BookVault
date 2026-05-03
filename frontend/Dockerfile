# ETAP 1: Build Vue app
FROM node:22-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

# Copy ALL frontend files (index.html is in root, not src/)
COPY . .
RUN npm run build-only

# ETAP 2: Serve with Nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
