# Medix

Aplicación Java Swing de pacientes y citas, organizada por paquetes sencillos para practicar Programación Orientada a Objetos.

## Carpetas y clases

- `conexion/ConexionBD.java`: conexión a MySQL/MariaDB de XAMPP.
- `modelo/`: clases con atributos privados, constructores, getters y `toString` para Usuario, Paciente, Médico y Cita.
- `dao/MedixDAO.java`: consultas SQL de usuarios, pacientes y citas.
- `controlador/`: inicio de sesión y operaciones de citas.
- `vista/`: pantallas Swing y calendario.
- `seguridad/PasswordService.java`: generación de contraseñas temporales y hash bcrypt.
- `database/medix.sql`: tablas sencillas que necesita el programa.

## Preparar XAMPP

1. Abre XAMPP y enciende **MySQL**.
2. Abre phpMyAdmin en `http://localhost/phpmyadmin`.
3. Importa `database/medix.sql` para crear `medix` y sus tablas. Hazlo en una base vacía; el script no borra información.
4. La conexión usa por defecto `jdbc:mariadb://localhost:3306/medix`, usuario `root` y contraseña vacía, como en una instalación local típica de XAMPP. Si tu configuración es distinta, define `MEDIX_DB_URL`, `MEDIX_DB_USER` y `MEDIX_DB_PASSWORD`.

La configuración de usuario root sin contraseña es solo para XAMPP local de desarrollo.

## Primera cuenta y ejecución

En una terminal abierta en la carpeta del proyecto, crea el primer gerente:

```bash
mvn -q -Dexec.mainClass=com.mycompany.medix.BootstrapManager exec:java
```

Escribe el nombre de usuario y el nombre del gerente. La aplicación genera una contraseña temporal y la muestra una sola vez. Se solicitará cambiarla en el primer acceso.

Inicia la aplicación:

```bash
mvn exec:java
```

El gerente puede crear cuentas de pacientes, atención al cliente, doctores y otros gerentes. Cada cuenta recibe una contraseña temporal aleatoria, que se revela una sola vez.

## Funciones incluidas

- Inicio y cierre de sesión con menú según perfil.
- Contraseñas guardadas como hash; nadie puede consultar la contraseña guardada.
- El paciente puede cambiar su propia contraseña y mostrar los caracteres mientras los escribe.
- Atención al cliente puede editar datos del paciente excepto nombre, apellido y DNI. La restricción se aplica también al guardar.
- Calendario para seleccionar la fecha y actualización de doctores y horarios libres.
- Validación de disponibilidad al guardar citas para evitar cruces de horarios.

Los horarios reales se cargan en la tabla `doctor_working_hours`; los bloqueos se guardan en `doctor_unavailability`. No se agregan turnos ficticios.

## Pruebas

Ejecuta `mvn test`. La prueba automatizada cubre generación y hash de contraseñas.
