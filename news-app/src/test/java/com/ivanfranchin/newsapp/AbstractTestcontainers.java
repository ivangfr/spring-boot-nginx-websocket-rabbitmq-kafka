package com.ivanfranchin.newsapp;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class AbstractTestcontainers {

    @Container
    static PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:18.0");

    @Container
    static RabbitMQContainer rabbitMQContainer =
            new RabbitMQContainer("rabbitmq:4.2.1-management");

    @Container
    static ConfluentKafkaContainer kafkaContainer =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.9.5"));

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
    }
}