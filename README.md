# Movie Catalog Service

Microservicio desarrollado con **Java**, **Spring Boot** y **Gradle** que actúa como fachada de la API de **TMDB**.

El objetivo del proyecto es construir progresivamente una aplicación de cine mientras se practican integraciones HTTP reales con **Spring Cloud OpenFeign**, incluyendo autenticación mediante interceptor, tratamiento de errores con `ErrorDecoder`, resiliencia, caché distribuida con Redis, observabilidad y documentación OpenAPI.

## Objetivos

El servicio será responsable de:

- Consultar películas y series en TMDB.
- Buscar contenido por título.
- Recuperar películas en tendencia.
- Obtener información detallada de una película.
- Consultar recomendaciones y contenido relacionado.
- Consultar proveedores de streaming por país.
- Adaptar los contratos externos de TMDB a modelos propios.
- Cachear respuestas para reducir llamadas innecesarias.
- Traducir errores externos a un contrato de errores estable.

## Stack tecnológico

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje y toolchain |
| Spring Boot 4 | Framework principal |
| Gradle | Construcción y gestión de dependencias |
| Spring Web MVC | API REST |
| Spring Cloud OpenFeign | Cliente HTTP declarativo para TMDB |
| Resilience4j | Circuit breaker y políticas de resiliencia |
| Spring Cache | Abstracción de caché |
| Spring Data Redis | Integración con Redis |
| Redis | Caché distribuida |
| Spring Boot Actuator | Health checks y métricas |
| Springdoc OpenAPI | OpenAPI y Swagger UI |
| Docker Compose | Entorno local de Redis |
| JUnit | Testing |

## Arquitectura inicial

```text
Cliente
   |
   v
MovieController
   |
   v
MovieCatalogService
   |
   +------> Redis Cache
   |
   v
TmdbClient (OpenFeign)
   |
   v
TMDB API
```

Los DTO recibidos desde TMDB no deben exponerse directamente desde los controladores:

```text
TMDB response DTO
        |
        v
      Mapper
        |
        v
Internal model / API response
```

## Estructura propuesta

```text
src/main/java/dev/berto/moviecatalog/
├── application/
│   └── service/
├── domain/
│   ├── exception/
│   └── model/
├── infrastructure/
│   ├── cache/
│   └── tmdb/
│       ├── client/
│       ├── config/
│       ├── dto/
│       ├── error/
│       ├── interceptor/
│       └── mapper/
└── web/
    ├── controller/
    ├── error/
    └── response/
```

Esta estructura es orientativa. El objetivo es mantener aislada la integración con TMDB sin sobredimensionar la arquitectura durante las primeras fases.

## Requisitos

- Java 21.
- Docker y Docker Compose.
- Una cuenta de TMDB.
- Un token de lectura de la API de TMDB.

Comprueba las versiones instaladas:

```bash
java --version
docker --version
docker compose version
```

## Configuración

### Variable de entorno

La aplicación espera el token de TMDB en la variable:

```text
TMDB_ACCESS_TOKEN
```

En una terminal:

```bash
export TMDB_ACCESS_TOKEN="tu-token-de-tmdb"
```

En IntelliJ IDEA:

```text
Run
└── Edit Configurations
    └── Environment variables
        └── TMDB_ACCESS_TOKEN=tu-token-de-tmdb
```

No guardes el token real en `application.yml`, `application.properties` ni en Git.

### Configuración de aplicación

Ejemplo para `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: movie-catalog-service

  data:
    redis:
      host: localhost
      port: 6379
      connect-timeout: 2s
      timeout: 2s

  cache:
    type: redis
    redis:
      time-to-live: 30m
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "movie-catalog::"

  cloud:
    openfeign:
      client:
        config:
          tmdbClient:
            connect-timeout: 2000
            read-timeout: 5000
            logger-level: basic

tmdb:
  base-url: https://api.themoviedb.org/3
  access-token: ${TMDB_ACCESS_TOKEN}
  language: es-ES
  region: ES

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## Redis con Docker Compose

El archivo `compose.yaml` debe estar en la raíz del proyecto:

```text
movie-catalog-service/
├── compose.yaml
├── build.gradle
├── settings.gradle
├── gradlew
└── src/
```

Levantar Redis manualmente:

```bash
docker compose up -d
```

Comprobar su estado:

```bash
docker compose ps
```

Probar la conexión:

```bash
docker exec -it movie-catalog-redis redis-cli ping
```

Respuesta esperada:

```text
PONG
```

Detener los contenedores:

```bash
docker compose down
```

Detenerlos y eliminar también el volumen:

```bash
docker compose down -v
```

Al incluir `spring-boot-docker-compose` como dependencia de desarrollo, Spring Boot también puede detectar el archivo `compose.yaml` y gestionar los servicios durante la ejecución local.

## Ejecución

### Con Gradle Wrapper

```bash
./gradlew bootRun
```

### Desde IntelliJ IDEA

Ejecuta la clase:

```text
MovieCatalogServiceApplication
```

## Compilación

```bash
./gradlew clean build
```

Ejecutar únicamente los tests:

```bash
./gradlew test
```

## OpenAPI y Swagger

Con la aplicación levantada:

```text
Swagger UI:
http://localhost:8080/swagger-ui.html

