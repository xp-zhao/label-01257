# 构建阶段 - 使用支持多平台的镜像
FROM node:20-alpine AS builder

WORKDIR /app

# 复制 package.json 并安装依赖
COPY package.json package-lock.json* ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码并构建
COPY . .
RUN npm run build

# 运行阶段 - 使用支持 amd64 和 arm64 的 nginx 镜像
FROM nginx:1.25-alpine

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
