# ==========================================
# Fase 1: Build (Mantenemos la compilación)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Fase 2: Runtime (Optimizado para Producción)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

# Principio de Menor Privilegio: Crear usuario del sistema sin shell
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copiar el artefacto desde la fase de build
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Parámetros de optimización de memoria para contenedores en Java 21
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]