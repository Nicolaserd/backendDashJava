# Reglas Generales del Proyecto

## Organizacion Por Entidades

El backend se organiza por entidad o modulo funcional. Cada entidad debe vivir en su propio paquete dentro de `src/main/java/com/nicolas/backenddash/`.

Ejemplo actual:

```text
dashboard/
+-- Dashboard.java
+-- DashboardType.java
+-- DashboardRepository.java
+-- DashboardService.java
+-- DashboardController.java
+-- dto/
    +-- DashboardRequest.java
    +-- DashboardResponse.java
```

Si se agrega una nueva entidad, debe seguir el mismo patron:

```text
entidad/
+-- Entidad.java
+-- EntidadRepository.java
+-- EntidadService.java
+-- EntidadController.java
+-- dto/
    +-- EntidadRequest.java
    +-- EntidadResponse.java
```

## Revision Obligatoria Antes De Cambiar Codigo

Antes de crear, editar o eliminar archivos, se debe revisar el codigo existente relacionado con el cambio. La revision debe identificar:

- Si ya existe una entidad, servicio, repositorio, DTO o metodo que resuelva parte del problema.
- Si hay logica repetida que pueda reutilizarse.
- Si el cambio pertenece a un modulo existente o necesita uno nuevo.
- Si eliminar codigo puede romper endpoints, tests o configuracion.

No se debe implementar una solucion nueva sin verificar primero si existe una logica reutilizable.

## Regla DRY Y Reutilizacion

Aplicar DRY: no duplicar logica de negocio, conversiones, validaciones o consultas si pueden centralizarse. Si dos entidades necesitan comportamiento similar, primero evaluar una utilidad, metodo privado, clase compartida o abstraccion simple.

La reutilizacion debe ser practica. No crear abstracciones grandes si solo hay un caso de uso. Extraer codigo compartido cuando reduzca duplicacion real o mejore mantenimiento.

## Separacion De Responsabilidades

- `Controller`: recibe HTTP, valida entrada y devuelve respuestas.
- `Service`: contiene logica de negocio y coordina repositorios.
- `Repository`: accede a base de datos con Spring Data JPA.
- `Entity`: representa la tabla y reglas de persistencia.
- `dto`: define entrada y salida de la API.

No poner logica de negocio en controllers ni acceso directo a base de datos fuera de repositories.

## Cambios De Base De Datos

Toda entidad persistente debe mapearse con JPA/Hibernate. Los nombres de tabla deben ser claros y en plural cuando representen colecciones, por ejemplo `dashboards`.

Las credenciales y URLs de base de datos deben estar en variables de entorno. No subir `.env`, passwords, tokens ni claves al repositorio.

## Verificacion Minima

Despues de cambios de codigo, ejecutar:

```powershell
.\mvnw.cmd test
```

Si el cambio afecta endpoints, probar manualmente el flujo REST correspondiente.

## Regla Operativa De Puertos De Prueba

Cuando se levante un servidor local para pruebas manuales (por ejemplo `8080`, `8081`, `8082`, etc.), al finalizar las pruebas se debe detener ese proceso y liberar el puerto.

No se deben dejar servidores de prueba activos en segundo plano al terminar una tarea.

## Regla De Limpieza De Archivos No Usados

Si durante una tarea se crea un archivo y al finalizar se confirma que no se usa en la aplicacion (codigo, configuracion o documentacion requerida), ese archivo debe eliminarse antes de cerrar la tarea.

Esto aplica especialmente a archivos temporales de pruebas, logs locales, borradores y artefactos auxiliares.
