-- Medix para XAMPP (MariaDB/MySQL) y phpMyAdmin.
-- Selecciona/importa en la base existente medix_db. No elimina tablas ni datos.
USE medix_db;

CREATE TABLE IF NOT EXISTS patients (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  dni VARCHAR(24) NOT NULL UNIQUE,
  birth_date DATE NULL,
  phone VARCHAR(40) NULL,
  email VARCHAR(254) NULL,
  address VARCHAR(255) NULL,
  sex VARCHAR(24) NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS doctors (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  display_name VARCHAR(201) NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS workers (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  display_name VARCHAR(201) NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(80) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role ENUM('PATIENT','CUSTOMER_SERVICE','DOCTOR','MANAGER') NOT NULL,
  patient_id BIGINT NULL UNIQUE,
  doctor_id BIGINT NULL UNIQUE,
  worker_id BIGINT NULL UNIQUE,
  must_change_password TINYINT(1) NOT NULL DEFAULT 1,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_app_users_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
  CONSTRAINT fk_app_users_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  CONSTRAINT fk_app_users_worker FOREIGN KEY (worker_id) REFERENCES workers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS doctor_working_hours (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  doctor_id BIGINT NOT NULL,
  weekday TINYINT NOT NULL COMMENT 'Lunes=1, domingo=7',
  starts_at TIME NOT NULL,
  ends_at TIME NOT NULL,
  slot_minutes SMALLINT NOT NULL DEFAULT 30,
  active TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_dwh_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  INDEX idx_doctor_weekday (doctor_id, weekday)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS doctor_unavailability (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  doctor_id BIGINT NOT NULL,
  starts_at DATETIME NOT NULL,
  ends_at DATETIME NOT NULL,
  reason VARCHAR(255) NULL,
  CONSTRAINT fk_unavailable_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  INDEX idx_doctor_unavailable (doctor_id, starts_at, ends_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS appointments (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  starts_at DATETIME NOT NULL,
  duration_minutes SMALLINT NOT NULL,
  status ENUM('BOOKED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'BOOKED',
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
  CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  CONSTRAINT fk_appointments_creator FOREIGN KEY (created_by) REFERENCES app_users(id),
  INDEX idx_appointment_doctor_time (doctor_id, starts_at, status),
  INDEX idx_appointment_patient_time (patient_id, starts_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Datos ficticios de demostración; las inserciones de personas evitan duplicados.
INSERT INTO patients (first_name,last_name,dni,birth_date,phone,email,address,sex)
VALUES
 ('Ana','Torres','90000001','1998-04-12','900111222','ana.torres@example.test','Lima','F'),
 ('Luis','Ramos','90000002','1987-09-30','900333444','luis.ramos@example.test','Lima','M')
ON DUPLICATE KEY UPDATE first_name=VALUES(first_name), last_name=VALUES(last_name);

INSERT INTO doctors (display_name,active)
SELECT 'Dra. Valeria Paredes',1
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE display_name='Dra. Valeria Paredes');
INSERT INTO doctors (display_name,active)
SELECT 'Dr. Mateo Salazar',1
WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE display_name='Dr. Mateo Salazar');

INSERT INTO workers (display_name,active)
SELECT 'Personal de atención Demo',1
WHERE NOT EXISTS (SELECT 1 FROM workers WHERE display_name='Personal de atención Demo');
INSERT INTO workers (display_name,active)
SELECT 'Gerente Demo',1
WHERE NOT EXISTS (SELECT 1 FROM workers WHERE display_name='Gerente Demo');

-- Horario de demostración: lunes a viernes 09:00-13:00 y 14:00-17:00.
-- Los datos son ejemplos; reemplázalos con los horarios reales de la clínica.
INSERT INTO doctor_working_hours (doctor_id,weekday,starts_at,ends_at,slot_minutes,active)
SELECT d.id,days.day_num,'09:00:00','13:00:00',30,1
FROM doctors d
JOIN (SELECT 1 AS day_num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) days
WHERE d.display_name IN ('Dra. Valeria Paredes','Dr. Mateo Salazar')
AND NOT EXISTS (SELECT 1 FROM doctor_working_hours h WHERE h.doctor_id=d.id AND h.weekday=days.day_num AND h.starts_at='09:00:00');
INSERT INTO doctor_working_hours (doctor_id,weekday,starts_at,ends_at,slot_minutes,active)
SELECT d.id,days.day_num,'14:00:00','17:00:00',30,1
FROM doctors d
JOIN (SELECT 1 AS day_num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) days
WHERE d.display_name IN ('Dra. Valeria Paredes','Dr. Mateo Salazar')
AND NOT EXISTS (SELECT 1 FROM doctor_working_hours h WHERE h.doctor_id=d.id AND h.weekday=days.day_num AND h.starts_at='14:00:00');

-- Cuentas de demostración locales. Contraseña inicial común: MedixDemo2026!
-- El hash es BCrypt ($2a$); todas obligan a cambiar la contraseña al entrar.
-- Son cuentas públicas de prueba: NO reutilices estos accesos ni los uses en producción.
INSERT INTO app_users (username,password_hash,role,patient_id,doctor_id,worker_id,must_change_password)
SELECT 'demo_paciente','$2a$12$c9wi2yrOCQ/jHx8OIhcEheQ8kVKJS1X4D1Q7SBEU9JMi73fXW2Vrm','PATIENT',p.id,NULL,NULL,1
FROM patients p
WHERE p.dni='90000001'
AND NOT EXISTS (SELECT 1 FROM app_users u WHERE u.username='demo_paciente');

INSERT INTO app_users (username,password_hash,role,patient_id,doctor_id,worker_id,must_change_password)
SELECT 'demo_doctor','$2a$12$c9wi2yrOCQ/jHx8OIhcEheQ8kVKJS1X4D1Q7SBEU9JMi73fXW2Vrm','DOCTOR',NULL,d.id,NULL,1
FROM doctors d
WHERE d.display_name='Dra. Valeria Paredes'
AND NOT EXISTS (SELECT 1 FROM app_users u WHERE u.username='demo_doctor');

INSERT INTO app_users (username,password_hash,role,patient_id,doctor_id,worker_id,must_change_password)
SELECT 'demo_atencion','$2a$12$c9wi2yrOCQ/jHx8OIhcEheQ8kVKJS1X4D1Q7SBEU9JMi73fXW2Vrm','CUSTOMER_SERVICE',NULL,NULL,w.id,1
FROM workers w
WHERE w.display_name='Personal de atención Demo'
AND NOT EXISTS (SELECT 1 FROM app_users u WHERE u.username='demo_atencion');

INSERT INTO app_users (username,password_hash,role,patient_id,doctor_id,worker_id,must_change_password)
SELECT 'demo_gerente','$2a$12$c9wi2yrOCQ/jHx8OIhcEheQ8kVKJS1X4D1Q7SBEU9JMi73fXW2Vrm','MANAGER',NULL,NULL,w.id,1
FROM workers w
WHERE w.display_name='Gerente Demo'
AND NOT EXISTS (SELECT 1 FROM app_users u WHERE u.username='demo_gerente');
