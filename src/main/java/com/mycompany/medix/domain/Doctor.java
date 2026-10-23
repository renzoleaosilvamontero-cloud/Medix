package com.mycompany.medix.domain;
public record Doctor(long id, String displayName) {
  @Override public String toString() { return displayName; }
}