# 🐾 Sanos y Salvos — Microservicio: Geolocalización

Este microservicio es el componente espacial de la plataforma **Sanos y Salvos**, encargado de registrar y gestionar las ubicaciones geográficas de las mascotas reportadas. Convierte direcciones textuales en coordenadas reales a través de la API de **LocationIQ** y las persiste como geometrías espaciales usando **PostGIS**, permitiendo búsquedas y análisis de proximidad en tiempo real.

***

## 🚀 Tecnologías y Herramientas

- **Lenguaje:** Java 21 (JDK 21)
- **Framework:** Spring Boot 3.5.14
- **Persistencia:** Spring Data JPA con Hibernate Spatial
- **Base de Datos:** PostgreSQL + **PostGIS** (extensión espacial para tipos `geometry`)
- **Geocodificación:** LocationIQ API v1 (conversión de dirección → coordenadas WGS-84)
- **Gestión de Dependencias:** Maven (wrapper `./mvnw` incluido)
- **Contenerización:** Docker (imagen multi-stage)
- **Librerías Extra:**
  - **Lombok:** Para un código limpio y sin boilerplate
  - **Validation:** Para asegurar la integridad de los datos de entrada
  - **Actuator:** Para monitoreo y health checks del servicio

***

## 🏛️ Arquitectura del Proyecto

El microservicio implementa una **Arquitectura en Capas** para garantizar el desacoplamiento y la facilidad de mantenimiento:

1. **Capa de Presentación (`controller`):** Define los endpoints REST y gestiona la comunicación HTTP con el cliente o API Gateway.
2. **Capa de Lógica (`service`):** Contiene las reglas de negocio, la geocodificación mediante LocationIQ y la conversión de entidades a DTOs.
3. **Capa de Acceso a Datos (`repository`):** Implementa el **Repository Pattern** para interactuar de forma eficiente con PostgreSQL/PostGIS.
4. **Capa de Dominio (`model`):** Contiene la entidad JPA `ReporteGeografico` con soporte de tipo espacial `Point` (JTS Topology Suite).
5. **Capa de Transferencia (`dto`):** Separa la representación interna de la API pública mediante objetos de entrada (`UbicacionRequestDTO`) y salida (`ReporteGeograficoResponseDTO`).

```
src/
└── main/
    ├── java/com/sanosysalvos/geolocalizacion/
    │   ├── GeolocalizacionApplication.java
    │   ├── controller/
    │   │   └── GeolocalizacionController.java
    │   ├── service/
    │   │   └── GeolocalizacionService.java
    │   ├── model/
    │   │   └── ReporteGeografico.java
    │   ├── dto/
    │   │   ├── UbicacionRequestDTO.java
    │   │   ├── ReporteGeograficoResponseDTO.java
    │   │   └── LocationIqResponse.java
    │   └── repository/
    │       └── ReporteGeograficoRepository.java
    └── resources/
        └── application.properties
```

***

## 🛠️ Instalación y Configuración

### Requisitos Previos

