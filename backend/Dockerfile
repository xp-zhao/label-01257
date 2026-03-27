# 构建阶段 - 使用支持多平台的镜像
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段 - 使用支持 amd64 和 arm64 的镜像
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 设置时区和编码
ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_OPTS="-Dfile.encoding=UTF-8"

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 从构建阶段复制 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar -Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom app.jar"]
