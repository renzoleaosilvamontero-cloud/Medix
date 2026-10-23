package com.mycompany.medix.modelo;
public class Paciente {
  private long id; private String firstName,lastName,dni,phone,email,address;
  public Paciente(long id,String firstName,String lastName,String dni,String phone,String email,String address){this.id=id;this.firstName=firstName;this.lastName=lastName;this.dni=dni;this.phone=phone;this.email=email;this.address=address;}
  public long getId(){return id;} public String getFirstName(){return firstName;} public String getLastName(){return lastName;} public String getDni(){return dni;}
  public String getPhone(){return phone;} public String getEmail(){return email;} public String getAddress(){return address;}
  public String toString(){return firstName+" "+lastName+" (DNI "+dni+")";}
}