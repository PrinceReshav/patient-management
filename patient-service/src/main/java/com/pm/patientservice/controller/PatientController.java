package com.pm.patientservice.controller;

import com.pm.patientservice.Service.PatientService;
import com.pm.patientservice.dto.PagedPatientResponseDTO;
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;


@RestController
@RequestMapping("/patients") // All the URL requests ending with patients will be handles by this controller
@Tag(name = "PATIENT", description = "API for managing patients")  // SWAGGER-UI
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // This method is part of a REST controller and handles HTTP GET requests to fetch a list of patients.
    @GetMapping
    @Operation(summary = "GET Patients" ) // SWAGGER-UI
    public ResponseEntity<PagedPatientResponseDTO> getPatients(
            @RequestParam(defaultValue = "1") int page, // 1 page
            @RequestParam(defaultValue = "10") int size, // 10 patients only
            @RequestParam(defaultValue = "asc") String sort, // sorting in ascending
            @RequestParam(defaultValue = "name") String sortField, // Sorting based on name
            @RequestParam(defaultValue = "") String searchValue   // keys to search ex: Pri from Prince
    ) {

        // Call the service layer to retrieve a list of patient DTOs (Data Transfer Objects).
        // These DTOs contain the patient data formatted for the API response.
        PagedPatientResponseDTO patients = patientService.getPatients(page,size,sort,sortField,searchValue);

        // Wrap the list of patients in a ResponseEntity with HTTP status 200 OK.
        // This allows you to customize the HTTP response (status code, headers, body).
        return ResponseEntity.ok().body(patients);
    }

    @PostMapping
    @Operation(summary = "Create a new patient") // SWAGGER-UI
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class})
        @RequestBody PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.createPatient(patientRequestDTO);

        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing patient")  // SWAGGER-UI
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable UUID id,@Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, patientRequestDTO);

        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a patient")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id ) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }


}