Especificación OpenAPI:
http://localhost:8080/v3/api-docs
```

## Actuator

Health check:

```text
http://localhost:8080/actuator/health
```

Métricas:

```text
http://localhost:8080/actuator/metrics
```

## API prevista

### Películas en tendencia

```http
GET /api/v1/movies/trending?window=DAY&page=1
```

### Buscar películas

```http
GET /api/v1/movies/search?query=alien&page=1
```

### Obtener detalle

```http
GET /api/v1/movies/{movieId}
```

### Obtener recomendaciones

```http
GET /api/v1/movies/{movieId}/recommendations
```

### Consultar plataformas de streaming

```http
GET /api/v1/movies/{movieId}/providers?country=ES
```

Los endpoints se irán implementando de forma incremental.

## Integración con OpenFeign

El cliente de TMDB deberá incorporar:

- `RequestInterceptor` para añadir el token Bearer.
- `ErrorDecoder` para interpretar errores de TMDB.
- Timeouts de conexión y lectura.
- Logging HTTP controlado.
- Circuit breaker.
- Reintentos únicamente para fallos transitorios.
- Métricas y observabilidad.

Headers mínimos enviados a TMDB:

```http
Authorization: Bearer <TMDB_ACCESS_TOKEN>
Accept: application/json
```

## Contrato de errores

El servicio no debe exponer directamente excepciones internas de Feign.

Ejemplo de respuesta:

```json
{
  "code": "MOVIE_NOT_FOUND",
  "message": "No se ha encontrado la película solicitada",
  "status": 404,
  "timestamp": "2026-07-16T17:30:00Z"
}
```

Mapeo inicial:

| Estado de TMDB | Excepción interna |
|---:|---|
| 400 | `TmdbBadRequestException` |
| 401 | `TmdbAuthenticationException` |
| 404 | `TmdbNotFoundException` |
| 429 | `TmdbRateLimitException` |
| 5xx | `TmdbServerException` |

## Estrategia de caché

TTL iniciales orientativos:

| Caché | TTL |
|---|---:|
| Tendencias | 20 minutos |
| Búsquedas | 10 minutos |
| Detalles | 6 horas |
| Géneros | 24 horas |
| Proveedores | 6 horas |

Las claves deben incluir todos los argumentos que alteren el resultado:

```text
movie-details::550:es-ES
trending-movies::DAY:1:es-ES
search-movies::alien:1:es-ES
```

Para inspeccionar claves en desarrollo:

```bash
docker exec -it movie-catalog-redis redis-cli
```

```redis
SCAN 0 MATCH "movie-catalog::*"
```

## Roadmap

### Fase 1 — Integración mínima

- [ ] Configurar las propiedades de TMDB.
- [ ] Crear el interceptor de autenticación.
- [ ] Crear el cliente Feign.
- [ ] Consultar películas en tendencia.
- [ ] Exponer el primer endpoint REST.

### Fase 2 — Gestión de errores

- [ ] Crear la jerarquía de excepciones.
- [ ] Implementar el `ErrorDecoder`.
- [ ] Crear un `@RestControllerAdvice`.
- [ ] Definir un contrato de errores común.

### Fase 3 — Catálogo

- [ ] Añadir búsqueda de películas.
- [ ] Añadir detalle de película.
- [ ] Añadir reparto y equipo.
- [ ] Añadir recomendaciones.
- [ ] Añadir proveedores de streaming.

### Fase 4 — Caché y resiliencia

- [ ] Añadir `@Cacheable`.
- [ ] Configurar TTL por caché.
- [ ] Configurar circuit breaker.
- [ ] Configurar retries para fallos transitorios.
- [ ] Gestionar respuestas `429`.

### Fase 5 — Testing

- [ ] Tests unitarios del mapper.
- [ ] Tests unitarios del servicio.
- [ ] Tests del `ErrorDecoder`.
- [ ] Tests de integración con WireMock.
- [ ] Tests de caché con Redis.
- [ ] Verificación de headers enviados por el interceptor.

### Fase 6 — Evolución de la aplicación

- [ ] Favoritos.
- [ ] Lista de películas pendientes.
- [ ] Películas vistas.
- [ ] Valoraciones de usuario.
- [ ] Recomendaciones personalizadas.
- [ ] Salas compartidas para elegir qué ver.

## Orden de implementación recomendado

```text
1. Configuración tipada de TMDB
2. RequestInterceptor
3. Cliente Feign
4. Endpoint de tendencias
5. ErrorDecoder
6. Contrato global de errores
7. Búsqueda
8. Detalle de película
9. Redis
10. Resilience4j
11. WireMock
12. OpenAPI
```

## Convenciones

- No exponer DTO externos directamente.
- No registrar tokens ni headers de autorización.
- No reintentar errores funcionales como `400`, `401` o `404`.
- Usar records para DTO inmutables cuando resulte apropiado.
- Mantener la lógica de integración dentro de `infrastructure.tmdb`.
- Usar nombres de caché y claves explícitos.
- Añadir tests para cada nuevo código de error gestionado.

## Aviso sobre TMDB

Este proyecto utiliza la API de TMDB como fuente externa de información cinematográfica.

TMDB no respalda ni certifica este proyecto. Antes de publicar o distribuir la aplicación, deben revisarse sus requisitos de atribución y condiciones de uso.

## Estado

Proyecto en desarrollo.
