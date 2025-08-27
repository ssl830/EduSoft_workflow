# 基于 Node 构建前端产物，再用 Nginx 生产环境托管
FROM node:20 AS build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm install --registry=https://registry.npmmirror.com
COPY frontend/ ./
RUN npm run build

FROM nginx:1.25-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
