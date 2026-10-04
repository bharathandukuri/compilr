# ------------------------------------------------------------------------------
# Build Stage: Compile and bundle React frontend with Vite
# ------------------------------------------------------------------------------
FROM node:22-alpine AS builder

WORKDIR /app

# Copy package descriptors first to leverage caching
COPY package.json package-lock.json ./
RUN npm ci

# Copy source and build static bundle
COPY . .
RUN npm run build

# ------------------------------------------------------------------------------
# Production Stage: Serve static assets with Nginx
# ------------------------------------------------------------------------------
FROM nginx:1.27-alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy build artifacts from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
