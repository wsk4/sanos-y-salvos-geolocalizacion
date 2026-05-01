# Etapa 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila el proyecto saltando los tests para agilizar el proceso
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Copia el .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto (cada contenedor sobrescribirá esto según su config)
EXPOSE 8081

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]