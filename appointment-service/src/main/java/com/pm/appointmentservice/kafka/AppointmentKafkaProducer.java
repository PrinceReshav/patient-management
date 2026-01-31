package com.pm.appointmentservice.kafka;


import appointment.events.AppointmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
@Service
public class AppointmentKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentKafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public AppointmentKafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAppointmentCreated(AppointmentEvent event) {
        kafkaTemplate.send(
                "appointment.created",
                event.getAppointmentId(),
                event.toByteArray()
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("❌ Failed to publish AppointmentEvent", ex);
            } else {
                log.info("✅ AppointmentEvent published. partition={}, offset={}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

}
