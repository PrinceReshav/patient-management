package com.pm.notificationservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.notificationservice.service.EmailService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class NotificationKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaConsumer.class);
    private final EmailService emailService;

    public NotificationKafkaConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = {"patient.created"},
            groupId = "notification-service"
    )
    public void consumePatientEvent(ConsumerRecord<byte[], byte[]> record) {

        try {
            byte[] payload = record.value();

            PatientEvent event = PatientEvent.parseFrom(payload);

            log.info("Received Patient Event for notification: {}", event);

            emailService.sendEmail(
                    event.getEmail(),
                    "Welcome to Our Clinic",
                    """
                    Hello %s,
    
                    Your patient profile has been successfully created.
                    Patient ID: %s
    
                    Thank you,
                    Clinic Team
                    """.formatted(event.getName(), event.getPatientId())
            );

        } catch (InvalidProtocolBufferException e) {
            log.error("❌ Invalid AppointmentEvent protobuf", e);
            throw new RuntimeException(e); // ✅ triggers DLT
        }
        catch (Exception e) {
            // 🔴 Infrastructure / email / runtime failure
            log.error("❌ Error while processing notification", e);
            throw e; // retryable
        }
    }

}
