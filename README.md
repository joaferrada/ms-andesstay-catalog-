# ms-andesstay-catalog

Catalogo de unidades de alojamiento de AndesStay (puerto **8085**): habitaciones, cabanas, lodges y domos de la red de 20 hostales. Spring Boot 4.1.1 · Java 17 · Resource Server JWT (Microsoft Entra ID) · Oracle Autonomous Database.

```
Frontend --> API Gateway --> BFF :8080 --> catalog :8085 (GET y CRUD)
                                reservations :8081 --> catalog :8085 (reserve / release de cupos, JWT propagado)
```

## Ejecutar

```powershell
$env:DB_URL="jdbc:oracle:thin:@andesstaydb_tp?TNS_ADMIN=C:/oracle/wallet_andesstay"
$env:DB_USERNAME="ANDESSTAY_CATALOG"
$env:DB_PASSWORD="<password de ANDESSTAY_CATALOG>"
.\mvnw.cmd spring-boot:run
```

| Variable | Default | Descripcion |
|---|---|---|
| `DB_URL` | `jdbc:oracle:thin:@andesstaydb_tp?TNS_ADMIN=C:/oracle/wallet_andesstay` | Alias TNS + carpeta del wallet |
| `DB_USERNAME` | `ANDESSTAY_CATALOG` | Usuario (schema) del catalogo |
| `DB_PASSWORD` | (sin default) | Obligatoria; nunca se versiona |

Ver `.env.example`. El wallet, los `.zip` y el `.env` estan en `.gitignore`.

## Base de datos

Misma BD Oracle ADB `andesstaydb` que reservations (ver su README para crear la BD y descargar el wallet), con un schema propio. En **Database Actions > SQL** como `ADMIN`:

```sql
CREATE USER ANDESSTAY_CATALOG IDENTIFIED BY "<password segura>";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO ANDESSTAY_CATALOG;
ALTER USER ANDESSTAY_CATALOG QUOTA UNLIMITED ON DATA;
```

Hibernate crea la tabla `CATALOG_UNITS` (`ddl-auto: update`). Al arrancar, `config/CatalogSeeder` carga **6 unidades de ejemplo solo si la tabla esta vacia**.

## Endpoints

Todos requieren `Authorization: Bearer <JWT de Entra ID>`.

| Metodo | Ruta | Descripcion | Codigos |
|---|---|---|---|
| GET | `/api/catalog/units` | Unidades activas. Filtros opcionales: `tipo`, `hostal` (sin distinguir mayusculas), `soloDisponibles=true` | 200, 400 |
| GET | `/api/catalog/units/{id}` | Detalle (incluye dadas de baja) | 200, 404 |
| POST | `/api/catalog/units` | Crea una unidad | 201, 400 |
| PUT | `/api/catalog/units/{id}` | Actualiza una unidad | 200, 400, 404 |
| DELETE | `/api/catalog/units/{id}` | Baja logica (`activo=false`) | 204, 404 |
| POST | `/api/catalog/units/{id}/reserve` | `cuposDisponibles - 1` | 200, 404, 409 |
| POST | `/api/catalog/units/{id}/release` | `cuposDisponibles + 1` sin superar `cuposTotales` | 200, 404 |

**Body de POST/PUT** (`dto/UnitRequest`):

| Campo | Regla |
|---|---|
| `nombre`, `hostal`, `ubicacion` | obligatorios |
| `tipo` | `HABITACION`, `CABANA`, `LODGE` o `DOMO` |
| `capacidad`, `tarifaNoche`, `cuposTotales` | obligatorios y > 0 |
| `cuposDisponibles` | opcional, entre 0 y `cuposTotales`. Al crear, por defecto `cuposTotales`; al actualizar se conserva (recortado si baja el total) |
| `activo` | opcional, por defecto `true` |

- `reserve` responde **409** si la unidad no tiene cupos o esta inactiva; `release` nunca supera `cuposTotales` (si ya estaban todos libres, no cambia nada).
- `@Version` en la entidad: dos operaciones de cupo simultaneas sobre la misma unidad no pueden descontar el mismo cupo (la segunda recibe 409 y debe reintentar).
- Errores JSON `{timestamp, status, error, message}`: 400 (validacion, `tipo` invalido), 404, 409 y 503 (BD no disponible).

```bash
curl -X POST http://localhost:8085/api/catalog/units \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H "Content-Type: application/json" \
  -d '{"nombre":"Cabana Lago Llanquihue","tipo":"CABANA","hostal":"Hostal Puerto Varas","ubicacion":"Puerto Varas, Los Lagos","capacidad":4,"tarifaNoche":85000,"cuposTotales":3}'
```

## Roles

App roles de Entra ID (claim `roles` -> `ROLE_*`, `security/JwtAuthConverter`), aplicados con `@PreAuthorize`. Si no se cumplen: **403**.

| Endpoint | Admin | Operador_Dominio | Cliente_Dominio | Auditor |
|---|---|---|---|---|
| GET (listar y `/{id}`) | si | si | si | si |
| POST / PUT / DELETE de unidades | si | no | no | no |
| POST `/{id}/reserve` y `/{id}/release` | si | si | no | no |

`reserve` y `release` los llama reservations con el JWT del usuario que confirma/cancela (Admin u Operador, los mismos que pueden cambiar el estado de una reserva).

## Docker

`Dockerfile` multi-stage: build con `maven:3.9-eclipse-temurin-17` y runtime `eclipse-temurin:17-jre` con usuario no root, puerto **8085** y `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`. Se levanta junto al resto del backend con el compose unico de `infra` (ver su README):

```powershell
cd ..\infra
docker compose up -d --build catalog
docker compose logs -f catalog
```

En el compose usa el wallet montado en `/wallet` (solo lectura) con `DB_URL=jdbc:oracle:thin:@andesstaydb_tp?TNS_ADMIN=/wallet`, usuario `ANDESSTAY_CATALOG` y clave `CATALOG_DB_PASSWORD`. Healthcheck: `GET http://localhost:8085/actuator/health`.

## Tests

```powershell
.\mvnw.cmd clean package
```

No necesitan OCI: perfil `test` con H2 en memoria (`src/test/resources/application-test.yaml`).

- `UnitTest`: el cupo no baja de 0 ni supera el total.
- `UnitServiceTest`: reserve/release, 409 sin cupos o inactiva, 404, baja logica, recorte de cupos al actualizar.
- `UnitRequestValidationTest`: obligatorios, > 0 y `cuposDisponibles` entre 0 y el total.
- `UnitRepositoryTest` (`@DataJpaTest`): filtros por tipo, hostal y disponibilidad; solo activas.
- `UnitControllerSecurityTest`: 200/201/204/403 por rol en cada endpoint, 401 sin token, 400/404/409 con mensaje.
- `MsAndesstayCatalogApplicationTests`: el seeder carga 6 unidades una sola vez.
