-- Medix schema v1. Back up the target database before applying this migration.
-- Run against an empty Medix database. No existing data is deleted.
CREATE DATABASE IF NOT EXISTS medix CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE medix;

CREATE TABLE IF NOT EXISTS patients (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  dni VARCHAR(24) NOT NULL UNIQUE,
  birth_date DATE NULL,
  phone VARCHAR(40) NULL,
  email VARCHAR(254) NULL,
  address VARCHAR(255) NULL,
  sex VARCHAR(24) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS doctors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  display_name VARCHAR(201) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS workers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  display_name VARCHAR(201) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS app_users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(80) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role ENUM('PATIENT','CUSTOMER_SERVICE','DOCTOR','MANAGER') NOT NULL,
  patient_id BIGINT NULL UNIQUE,
  doctor_id BIGINT NULL UNIQUE,
  worker_id BIGINT NULL UNIQUE,
  must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
  CONSTRAINT fk_user_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  CONSTRAINT fk_user_worker FOREIGN KEY (worker_id) REFERENCES workers(id),
  CONSTRAINT chk_user_role_link CHECK (
    (role='PATIENT' AND patient_id IS NOT NULL AND doctor_id IS NULL AND worker_id IS NULL) OR
    (role='DOCTOR' AND doctor_id IS NOT NULL AND patient_id IS NULL AND worker_id IS NULL) OR
    (role IN ('CUSTOMER_SERVICE','MANAGER') AND worker_id IS NOT NULL AND patient_id IS NULL AND doctor_id IS NULL)
  )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS doctor_working_hours (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  weekday TINYINT NOT NULL COMMENT 'Monday=1 through Sunday=7',
  starts_at TIME NOT NULL,
  ends_at TIME NOT NULL,
  slot_minutes SMALLINT NOT NULL DEFAULT 30,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_hours_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  CONSTRAINT chk_hours_weekday CHECK (weekday BETWEEN 1 AND 7),
  CONSTRAINT chk_hours_interval CHECK (ends_at > starts_at AND slot_minutes > 0),
  INDEX ix_hours_doctor_weekday (doctor_id, weekday, active)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS doctor_unavailability (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  starts_at DATETIME NOT NULL,
  ends_at DATETIME NOT NULL,
  reason VARCHAR(255) NULL,
  CONSTRAINT fk_unavailability_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  CONSTRAINT chk_unavailability_interval CHECK (ends_at > starts_at),
  INDEX ix_unavailability_doctor_time (doctor_id, starts_at, ends_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  starts_at DATETIME NOT NULL,
  duration_minutes SMALLINT NOT NULL,
  status ENUM('BOOKED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'BOOKED',
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
  CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  CONSTRAINT fk_appointment_user FOREIGN KEY (created_by) REFERENCES app_users(id),
  CONSTRAINT chk_appointment_duration CHECK (duration_minutes BETWEEN 5 AND 480),
  INDEX ix_appointment_doctor_time (doctor_id, starts_at, status),
  INDEX ix_appointment_patient_time (patient_id, starts_at)
) ENGINE=InnoDB;
