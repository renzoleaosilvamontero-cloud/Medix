# Medix

Aplicación Java Swing de pacientes y citas.

## Clases principales

- `conexion/ConexionBD.java`: conexión a MariaDB/MySQL de XAMPP usando `medix_db`.
- `modelo/`: entidades Usuario, Paciente, Médico, Cita y Rol.
- `dao/MedixDAO.java`: acceso a usuarios, pacientes y citas.
- `controlador/`: autenticación y operaciones de citas.
- `vista/`: calendario y selección de fechas.
- `seguridad/PasswordService.java`: contraseñas temporales y hash BCrypt.
- `database/medix.sql`: esquema y datos locales de demostración.

## Preparar XAMPP/phpMyAdmin

1. Inicia Apache y MySQL en XAMPP.
2. Abre `http://localhost/phpmyadmin` y selecciona la base existente **`medix_db`**.
3. Si contiene información importante, expórtala antes como copia de seguridad.
4. En **Importar**, selecciona `database/medix.sql`. El script usa `USE medix_db`, crea las tablas que falten y agrega datos de demostración; no borra tablas.
5. La conexión por defecto es `jdbc:mariadb://localhost:3306/medix_db`, usuario MySQL `root` y contraseña vacía. Si tu configuración cambia, define `MEDIX_DB_URL`, `MEDIX_DB_USER` y `MEDIX_DB_PASSWORD`.

El usuario MySQL `root` es la cuenta técnica de conexión; es distinto de los usuarios de la aplicación Medix.

## Usuarios de prueba

Contraseña temporal común: **`MedixDemo2026!`**. Se exige cambiarla al iniciar sesión.

| Perfil | Usuario |
|---|---|
| Paciente | `demo_paciente` |
| Doctor | `demo_doctor` |
| Atención al cliente | `demo_atencion` |
| Gerente | `demo_gerente` |

Son credenciales públicas de demostración para uso local. No las uses con información real ni en producción. Sustitúyelas o elimina esas cuentas antes de desplegar.

El gerente puede crear más cuentas desde la aplicación. Si la tabla `app_users` está vacía, se puede crear el gerente inicial con:
```bash
mvn -q -Dexec.mainClass=com.mycompany.medix.BootstrapManager exec:java
```

## Ejecutar

```bash
mvn test
mvn exec:java
```

Los horarios que se insertan son ejemplos de lunes a viernes; reemplázalos por los horarios reales de la clínica. No se insertan citas ficticias.
