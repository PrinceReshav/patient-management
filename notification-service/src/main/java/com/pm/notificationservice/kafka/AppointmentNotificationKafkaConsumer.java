package com.pm.notificationservice.kafka;


import appointment.events.AppointmentEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.notificationservice.service.EmailService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AppointmentNotificationKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(AppointmentNotificationKafkaConsumer.class);

    private final EmailService emailService;

    public AppointmentNotificationKafkaConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = "appointment.created",
            groupId = "notification-service"
    )
    public void consumeAppointmentEvent(ConsumerRecord<byte[], byte[]> record) {

        try {
            AppointmentEvent event =
                    AppointmentEvent.parseFrom(record.value());

            log.info("📅 Appointment event received: {}", event);

            // 👇 Intentionally ignore doctorName from event
            String doctorName = "Dr. Do Little";

            emailService.sendEmail(
                    event.getPatientEmail(),
                    "Appointment Confirmed",
                    """
                    Hello %s,

                    Your appointment has been confirmed.

                    🧾 Appointment ID: %s
                    📅 Date: %s
                    ⏰ Time: %s
                    👨‍⚕️ Doctor: %s

                    Thank you,
                    Clinic Team
                    """.formatted(
                            event.getPatientName(),
                            event.getAppointmentId(),
                            event.getAppointmentDate(),
                            event.getAppointmentTime(),
                            doctorName
                    )
            );

        }
        catch (InvalidProtocolBufferException e) {
            log.error("❌ Invalid PatientEvent protobuf", e);
            throw new RuntimeException(e); // ✅ triggers DLT
        }

        catch (Exception e) {
            log.error("❌ Error processing appointment notification", e);
            throw e; // retryable
        }
    }
}
