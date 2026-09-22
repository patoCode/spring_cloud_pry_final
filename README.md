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
1. Ejecutar un contenedor de PostgreSQL (por ejemplo, en el puerto `5432` con usuario `admin` y contraseña `password`).
2. Ejecutar el script `docker/init-db/create-databases.sql` para crear las bases de datos `moviedb` y `bookingdb` con sus permisos correspondientes.

### 2. Levantar el Auth-Service
Debes levantar el servicio de autenticación proporcionado durante el curso en el puerto `50003`, el cual servirá como emisor (Issuer) de los JWT para los microservicios de cine.

### 3. Compilar e Iniciar los Servicios
Para arrancar los microservicios, abre dos terminales separadas en la raíz de cada proyecto y ejecuta:

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
1. Accede a los Swagger UI mencionados arriba.
2. Autoriza tu sesión en Swagger utilizando un token "Bearer JWT" válido (generado por tu `auth-service` en el puerto `50003`).
3. Realiza la prueba del camino feliz creando una reserva en `POST /bookings`.
4. Verifica que el asiento se ha descontado llamando a `GET /screenings/{id}`.
5. Intenta realizar una reserva en una sala llena para forzar y visualizar cómo la compensación del SAGA marca automáticamente el estado del booking en `CANCELLED`.
