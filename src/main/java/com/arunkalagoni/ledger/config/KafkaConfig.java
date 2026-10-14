package com.arunkalagoni.ledger.config;

import com.arunkalagoni.ledger.domain.TaxCalculator;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Retry and dead-letter behavior for the purchase consumer.
 *
 * <p>Transient failures (a database blip, a Cassandra timeout) are retried with exponential
 * backoff. After retries are exhausted, the record goes to {@code <topic>.DLT} so one bad message
 * never blocks the partition. Errors that retrying cannot fix, such as malformed payloads or an
 * unknown country, skip retries and go straight to the dead-letter topic.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic purchaseTopic(@Value("${ledger.kafka.topic}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic purchaseDeadLetterTopic(@Value("${ledger.kafka.topic}") String topic) {
        return TopicBuilder.name(topic + ".DLT").partitions(6).replicas(1).build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
        ExponentialBackOff backOff = new ExponentialBackOff(500L, 2.0);
        backOff.setMaxElapsedTime(10_000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), backOff);
        handler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                TaxCalculator.UnknownCountryException.class,
                com.fasterxml.jackson.core.JsonProcessingException.class);
        return handler;
    }
}
