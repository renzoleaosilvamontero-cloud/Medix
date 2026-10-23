package com.mycompany.medix.service;
import com.mycompany.medix.domain.*;
import com.mycompany.medix.repository.MedixRepository;
import java.sql.SQLException;
import java.time.*;
import java.util.List;
public class AppointmentService {
  private final MedixRepository repo;
  public AppointmentService(MedixRepository repo){this.repo=repo;}
  public List<Doctor> doctors(Session s,LocalDate date)throws SQLException{return repo.availableDoctors(s,date);}
  public List<LocalTime> slots(Session s,long doctor,LocalDate date)throws SQLException{return repo.availableSlots(s,doctor,date);}
  public void book(Session s,long patient,long doctor,LocalDateTime start,int duration)throws SQLException{repo.book(s,patient,doctor,start,duration);}
}