package com.pm.patientservice.kafka;

import billing.events.BillingAccountEvent;
import com.pm.patientservice.Model.Patient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KafkaProducer {


    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


// Anytime we want to send an event to kafka topic we need to know some info about patient coming from 'patient object'
public void sendPatientCreatedEvent(Patient patient) {
    PatientEvent event = PatientEvent.newBuilder()
            .setPatientId(patient.getId().toString())
            .setName(patient.getName())
            .setEmail(patient.getEmail())
            .build();

    try {
        kafkaTemplate.send("patient.created", event.toByteArray());
    } catch (Exception e) {
        log.error("Error sending PatientCreated event: {}", event);
    }
}

    public void sendPatientUpdatedEvent(Patient patient) {
        PatientEvent event = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .build();

        try {
            kafkaTemplate.send("patient.updated", event.toByteArray());
        } catch (Exception e) {
            log.error("Error sending PatientUpdated event: {}", event);
        }
    }
    public void sendBillingAccountEvent(String patientId, String name,
                                        String email) {

        BillingAccountEvent event = BillingAccountEvent.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .setEventType("BILLING_ACCOUNT_CREATE_REQUESTED")
                .build();

        try{
            kafkaTemplate.send("billing-account", event.toByteArray());
        } catch (Exception e) {
            log.error("Error sending BillingAccountCreated event: {}", e.getMessage());
        }
    }

}
