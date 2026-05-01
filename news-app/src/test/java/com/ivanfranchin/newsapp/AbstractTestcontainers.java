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
  static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18.0");

  @Container
  static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:4.2.1-management");

  @Container
  static ConfluentKafkaContainer kafkaContainer =
      new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.9.5"));

  @DynamicPropertySource
  static void configureKafka(DynamicPropertyRegistry registry) {
    // Kafka
    registry.add(
        "spring.cloud.stream.binders.kafkaBinder.environment.spring.cloud.stream.kafka.binder.brokers",
        kafkaContainer::getBootstrapServers);

    // PostgreSQL
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

    // RabbitMQ
    registry.add(
        "spring.cloud.stream.binders.rabbitBinder.environment.spring.rabbitmq.host",
        rabbitMQContainer::getHost);
    registry.add(
        "spring.cloud.stream.binders.rabbitBinder.environment.spring.rabbitmq.port",
        () -> rabbitMQContainer.getMappedPort(5672));
    registry.add(
        "spring.cloud.stream.binders.rabbitBinder.environment.spring.rabbitmq.username",
        () -> "guest");
    registry.add(
        "spring.cloud.stream.binders.rabbitBinder.environment.spring.rabbitmq.password",
        () -> "guest");
  }
}
