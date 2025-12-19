package com.pm.patientservice.Repository;

import java.util.UUID;
import com.pm.patientservice.Model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Page<Patient> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
