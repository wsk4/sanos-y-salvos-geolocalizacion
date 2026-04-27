# 1. Imagen Base: Utilizamos un entorno ligero (alpine) que ya tiene Java 21 instalado.
FROM eclipse-temurin:21-jdk-alpine

# 2. Información del mantenedor (opcional, buena práctica)
LABEL maintainer="Equipo Sanos y Salvos"

# 3. Directorio de trabajo: Creamos una carpeta /app dentro del contenedor
WORKDIR /app

# 4. Copiar el ejecutable: Tomamos el archivo .jar generado por Maven y lo metemos al contenedor con el nombre 'app.jar'
COPY target/gestormascotas-0.0.1-SNAPSHOT.jar app.jar

# 5. Puertos: Le indicamos a Docker que este contenedor recibirá tráfico por el puerto 8080
EXPOSE 8081

# 6. Comando de inicio: Lo que ejecutará el contenedor al encenderse
ENTRYPOINT ["java", "-jar", "app.jar"]