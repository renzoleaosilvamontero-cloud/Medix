package com.mycompany.medix.modelo;
public enum Rol {
  PATIENT("Paciente"),
  CUSTOMER_SERVICE("Atención al cliente"),
  DOCTOR("Doctor"),
  MANAGER("Gerente");
  private final String nombreVisible;
  Rol(String nombreVisible){this.nombreVisible=nombreVisible;}
  @Override public String toString(){return nombreVisible;}
}