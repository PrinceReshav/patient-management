package com.pm.appointmentservice.service;

import appointment.events.AppointmentEvent;
import com.pm.appointmentservice.dto.AppointmentRequestDto;
import com.pm.appointmentservice.dto.AppointmentResponseDto;
import com.pm.appointmentservice.entity.Appointment;
import com.pm.appointmentservice.entity.CachedPatient;
import com.pm.appointmentservice.exception.InvalidTimeRangeException;
import com.pm.appointmentservice.kafka.AppointmentKafkaProducer;
import com.pm.appointmentservice.repository.AppointmentRepository;
import com.pm.appointmentservice.repository.CachedPatientRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.pm.appointmentservice.exception.PatientNotFoundException;
import com.pm.appointmentservice.exception.SlotUnavailableException;


import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final CachedPatientRepository cachedPatientRepository;
    private final AppointmentKafkaProducer appointmentKafkaProducer;

  public AppointmentService(AppointmentRepository appointmentRepository,
      CachedPatientRepository cachedPatientRepository, AppointmentKafkaProducer appointmentKafkaProducer) {
    this.appointmentRepository = appointmentRepository;
    this.cachedPatientRepository = cachedPatientRepository;
    this.appointmentKafkaProducer = appointmentKafkaProducer;
  }

  public List<AppointmentResponseDto> getAppointmentsByDateRange(
      LocalDateTime from, LocalDateTime to
  ) {
    return appointmentRepository.findByStartTimeBetween(from, to).stream()
        .map(appointment -> {

          String name = cachedPatientRepository
              .findById(appointment.getPatientId())
              .map(CachedPatient::getFullName)
              .orElse("Unknown");

          AppointmentResponseDto appointmentResponseDto
              = new AppointmentResponseDto();

          appointmentResponseDto.setId(appointment.getId());
          appointmentResponseDto.setPatientId(appointment.getPatientId());
          appointmentResponseDto.setStartTime(appointment.getStartTime());
          appointmentResponseDto.setEndTime(appointment.getEndTime());
          appointmentResponseDto.setReason(appointment.getReason());
          appointmentResponseDto.setVersion(appointment.getVersion());
          appointmentResponseDto.setPatientName(name);

          return appointmentResponseDto;
        }).toList();
  }

    public List<AppointmentResponseDto> getAppointmentsForPatient(UUID patientId) {

        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(appointment -> {

                    String name = cachedPatientRepository
                            .findById(patientId)
                            .map(CachedPatient::getFullName)
                            .orElse("Unknown");

                    return new AppointmentResponseDto(
                            appointment.getId(),
                            appointment.getPatientId(),
                            name,
                            appointment.getStartTime(),
                            appointment.getEndTime(),
                            appointment.getReason(),
                            appointment.getVersion()
                    );
                })
                .toList();
    }

    public AppointmentResponseDto createAppointment(AppointmentRequestDto request) {

        // 0. Validate time range FIRST
        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().isEqual(request.getStartTime())) {
            throw new InvalidTimeRangeException("End time must be after start time");
        }

        // 1. Validate patient exists
        CachedPatient patient = cachedPatientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not registered. Please create patient first."));


        // 2. Prevent overlapping appointments
        boolean conflict = appointmentRepository
                .existsByStartTimeLessThanAndEndTimeGreaterThan(
                        request.getEndTime(),
                        request.getStartTime()
                );

        if (conflict) {
            throw new SlotUnavailableException("Time slot already booked. Please choose another time.");
        }

        // 3. Save appointment
        Appointment appointment = new Appointment(
                request.getPatientId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getReason()
        );

        Appointment saved = appointmentRepository.save(appointment);

        // 4. Publish Kafka event
        AppointmentEvent event = AppointmentEvent.newBuilder()
                .setAppointmentId(saved.getId().toString())
                .setPatientId(patient.getId().toString())
                .setPatientName(patient.getFullName())
                .setPatientEmail(patient.getEmail())
                .setAppointmentDate(saved.getStartTime().toLocalDate().toString())
                .setAppointmentTime(saved.getStartTime().toLocalTime().toString())
                .setDoctorName("Dr. Do Little") // notification service ignores anyway
                .setEventType("CREATED")
                .build();

        appointmentKafkaProducer.publishAppointmentCreated(event);

        // 5. Return response
        return new AppointmentResponseDto(
                saved.getId(),
                saved.getPatientId(),
                patient.getFullName(),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getReason(),
                saved.getVersion()
        );
    }


}
