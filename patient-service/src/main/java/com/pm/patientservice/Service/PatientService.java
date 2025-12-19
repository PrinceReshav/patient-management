package com.pm.patientservice.Service;


import com.pm.patientservice.Model.Patient;
import com.pm.patientservice.Repository.PatientRepository;
import com.pm.patientservice.dto.PagedPatientResponseDTO;
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// All the business logic is handled here including conversion between DTO and domain models
@Service
public class PatientService {

    // this below block id called dependency injection
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;
    private static final Logger log = LoggerFactory.getLogger(
            PatientService.class);

    public PatientService(PatientRepository patientRepository,
                          BillingServiceGrpcClient billingServiceGrpcClient,
                          KafkaProducer kafkaProducer
                          ) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    // Service layer: converts entities to response DTOs for clients

    @Cacheable(
            value = "patients",
            key = "#page + '-' + #size + '-' + #sort + '-' + #sortField",  // cache keys
            condition = "#searchValue == ''"
    )
    public PagedPatientResponseDTO getPatients(int page, int  size,String sort,String sortField,String searchValue) {

        log.info("[REDIS]: Cache miss - fetching from DB");

        int pageIndex = Math.max(page, 0);  // FIX: never negative

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        // page - 1 to fix Page 0 -> zero index fix
        Pageable pageable = PageRequest.of(page - 1, size,
                 sort.equalsIgnoreCase("desc")
                        ? Sort.by(sortField).descending()
                        : Sort.by(sortField).ascending()

        );

        Page<Patient> patientPage;

        if(searchValue==null ||  searchValue.isBlank()) {
            patientPage = patientRepository.findAll(pageable);
        }
        else{
            patientPage =  patientRepository.findByNameContainingIgnoreCase(searchValue,pageable);
        }

        List<PatientResponseDTO> patientResponseDtos = patientPage.getContent()
                .stream()
                .map(PatientMapper :: toDTO)
                .toList();

        return new PagedPatientResponseDTO(
                patientResponseDtos,
                patientPage.getNumber() + 1,
                patientPage.getSize(),
                patientPage.getTotalPages(),
                (int)patientPage.getTotalElements()
        );

        // Similar to for loop-> It will iterate over each item in the given list(Patients list in this case) a
        // gives us the patient its currently on loop as variable patient here for each patient we are going to call
        // this static toDTO(patient) method with patient as parameter on PatientMapper we created previously
        // through that method we pass patient , the result gets returned to patientResponseDTOs as a list.

        // .map(patient -> PatientMapper.toDTO(patient)).toList(); =  .map(PatientMapper::toDTO).toList();
        // above we used Lambda Expression and Method Reference

    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {

        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email" + "already exists" +
                    patientRequestDTO.getEmail());
        }

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));


        // AFTER PATIENT IS CREATE A WE CREATE A BILLING ACCOUNT
        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(),
                newPatient.getName(), newPatient.getEmail());

        kafkaProducer.sendEvent(newPatient);

        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new PatientNotFoundException("Patient not found with ID : " + id)
        );
        if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),id)) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email" + "already exists" +
                    patientRequestDTO.getEmail());
        }
        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(patient);
    }


    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }

}
