# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar arquivos do Maven
COPY pom.xml .

# Baixar dependências primeiro (para cache)
RUN mvn dependency:go-offline -B || true

# Copiar código fonte
COPY src ./src

# Build da aplicação com mais output
RUN mvn clean package -DskipTests -e || (echo "Build failed. Checking for errors..." && find target -name "*.log" -exec cat {} \; && exit 1)

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar JAR da stage de build
COPY --from=build /app/target/*.jar app.jar

# Expor porta
EXPOSE 8080

# Variáveis de ambiente (serão sobrescritas pelo docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

# Executar aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

