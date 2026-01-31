package com.pm.notificationservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.kafka.core.KafkaTemplate;


@RestController
@RequestMapping("/test/kafka")
public class KafkaDlqTestController {

    private final KafkaTemplate<byte[], byte[]> kafkaTemplate;

    public KafkaDlqTestController(KafkaTemplate<byte[], byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/invalid-appointment")
    public String sendInvalidAppointmentEvent() {

        byte[] garbage = "THIS_IS_NOT_PROTOBUF".getBytes();

        try {
            kafkaTemplate.send("appointment.created", garbage);
            return "❌ Invalid appointment event sent to Kafka";
        } catch (Exception e) {
            return "⚠️ Kafka send failed: " + e.getMessage();
        }
    }

}
