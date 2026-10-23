# Medix (Swing + MySQL)

Aplicación de escritorio de gestión de pacientes y citas. La rama parte de un repositorio que solo tenía un fragmento incompleto de una clase Swing; esta versión establece una estructura ejecutable nueva con Java 17+, Maven y MySQL 8.0.16+.

## Configuración

1. Haz respaldo de la base de datos destino antes de migrar.
2. Aplica una sola vez [V1__medix_schema.sql](src/main/resources/db/migration/V1__medix_schema.sql) a una instancia MySQL vacía. El script crea tablas y no borra filas. No es una migración automática para un esquema legado.
3. Configura las variables de conexión. PowerShell de ejemplo:
   ```powershell
   $env:MEDIX_DB_URL = "jdbc:mysql://localhost:3306/medix?useSSL=true&serverTimezone=UTC"
   $env:MEDIX_DB_USER = "medix_app"
   $env:MEDIX_DB_PASSWORD = "<secreto-local>"
   ```
   Usa una cuenta MySQL con permisos mínimos sobre la base medix. No guardes secretos en Git.
4. Crea la primera cuenta gerente (solo funciona cuando no existen cuentas):
   ```powershell
   mvn -q -Dexec.mainClass=com.mycompany.medix.BootstrapManager exec:java
   ```
   Se solicitan usuario, nombre y contraseña en una terminal interactiva. La contraseña se guarda con bcrypt; el programa no la imprime.
5. Inicia la aplicación con `mvn exec:java`.

## Operaciones incluidas

- Inicio y cierre de sesión por roles, bcrypt, cambio de contraseña y bloqueo de operaciones por rol en el repositorio.
- Alta de pacientes por gerente: contraseña temporal aleatoria, hash bcrypt, marca de cambio obligatorio y revelado una sola vez en la pantalla de alta.
- Edición de pacientes: atención al cliente solo puede cambiar teléfono, correo y dirección; el repositorio también limita la actualización. Los pacientes solo actualizan su propia cuenta.
- Selector de calendario y consulta de doctores/horarios con jornada, bloqueos y citas.
- Reserva transaccional que bloquea la fila del doctor, vuelve a validar el intervalo e impide solapamientos concurrentes para ese doctor.

## Configurar doctores y horarios

Los doctores y horarios deben ser cargados por el administrador de base de datos según la jornada real de la clínica. Usa registros de `doctors` y `doctor_working_hours`; `weekday` usa lunes=1 y domingo=7. Los bloqueos se registran en `doctor_unavailability`. No se inventaron horarios ni se cargaron datos clínicos.

## Pruebas

Ejecuta `mvn test`. Las pruebas incluidas cubren generación/hash de contraseñas. Las pruebas que requieren MySQL, concurrencia, Swing o permisos integrados necesitan una base de pruebas aislada y todavía deben añadirse/ejecutarse antes de usar datos reales.

## Límites conocidos de esta primera base

- El repositorio original no contenía un esquema ni lógica de citas que preservar. V1 define un esquema nuevo y no transforma tablas heredadas.
- El alta inicial de gerente está disponible. El alta de cuentas internas (atención al cliente y doctor), reportes, cancelaciones, historial de citas y calendarios de trabajo desde la interfaz todavía requieren desarrollo.
- No se ha ejecutado migración contra una base ni se ha verificado conexión MySQL en este entorno. Revisa la URL TLS/certificados de tu instalación antes de producción.
