-- Base de datos sencilla para importar con phpMyAdmin en XAMPP.
-- Importar en una base vacía llamada MEDIX_DB. No elimina datos.
CREATE DATABASE IF NOT EXISTS MEDIX_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE MEDIX_DB;

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
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS doctors (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  display_name VARCHAR(201) NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS workers (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  display_name VARCHAR(201) NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

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
  FOREIGN KEY (patient_id) REFERENCES patients(id),
  FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  FOREIGN KEY (worker_id) REFERENCES workers(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS doctor_working_hours (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  doctor_id BIGINT NOT NULL,
  weekday TINYINT NOT NULL COMMENT 'Lunes=1, domingo=7',
  starts_at TIME NOT NULL,
  ends_at TIME NOT NULL,
  slot_minutes SMALLINT NOT NULL DEFAULT 30,
  active TINYINT(1) NOT NULL DEFAULT 1,
  FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  INDEX idx_doctor_weekday (doctor_id, weekday)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS doctor_unavailability (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  doctor_id BIGINT NOT NULL,
  starts_at DATETIME NOT NULL,
  ends_at DATETIME NOT NULL,
  reason VARCHAR(255) NULL,
  FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  INDEX idx_doctor_unavailable (doctor_id, starts_at, ends_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointments (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  starts_at DATETIME NOT NULL,
  duration_minutes SMALLINT NOT NULL,
  status ENUM('BOOKED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'BOOKED',
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (patient_id) REFERENCES patients(id),
  FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  FOREIGN KEY (created_by) REFERENCES app_users(id),
  INDEX idx_appointment_doctor_time (doctor_id, starts_at, status),
  INDEX idx_appointment_patient_time (patient_id, starts_at)
) ENGINE=InnoDB;
