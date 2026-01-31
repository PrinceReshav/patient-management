package com.pm.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<byte[], byte[]>
    kafkaListenerContainerFactory(
            ConsumerFactory<byte[], byte[]> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {

        ConcurrentKafkaListenerContainerFactory<byte[], byte[]> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // 🔥 Retry + DLT handled entirely here
        factory.setCommonErrorHandler(kafkaErrorHandler);

        // Good practice (aligns with your single-partition topics)
        factory.setConcurrency(1);

        return factory;
    }
}
