# Backend Context

Este documento resume el estado funcional actual del backend `JavaBackEndDash` (Spring Boot), incluyendo entidades, reglas, endpoints, permisos, entradas/salidas y manejo de errores.

## 1. Estructura actual

- `src/main/java/com/nicolas/backenddash/auth`: login, register, JWT.
- `src/main/java/com/nicolas/backenddash/usuario`: usuarios, roles, estado, administración.
- `src/main/java/com/nicolas/backenddash/empresa`: empresas, activación/desactivación.
- `src/main/java/com/nicolas/backenddash/dashboard`: dashboards y asignación de consumidores.
- `src/main/java/com/nicolas/backenddash/security`: interceptor JWT + autorización.
- `src/main/java/com/nicolas/backenddash/error`: respuesta estándar de errores.

## 2. Entidades y enums

### Empresa
- `id: UUID`
- `nombre: String`
- `numeroEmpleados: Integer` (calculado desde usuarios)
- `activa: Boolean` (default `true`)

### Usuario
- `id: UUID`
- `nombre: String`
- `apellidos: String`
- `rol: UsuarioRol`
- `estado: UsuarioEstado`
- `email: String`
- `passwordHash: String`
- `activo: Boolean`
- `empresa: Empresa`
- `createdAt`, `updatedAt`

### Dashboard
- `id: UUID`
- `nombre: String`
- `tipo: DashboardType` (`POWER_BI`, `TEMPLATE`)
- `contenido: String`
- `creador: Usuario`
- `empresa: Empresa`
- `usuariosAsignados: Set<Usuario>`

### Roles (`UsuarioRol`)
- `SUPER_ADMIN`
- `ADMIN`
- `DASHBOARD_CREADOR`
- `DASHBOARD_USUARIO`

### Estado usuario (`UsuarioEstado`)
- `NO_APROBADO`
- `APROBADO`

## 3. Reglas de seguridad y negocio

1. Todos los IDs son UUID.
2. `POST /api/auth/register` crea usuario con:
   - `rol = DASHBOARD_USUARIO`
   - `estado = NO_APROBADO`
   - `activo = true`
3. Login requiere:
   - credenciales válidas
   - `activo = true`
   - `estado = APROBADO`
   - empresa activa (excepto `SUPER_ADMIN`)
4. En rutas protegidas (`/api/**`):
   - token `Bearer` obligatorio
   - se valida usuario actual en BD
   - si `activo=false` => bloqueado
   - si `estado!=APROBADO` => bloqueado
   - si empresa inactiva => bloqueado para no-super-admin
5. `SUPER_ADMIN` tiene alcance global.
6. `ADMIN` solo opera dentro de su empresa.
7. `DASHBOARD_CREADOR` gestiona solo dashboards propios.
8. `DASHBOARD_USUARIO` solo ve dashboards asignados.
9. Asignar consumidor a dashboard exige:
   - rol del usuario destino = `DASHBOARD_USUARIO`
   - misma empresa dashboard/usuario
   - si ya estaba asignado => conflicto.

## 4. Endpoints

## 4.1 Auth

### `POST /api/auth/register` (público)
**Input**
```json
{
  "nombre": "string",
  "apellidos": "string",
  "email": "user@example.com",
  "password": "min-8",
  "empresaId": "uuid"
}
```
**Output 200** (`AuthResponse`)
```json
{
  "token": "jwt",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "usuario": {
    "id": "uuid",
    "nombre": "string",
    "apellidos": "string",
    "rol": "DASHBOARD_USUARIO",
    "estado": "NO_APROBADO",
    "email": "user@example.com",
    "activo": true,
    "empresaId": "uuid",
    "createdAt": "datetime",
    "updatedAt": "datetime"
  }
}
```
**Errores frecuentes**
- `400`: `empresaId must belong to an existing empresa`
- `409`: `Email already exists`
- `400`: errores de validación DTO

### `POST /api/auth/login` (público)
**Input**
```json
{ "email": "user@example.com", "password": "string" }
```
**Output 200**: `AuthResponse`
**Errores frecuentes**
- `401`: `Invalid credentials`
- `401`: `User is not approved yet`
- `403`: `Empresa is inactive`

