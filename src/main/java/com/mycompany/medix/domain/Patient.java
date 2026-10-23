package com.mycompany.medix.domain;
public record Patient(long id, String firstName, String lastName, String dni, String phone, String email, String address) {
  @Override public String toString() { return firstName + " " + lastName + " (DNI " + dni + ")"; }
}