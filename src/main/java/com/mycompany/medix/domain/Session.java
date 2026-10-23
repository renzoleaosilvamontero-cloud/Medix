package com.mycompany.medix.domain;
public record Session(long userId, String username, Role role, Long patientId, Long doctorId, boolean mustChangePassword) {}