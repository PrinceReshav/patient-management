package com.pm.notificationservice.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DltKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(DltKafkaConsumer.class);

    @KafkaListener(
            topics = {
                    "appointment.created.DLT",
                    "patient.created.DLT"
            },
            groupId = "notification-dlt"
    )
    public void consumeDlt(ConsumerRecord<byte[], byte[]> record) {

        log.error("""
                ☠️ MESSAGE SENT TO DLT
                Topic: {}
                Partition: {}
                Offset: {}
                Headers: {}
                Payload size: {}
                """,
                record.topic(),
                record.partition(),
                record.offset(),
                record.headers(),
                record.value().length
        );

        // ❌ NEVER throw exception here
    }
}