- **Docker Desktop** instalado y en ejecución
- **Java 21** instalado (solo si se ejecuta sin Docker)
- **API Key de LocationIQ** — Regístrate gratis en [locationiq.com](https://locationiq.com/)

### 1. Configurar Variables de Entorno

Edita el archivo `src/main/resources/application.properties` con tus propias credenciales:

| Propiedad | Descripción | Ejemplo |
|---|---|---|
| `server.port` | Puerto del servicio | `8081` |
| `spring.datasource.url` | URL de conexión PostgreSQL | `jdbc:postgresql://localhost:5432/sanosysalvos_mascotas` |
| `spring.datasource.username` | Usuario de la base de datos | `postgres` |
| `spring.datasource.password` | Contraseña de la base de datos | `••••••••` |
| `locationiq.api.key` | API Key de LocationIQ | `pk.xxxxxxxxxxxxxxxxx` |
| `locationiq.api.url` | URL base de la API | `https://us1.locationiq.com/v1/search.php` |

> ⚠️ **Seguridad:** Nunca expongas credenciales reales en el repositorio. Usa variables de entorno o un archivo `.env` ignorado por Git.

### 2. Ejecutar con Docker

```bash
# Construir la imagen (multi-stage: Maven para build + Alpine para ejecución)
docker build -t sanosysalvos-geolocalizacion .

# Ejecutar el contenedor
docker run -d \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/sanosysalvos_mascotas \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=tu_password \
  -e LOCATIONIQ_API_KEY=tu_api_key \
  --name geolocalizacion-service \
  sanosysalvos-geolocalizacion
```

### 3. Ejecutar sin Docker

```bash
./mvnw spring-boot:run
```

La API estará disponible en: `http://localhost:8081/api/v1/geolocalizacion`

***

## 📡 Documentación de la API (Endpoints)

**Base URL:** `http://localhost:8081/api/v1/geolocalizacion`

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/geolocalizacion` | Registra una ubicación geocodificando una dirección textual |
| `GET` | `/api/v1/geolocalizacion` | Obtiene la lista completa de reportes geográficos |
| `GET` | `/api/v1/geolocalizacion/{id}` | Obtiene el detalle de un reporte geográfico por su ID |
| `PATCH` | `/api/v1/geolocalizacion/{id}` | Actualiza parcialmente un reporte (radio, estado o dirección) |
| `DELETE` | `/api/v1/geolocalizacion/{id}` | Elimina un reporte geográfico |

### Ejemplo de cuerpo para `POST /api/v1/geolocalizacion`

```json
{
  "mascotaId": 1,
  "direccion": "Av. Apoquindo 4800, Las Condes, Santiago"
}
```

**Response `201 Created`:**

```json
{
  "id": 1,
  "mascotaId": 1,
  "latitud": -33.4156,
  "longitud": -70.5953,
  "radioKm": 5.0,
  "esActivo": true
}
```

### Ejemplo de cuerpo para `PATCH /api/v1/geolocalizacion/{id}`

Todos los campos son opcionales. Si se envía `direccion`, se re-geocodifica automáticamente.

```json
{
  "radioKm": 10.0,
  "esActivo": false,
  "direccion": "Calle Nueva 123, Santiago"
}
```

***

## 🗂️ Modelo de Datos

La entidad principal del dominio es `ReporteGeografico`, mapeada a la tabla `reportes_geograficos` en PostgreSQL:

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Integer` | Identificador único (autoincremental) |
| `mascota_id` | `Integer` | ID de la mascota asociada (referencia externa, requerido) |
| `ubicacion` | `geometry(Point, 4326)` | Coordenadas espaciales en sistema WGS-84 (longitud, latitud) |
| `radio_km` | `Double` | Radio de búsqueda en kilómetros (default: `5.0`) |
| `es_activo` | `Boolean` | Estado del reporte geográfico (default: `true`) |

> El SRID **4326** corresponde al sistema de referencia **WGS-84**, estándar utilizado por GPS y APIs de mapas como Google Maps y OpenStreetMap.

***

## 💡 Decisiones de Diseño Clave

- **Independencia:** Este servicio es autónomo con su propia base de datos y ciclo de vida, desacoplado del resto del sistema.
- **Geocodificación delegada:** La conversión de dirección a coordenadas se delega completamente a LocationIQ, evitando lógica propia de geocodificación.
- **Tipos espaciales nativos:** Se utiliza `org.locationtech.jts.geom.Point` con Hibernate Spatial para persistir geometrías directamente en PostgreSQL sin conversiones adicionales.
- **Actualización parcial:** El endpoint `PATCH` acepta un `Map<String, Object>` genérico, permitiendo actualizar solo los campos enviados sin requerir el objeto completo.
- **Radio por defecto:** Al registrar una ubicación, el radio de búsqueda se inicializa en `5.0 km` automáticamente, valor editable posteriormente.
- **Construcción multi-stage:** El `Dockerfile` usa Maven para compilar y Alpine JDK para ejecutar, optimizando el tamaño final de la imagen Docker.

***

## 🔍 Health Check

Spring Boot Actuator está habilitado. Puedes verificar el estado del servicio en:

```
GET http://localhost:8081/actuator/health
```

***

## 🧪 Ejecutar Tests

```bash
./mvnw test
```

***

## 🌿 Ramas

| Rama | Descripción |
|---|---|
| `main` | Versión estable en producción |
| `develop` | Rama activa de desarrollo |

***

## 👥 Equipo de Desarrollo

- Renato Barriga
- Matías González
- Cristóbal Véliz

Este proyecto es parte del caso semestral: **"Sanos y Salvos – Plataforma Inteligente para la recuperación de mascotas perdidas"**.
