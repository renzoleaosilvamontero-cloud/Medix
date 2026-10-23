package com.mycompany.medix.modelo;
public class Usuario {
  private long userId; private String username; private Rol role; private Long patientId,doctorId; private boolean mustChangePassword;
  public Usuario(long userId,String username,Rol role,Long patientId,Long doctorId,boolean mustChangePassword){this.userId=userId;this.username=username;this.role=role;this.patientId=patientId;this.doctorId=doctorId;this.mustChangePassword=mustChangePassword;}
  public long getUserId(){return userId;} public String getUsername(){return username;} public Rol getRole(){return role;}
  public Long getPatientId(){return patientId;} public Long getDoctorId(){return doctorId;} public boolean isMustChangePassword(){return mustChangePassword;}
}