## 4.2 Empresas

### `GET /api/empresas` (público)
Lista empresas para registro.

**Output 200**
```json
[
  { "id": "uuid", "nombre": "Empresa X", "numeroEmpleados": 10, "activa": true }
]
```

### `GET /api/empresas/{id}` (ADMIN/SUPER_ADMIN)
### `POST /api/empresas` (SUPER_ADMIN)
### `PUT /api/empresas/{id}` (SUPER_ADMIN)
### `DELETE /api/empresas/{id}` (SUPER_ADMIN)
### `PATCH /api/empresas/{id}/activa` (ADMIN de esa empresa o SUPER_ADMIN)

**Input `PATCH /activa`**
```json
{ "activa": false }
```

**Errores frecuentes**
- `403`: acceso fuera de alcance de empresa
- `403`: intento de cambiar recursos restringidos de empresa madre
- `409`: nombre duplicado o empresa con usuarios al eliminar
- `404`: empresa no encontrada

## 4.3 Usuarios (todas protegidas)

### `GET /api/usuarios`
### `GET /api/usuarios/{id}`
### `POST /api/usuarios`
### `PUT /api/usuarios/{id}`
### `DELETE /api/usuarios/{id}`

### `PATCH /api/usuarios/{id}/estado`
**Input**
```json
{ "estado": "APROBADO" }
```

### `PATCH /api/usuarios/{id}/activo`
**Input**
```json
{ "activo": false }
```

**Notas**
- Solo `ADMIN` de la misma empresa o `SUPER_ADMIN` pueden cambiar `estado`/`activo`.
- Solo `SUPER_ADMIN` puede asignar rol `SUPER_ADMIN`.

**UsuarioRequest (create/update)**
```json
{
  "nombre": "string",
  "apellidos": "string",
  "rol": "ADMIN|DASHBOARD_CREADOR|DASHBOARD_USUARIO|SUPER_ADMIN",
  "email": "user@example.com",
  "password": "min-8",
  "activo": true,
  "estado": "APROBADO|NO_APROBADO",
  "empresaId": "uuid"
}
```

## 4.4 Dashboards (todas protegidas)

### `GET /api/dashboards`
### `GET /api/dashboards/{id}`
### `POST /api/dashboards`
### `PUT /api/dashboards/{id}`
### `DELETE /api/dashboards/{id}`

### `POST /api/dashboards/{id}/usuarios`
Asigna consumidor.

**Input**
```json
{ "usuarioId": "uuid" }
```

**Errores frecuentes**
- `403`: usuario no consumidor
- `403`: usuario/dashboard de empresas distintas
- `409`: consumidor ya asignado

### `DELETE /api/dashboards/{id}/usuarios/{usuarioId}`
Quita consumidor (también valida rol consumidor).

## 5. Formato estándar de error

Todas las excepciones retornan:
```json
{
  "timestamp": "2026-04-27T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/...",
  "details": [
    { "field": "email", "message": "email format is invalid" }
  ]
}
```

### Mapeo relevante
- `MethodArgumentNotValidException` -> `400 Validation failed`
- `ConstraintViolationException` -> `400 Validation failed`
- `HttpMessageNotReadableException` -> `400 Invalid request body`
- `MethodArgumentTypeMismatchException` -> `400 Invalid path/query parameter`
- `ResponseStatusException` -> estado/mensaje de negocio
- `DataIntegrityViolationException` -> `409 Resource conflict`
- `DataAccessException` -> `500 Database operation failed`
- `Exception` -> `500 Internal server error`

## 6. Rutas públicas vs protegidas

### Públicas
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/empresas`
- `swagger-ui` y `v3/api-docs`

### Protegidas
- Todo `/api/**` no listado arriba.

## 7. Observaciones operativas

- Las reglas actuales priorizan seguridad por empresa y estado de usuarios.
- Si empresa se desactiva (`activa=false`), empleados no-super-admin quedan bloqueados en rutas protegidas.
- `SUPER_ADMIN` no queda bloqueado por empresa inactiva.

