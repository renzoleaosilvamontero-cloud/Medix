package com.mycompany.medix.modelo;
public class Medico {
  private long id; private String displayName;
  public Medico(long id,String displayName){this.id=id;this.displayName=displayName;}
  public long getId(){return id;} public String getDisplayName(){return displayName;}
  public String toString(){return displayName;}
}