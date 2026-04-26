# Backend Dash Java

Backend Spring Boot para administrar dashboards, usuarios y autenticacion con JWT.

## Ejecutar Localmente

En Git Bash:

```bash
./mvnw spring-boot:run
```

Si la terminal muestra Java 8, cierra Git Bash y abre una nueva. El wrapper del proyecto prioriza el JDK 21 instalado en `C:\Program Files\Java`.

En PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en:

```text
http://localhost:8080
```

## Documentacion Swagger

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Autenticacion

Iniciar sesion:

```http
POST /api/auth/login
```

Registrar consumidor:

```http
POST /api/auth/register
```

Las rutas protegidas requieren:

```http
Authorization: Bearer <token>
```

## Roles

- `ADMIN`: administra usuarios y dashboards.
- `DASHBOARD_CREADOR`: administra solo sus dashboards y asigna consumidores.
- `DASHBOARD_USUARIO`: ve solo dashboards asignados.

## Modulos

- `auth`: login, registro y JWT.
- `usuario`: CRUD administrativo de usuarios.
- `dashboard`: CRUD de dashboards y asignacion a consumidores.
- `security`: autenticacion, contexto actual y reglas compartidas.
