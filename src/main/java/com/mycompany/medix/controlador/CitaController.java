package com.mycompany.medix.controlador;
import com.mycompany.medix.modelo.*;
import com.mycompany.medix.dao.MedixDAO;
import java.sql.SQLException;
import java.time.*;
import java.util.List;
public class CitaController {
  private final MedixDAO repo;
  public CitaController(MedixDAO repo){this.repo=repo;}
  public List<Medico> doctors(Usuario s,LocalDate date)throws SQLException{return repo.availableMedicos(s,date);}
  public List<LocalTime> slots(Usuario s,long doctor,LocalDate date)throws SQLException{return repo.availableSlots(s,doctor,date);}
  public void book(Usuario s,long patient,long doctor,LocalDateTime start,int duration)throws SQLException{repo.book(s,patient,doctor,start,duration);}
}