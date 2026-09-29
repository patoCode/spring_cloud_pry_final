# Sistema de Reserva de Cine - Proyecto Final

Este proyecto es una implementación simplificada de un sistema de reserva de entradas de cine, construido aplicando patrones de microservicios como **SAGA**, **Domain-Driven Design (DDD)**, y **Arquitectura Hexagonal**, correspondientes al proyecto final del Curso Spring Boot Cloud.

## Arquitectura y Patrones Implementados

El sistema consta de dos microservicios principales que se comunican de forma síncrona mediante llamadas REST, orquestando una transacción distribuida con el patrón **SAGA**.

*   **Arquitectura Hexagonal:** Ambos servicios separan estrictamente el código en capas `api`, `application`, `domain`, `infrastructure` y `exception`.
*   **Domain-Driven Design (DDD):** Entidades de dominio ricas con invariantes de negocio encapsuladas (ej. la reserva no permite asientos negativos `availableSeats < 0`).
*   **SAGA de 2 Pasos:** `booking-service` actúa como orquestador. Las reservas crean un estado inicial (`PENDING`), se comunican con `movie-service` para confirmar el asiento, y en caso de éxito se marcan como `CONFIRMED`. Si hay fallo de red o falta de asientos, la reserva se compensa marcándose como `CANCELLED`.
*   **Seguridad:** Integración con OAuth2 JWT Resource Server en ambos microservicios.
*   **Resiliencia:** Uso de la anotación `@Retryable` en la comunicación REST para manejar fallos de red esporádicos con *backoff*.
*   **Documentación:** API completamente documentada con OpenAPI (Swagger) accesible vía web.

---

## Servicios

### 1. `movie-service` (Puerto: 51100)
Administra el catálogo de películas y la disponibilidad de las funciones.
*   **Base de datos:** `moviedb` (PostgreSQL)
*   **Entidades:** `Movie` y `Screening`.
*   **Swagger UI:** [http://localhost:51100/swagger-ui.html](http://localhost:51100/swagger-ui.html)

### 2. `booking-service` (Puerto: 51200)
Gestor de las reservas de los clientes y orquestador del flujo SAGA.
*   **Base de datos:** `bookingdb` (PostgreSQL)
*   **Entidad:** `Booking` (con variables de estado `PENDING`, `CONFIRMED`, `CANCELLED`).
*   **Swagger UI:** [http://localhost:51200/swagger-ui.html](http://localhost:51200/swagger-ui.html)

---

## Instrucciones de Arranque

### Prerrequisitos
*   **Java 21+** instalado.
*   **Docker** para levantar la base de datos PostgreSQL.
*   Maven configurado en el sistema.

### 1. Levantar la Base de Datos
Desde la raíz del proyecto:
```bash
docker compose up -d
```
Levanta PostgreSQL (`cinema-postgres`, puerto `5432`, usuario `admin` / contraseña `password`) y ejecuta `docker/init-db/create-databases.sql` en el primer arranque para crear `moviedb` y `bookingdb`.

### 2. Levantar el Auth-Service
```bash
cd auth-service
mvn clean compile spring-boot:run
```
Es el emisor (Issuer) de los JWT en el puerto `50003` (`http://localhost:50003`), expone OIDC Discovery y JWKS. **Debe estar arriba antes** que los microservicios, ya que validan el `issuer-uri` al arrancar.

Clientes registrados:
*   `cinema-client` / secreto `cinema-secret` → `client_credentials` (automatizable con curl/Postman).
*   `cinema-swagger` (público, PKCE) → `authorization_code` para los Swagger UI.

Usuarios en memoria para el flujo de login: `demo`/`demo` y `admin`/`admin`.

### 3. Compilar e Iniciar los Servicios
Orden recomendado: **Docker → auth-service → movie-service → booking-service**. En dos terminales separadas:

**Para Movie Service:**
```bash
cd movie-service
mvn clean compile spring-boot:run
```
*(Flyway creará automáticamente las tablas e insertará las películas y funciones de prueba).*

**Para Booking Service:**
```bash
cd booking-service
mvn clean compile spring-boot:run
```

### 4. Pruebas y Uso
Una vez iniciados:
1. Obtén un token:
   ```bash
   curl -u cinema-client:cinema-secret -d grant_type=client_credentials -d scope=cinema http://localhost:50003/oauth2/token
   ```
   O bien ejecuta en Postman el request **Auth Service → Get Token (client_credentials)**, que guarda el token en la variable `TOKEN`.
2. Accede a los Swagger UI y pega el token en **Authorize** (Bearer JWT), o usa la colección `postman/Cinema_System.postman_collection.json`.
3. Realiza la prueba del camino feliz creando una reserva en `POST /bookings` (201 + estado `CONFIRMED`).
4. Verifica que el asiento se ha descontado llamando a `GET /screenings/{id}`. El `booking-service` reenvía el JWT recibido hacia `movie-service` (token relay), por lo que `PATCH /screenings/{id}/reserve` autentica correctamente.
5. Cancela la reserva con `PATCH /bookings/{id}/cancel` (SAGA inverso: libera el asiento) o intenta reservar en una sala llena para ver la compensación en los logs del `booking-service` (`Fallo al reservar asiento. Ejecutando compensación`).
