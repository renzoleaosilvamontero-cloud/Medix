package com.mycompany.medix.domain;
import java.time.LocalDateTime;
public record AppointmentInfo(long id,String patientName,String doctorName,LocalDateTime startsAt,int durationMinutes,String status) {}
