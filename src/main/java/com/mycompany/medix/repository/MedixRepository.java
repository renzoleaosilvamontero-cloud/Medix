package com.mycompany.medix.repository;

import com.mycompany.medix.config.Database;
import com.mycompany.medix.domain.*;
import com.mycompany.medix.security.PasswordService;
import java.sql.*;
import java.time.*;
import java.util.*;

public class MedixRepository {
  public Optional<Session> authenticate(String username, char[] password) throws SQLException {
    String sql = "SELECT id,username,password_hash,role,patient_id,doctor_id,must_change_password FROM app_users WHERE username=? AND active=1";
    try (Connection c=Database.connect(); PreparedStatement p=c.prepareStatement(sql)) {
      p.setString(1, username.trim());
      try (ResultSet r=p.executeQuery()) {
        if (!r.next() || !PasswordService.verify(password,r.getString("password_hash"))) return Optional.empty();
        long patient=r.getLong("patient_id"); Long patientId=r.wasNull()?null:patient;
        long doctor=r.getLong("doctor_id"); Long doctorId=r.wasNull()?null:doctor;
        return Optional.of(new Session(r.getLong("id"),r.getString("username"),Role.valueOf(r.getString("role")),patientId,doctorId,r.getBoolean("must_change_password")));
      }
    }
  }
  public void changePassword(Session session, char[] oldPassword, char[] newPassword) throws SQLException {
    try (Connection c=Database.connect(); PreparedStatement q=c.prepareStatement("SELECT password_hash FROM app_users WHERE id=? AND active=1")) {
      q.setLong(1,session.userId());
      try(ResultSet r=q.executeQuery()) {
        if(!r.next() || !PasswordService.verify(oldPassword,r.getString(1))) throw new SecurityException("Credenciales inválidas.");
      }
    }
    try(Connection c=Database.connect(); PreparedStatement p=c.prepareStatement("UPDATE app_users SET password_hash=?,must_change_password=0 WHERE id=?")) {
      p.setString(1,PasswordService.hash(newPassword)); p.setLong(2,session.userId()); p.executeUpdate();
    }
  }
  public String createInternalAccount(Session actor,String username,String displayName,Role role) throws SQLException {
    require(actor,Role.MANAGER);
    if(role!=Role.CUSTOMER_SERVICE && role!=Role.DOCTOR && role!=Role.MANAGER)throw new IllegalArgumentException("Perfil interno inválido.");
    String temporary=PasswordService.temporaryPassword();
    try(Connection c=Database.connect()){
      c.setAutoCommit(false);
      try{
        String linkColumn=role==Role.DOCTOR?"doctor_id":"worker_id";
        String linkedTable=role==Role.DOCTOR?"doctors":"workers";
        long linkedId;
        try(PreparedStatement p=c.prepareStatement("INSERT INTO "+linkedTable+"(display_name) VALUES(?)",Statement.RETURN_GENERATED_KEYS)){
          p.setString(1,displayName.trim());p.executeUpdate();
          try(ResultSet keys=p.getGeneratedKeys()){if(!keys.next())throw new SQLException("No se creó el perfil.");linkedId=keys.getLong(1);}
        }
        try(PreparedStatement p=c.prepareStatement("INSERT INTO app_users(username,password_hash,role,"+linkColumn+",must_change_password) VALUES(?,?,?, ?,1)")){
          p.setString(1,username.trim());p.setString(2,PasswordService.hash(temporary.toCharArray()));p.setString(3,role.name());p.setLong(4,linkedId);p.executeUpdate();
        }
        c.commit();return temporary;
      }catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;if(e instanceof RuntimeException re)throw re;throw new SQLException(e);}
      finally{c.setAutoCommit(true);}
    }
  }
  public String createPatientAccount(Session actor,String username,String firstName,String lastName,String dni) throws SQLException {
    require(actor,Role.MANAGER);
    String temporary=PasswordService.temporaryPassword();
    try(Connection c=Database.connect()){
      c.setAutoCommit(false);
      try{
        long patientId;
        try(PreparedStatement p=c.prepareStatement("INSERT INTO patients(first_name,last_name,dni) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){
          p.setString(1,firstName.trim());p.setString(2,lastName.trim());p.setString(3,dni.trim());p.executeUpdate();
          try(ResultSet keys=p.getGeneratedKeys()){if(!keys.next())throw new SQLException("No se creó el paciente.");patientId=keys.getLong(1);}
        }
        try(PreparedStatement p=c.prepareStatement("INSERT INTO app_users(username,password_hash,role,patient_id,must_change_password) VALUES(?,?,'PATIENT',?,1)")){
          p.setString(1,username.trim());p.setString(2,PasswordService.hash(temporary.toCharArray()));p.setLong(3,patientId);p.executeUpdate();
        }
        c.commit();return temporary;
      }catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;if(e instanceof RuntimeException re)throw re;throw new SQLException(e);}
      finally{c.setAutoCommit(true);}
    }
  }
  public List<Patient> patients(Session s) throws SQLException {
    require(s,Role.CUSTOMER_SERVICE,Role.DOCTOR,Role.MANAGER,Role.PATIENT);
    String sql="SELECT id,first_name,last_name,dni,phone,email,address FROM patients WHERE (? IS NULL OR id=?) ORDER BY last_name,first_name";
    try(Connection c=Database.connect(); PreparedStatement p=c.prepareStatement(sql)) {
      if(s.role()==Role.PATIENT){p.setLong(1,s.patientId());p.setLong(2,s.patientId());} else {p.setNull(1,Types.BIGINT);p.setNull(2,Types.BIGINT);}
      try(ResultSet r=p.executeQuery()){List<Patient> out=new ArrayList<>();while(r.next())out.add(new Patient(r.getLong(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getString(7)));return out;}
    }
  }
  public void updatePatient(Session s, Patient p) throws SQLException {
    require(s,Role.CUSTOMER_SERVICE,Role.PATIENT,Role.MANAGER);
    if(s.role()==Role.PATIENT && !Objects.equals(s.patientId(),p.id())) throw new SecurityException("Solo puedes modificar tu perfil.");
    String sql=s.role()==Role.CUSTOMER_SERVICE
      ? "UPDATE patients SET phone=?,email=?,address=? WHERE id=?"
      : "UPDATE patients SET first_name=?,last_name=?,dni=?,phone=?,email=?,address=? WHERE id=?";
    try(Connection c=Database.connect();PreparedStatement q=c.prepareStatement(sql)){
      int i=1;
      if(s.role()!=Role.CUSTOMER_SERVICE){q.setString(i++,p.firstName());q.setString(i++,p.lastName());q.setString(i++,p.dni());}
      q.setString(i++,p.phone());q.setString(i++,p.email());q.setString(i++,p.address());q.setLong(i,p.id());
      if(q.executeUpdate()!=1)throw new SQLException("Paciente no encontrado.");
    }
  }
  public List<AppointmentInfo> appointments(Session s) throws SQLException {
    require(s,Role.PATIENT,Role.DOCTOR,Role.CUSTOMER_SERVICE,Role.MANAGER);
    String filter=s.role()==Role.PATIENT?" WHERE a.patient_id=?":s.role()==Role.DOCTOR?" WHERE a.doctor_id=?":"";
    String sql="SELECT a.id,p.first_name,p.last_name,d.display_name,a.starts_at,a.duration_minutes,a.status FROM appointments a JOIN patients p ON p.id=a.patient_id JOIN doctors d ON d.id=a.doctor_id"+filter+" ORDER BY a.starts_at DESC";
    try(Connection c=Database.connect();PreparedStatement q=c.prepareStatement(sql)){
      if(s.role()==Role.PATIENT)q.setLong(1,s.patientId());else if(s.role()==Role.DOCTOR)q.setLong(1,s.doctorId());
      try(ResultSet rs=q.executeQuery()){List<AppointmentInfo> out=new ArrayList<>();while(rs.next())out.add(new AppointmentInfo(rs.getLong(1),rs.getString(2)+" "+rs.getString(3),rs.getString(4),rs.getTimestamp(5).toLocalDateTime(),rs.getInt(6),rs.getString(7)));return out;}
    }
  }
  public List<Doctor> availableDoctors(Session s, LocalDate date) throws SQLException {
    require(s,Role.PATIENT,Role.CUSTOMER_SERVICE,Role.MANAGER);
    List<Doctor> result=new ArrayList<>();
    String sql="SELECT d.id,d.display_name FROM doctors d WHERE d.active=1 ORDER BY d.display_name";
    try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
      while(r.next()){Doctor d=new Doctor(r.getLong(1),r.getString(2));if(!slots(c,d.id(),date).isEmpty())result.add(d);}
    }
    return result;
  }
  public List<LocalTime> availableSlots(Session s,long doctorId,LocalDate date) throws SQLException {
    require(s,Role.PATIENT,Role.CUSTOMER_SERVICE,Role.MANAGER);
    try(Connection c=Database.connect()){return slots(c,doctorId,date);}
  }
  private List<LocalTime> slots(Connection c,long doctorId,LocalDate date) throws SQLException {
    if(date.isBefore(LocalDate.now()))return List.of();
    DayOfWeek dow=date.getDayOfWeek(); int weekday=dow.getValue();
    List<LocalTime> out=new ArrayList<>();
    String hours="SELECT starts_at,ends_at,slot_minutes FROM doctor_working_hours WHERE doctor_id=? AND weekday=? AND active=1 ORDER BY starts_at";
    try(PreparedStatement p=c.prepareStatement(hours)){p.setLong(1,doctorId);p.setInt(2,weekday);try(ResultSet r=p.executeQuery()){
      while(r.next()){
        LocalTime start=r.getTime(1).toLocalTime(), end=r.getTime(2).toLocalTime(); int minutes=r.getInt(3);
        for(LocalTime t=start;!t.plusMinutes(minutes).isAfter(end);t=t.plusMinutes(minutes)){
          LocalDateTime a=LocalDateTime.of(date,t), b=a.plusMinutes(minutes);
          if(!a.isAfter(LocalDateTime.now()) || unavailable(c,doctorId,a,b) || overlaps(c,doctorId,a,b))continue;
          out.add(t);
        }
      }
    }}
    return out;
  }
  private boolean unavailable(Connection c,long doctor,LocalDateTime a,LocalDateTime b)throws SQLException{
    try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM doctor_unavailability WHERE doctor_id=? AND starts_at<? AND ends_at>? LIMIT 1")){
      p.setLong(1,doctor);p.setTimestamp(2,Timestamp.valueOf(b));p.setTimestamp(3,Timestamp.valueOf(a));try(ResultSet r=p.executeQuery()){return r.next();}
    }
  }
  private boolean overlaps(Connection c,long doctor,LocalDateTime a,LocalDateTime b)throws SQLException{
    try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM appointments WHERE doctor_id=? AND status='BOOKED' AND starts_at<? AND DATE_ADD(starts_at,INTERVAL duration_minutes MINUTE)>? LIMIT 1")){
      p.setLong(1,doctor);p.setTimestamp(2,Timestamp.valueOf(b));p.setTimestamp(3,Timestamp.valueOf(a));try(ResultSet r=p.executeQuery()){return r.next();}
    }
  }
  public void book(Session s,long patientId,long doctorId,LocalDateTime start,int duration) throws SQLException {
    require(s,Role.PATIENT,Role.CUSTOMER_SERVICE,Role.MANAGER);
    if(s.role()==Role.PATIENT && !Objects.equals(s.patientId(),patientId))throw new SecurityException("Solo puedes reservar para tu cuenta.");
    if(start.isBefore(LocalDateTime.now()) || duration<5 || duration>480)throw new IllegalArgumentException("Fecha u duración inválida.");
    try(Connection c=Database.connect()){
      c.setAutoCommit(false);
      try{
        try(PreparedStatement lock=c.prepareStatement("SELECT id FROM doctors WHERE id=? AND active=1 FOR UPDATE")){lock.setLong(1,doctorId);try(ResultSet r=lock.executeQuery()){if(!r.next())throw new SQLException("Doctor no disponible.");}}
        if(!isInsideHours(c,doctorId,start,duration) || unavailable(c,doctorId,start,start.plusMinutes(duration)) || overlaps(c,doctorId,start,start.plusMinutes(duration)))throw new SQLException("El horario ya no está disponible.");
        try(PreparedStatement p=c.prepareStatement("INSERT INTO appointments(patient_id,doctor_id,starts_at,duration_minutes,created_by) VALUES(?,?,?,?,?)")){
          p.setLong(1,patientId);p.setLong(2,doctorId);p.setTimestamp(3,Timestamp.valueOf(start));p.setInt(4,duration);p.setLong(5,s.userId());p.executeUpdate();
        }
        c.commit();
      }catch(Exception e){c.rollback();if(e instanceof SQLException se)throw se;if(e instanceof RuntimeException re)throw re;throw new SQLException(e);}
      finally{c.setAutoCommit(true);}
    }
  }
  private boolean isInsideHours(Connection c,long doctor,LocalDateTime start,int duration)throws SQLException{
    String sql="SELECT 1 FROM doctor_working_hours WHERE doctor_id=? AND weekday=? AND active=1 AND starts_at<=? AND ends_at>=? AND MOD(TIME_TO_SEC(TIME(?))-TIME_TO_SEC(starts_at),slot_minutes*60)=0 LIMIT 1";
    try(PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,doctor);p.setInt(2,start.getDayOfWeek().getValue());p.setTime(3,Time.valueOf(start.toLocalTime()));p.setTime(4,Time.valueOf(start.toLocalTime().plusMinutes(duration)));p.setTime(5,Time.valueOf(start.toLocalTime()));try(ResultSet r=p.executeQuery()){return r.next();}}
  }
  private static void require(Session s,Role... roles){for(Role r:roles)if(s.role()==r)return;throw new SecurityException("Tu perfil no tiene permiso para esta operación.");}
}