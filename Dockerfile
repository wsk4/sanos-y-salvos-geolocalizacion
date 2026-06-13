# ==========================================
# Fase 1: Build (Compilación)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests

# ==========================================
# Fase 2: Runtime (Producción)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

# Crear usuario seguro del sistema para evitar ejecuciones vectorizadas como root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Optimización de recolección de basura y límites estrictos de consumo de memoria RAM en Cloud
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]