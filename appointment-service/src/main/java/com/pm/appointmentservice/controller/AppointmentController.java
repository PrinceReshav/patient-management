package com.pm.appointmentservice.controller;

import com.pm.appointmentservice.dto.AppointmentRequestDto;
import com.pm.appointmentservice.dto.AppointmentResponseDto;
import com.pm.appointmentservice.service.AppointmentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

  private final AppointmentService appointmentService;

  public AppointmentController(AppointmentService appointmentService) {
    this.appointmentService = appointmentService;
  }

  @GetMapping
  public List<AppointmentResponseDto> getAppointmentsByDateRange(
      @RequestParam LocalDateTime from,
      @RequestParam LocalDateTime to
  ){
    return appointmentService.getAppointmentsByDateRange(from, to);
  }

    @PostMapping
    public AppointmentResponseDto createAppointment(
            @Valid @RequestBody AppointmentRequestDto request
    ) {
        return appointmentService.createAppointment(request);
    }

    @GetMapping("/patient/{patientId}")
    public List<AppointmentResponseDto> getAppointmentsForPatient(@PathVariable UUID patientId) {
        return appointmentService.getAppointmentsForPatient(patientId);
    }


}
