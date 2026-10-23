package com.mycompany.medix.modelo;
import java.time.LocalDateTime;
public class Cita {
  private long id; private String patientName,doctorName; private LocalDateTime startsAt; private int durationMinutes; private String status;
  public Cita(long id,String patientName,String doctorName,LocalDateTime startsAt,int durationMinutes,String status){this.id=id;this.patientName=patientName;this.doctorName=doctorName;this.startsAt=startsAt;this.durationMinutes=durationMinutes;this.status=status;}
  public long getId(){return id;} public String getPatientName(){return patientName;} public String getDoctorName(){return doctorName;}
  public LocalDateTime getStartsAt(){return startsAt;} public int getDurationMinutes(){return durationMinutes;} public String getStatus(){return status;}
}