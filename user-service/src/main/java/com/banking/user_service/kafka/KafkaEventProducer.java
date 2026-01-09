package com.banking.user_service.kafka;

import com.banking.user_service.event.UserLoginEvent;
import com.banking.user_service.event.UserRegisteredEvent;
import com.banking.user_service.event.UserStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaEventProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // Kafka Topics
    private static final String USER_REGISTERED_TOPIC = "user-registered-topic";
    private static final String USER_LOGIN_TOPIC = "user-login-topic";
    private static final String USER_STATUS_CHANGED_TOPIC = "user-status-changed-topic";

    /**
     * Publish User Registration Event
     * This event will be consumed by:
     * - Account Service (to create default account)
     * - Notification Service (to send welcome email/SMS)
     */
    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            log.info("Publishing UserRegisteredEvent for userId: {}", event.getUserId());

            Message<UserRegisteredEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, USER_REGISTERED_TOPIC)
                    .setHeader(KafkaHeaders.KEY, event.getUserId().toString())
                    .build();

            kafkaTemplate.send(message);
            log.info(" UserRegisteredEvent published successfully for userId: {}", event.getUserId());

        } catch (Exception e) {
            log.error(" Failed to publish UserRegisteredEvent for userId: {}", event.getUserId(), e);
        }
    }

    /**
     * Publish User Login Event
     * This event will be consumed by:
     * - Notification Service (to send login alert)
     * - Audit Service (for security monitoring)
     */
    public void publishUserLoginEvent(UserLoginEvent event) {
        try {
            log.info("Publishing UserLoginEvent for userId: {}", event.getUserId());

            Message<UserLoginEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, USER_LOGIN_TOPIC)
                    .setHeader(KafkaHeaders.KEY, event.getUserId().toString())
                    .build();

            kafkaTemplate.send(message);
            log.info(" UserLoginEvent published successfully for userId: {}", event.getUserId());

        } catch (Exception e) {
            log.error("Failed to publish UserLoginEvent for userId: {}", event.getUserId(), e);
        }
    }

    /**
     * Publish User Status Changed Event
     * This event will be consumed by:
     * - Notification Service (to alert user)
     * - Account Service (to block/unblock accounts)
     */
    public void publishUserStatusChangedEvent(UserStatusChangedEvent event) {
        try {
            log.info("Publishing UserStatusChangedEvent for userId: {} - Status changed from {} to {}",
                    event.getUserId(), event.getOldStatus(), event.getNewStatus());

            Message<UserStatusChangedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, USER_STATUS_CHANGED_TOPIC)
                    .setHeader(KafkaHeaders.KEY, event.getUserId().toString())
                    .build();

            kafkaTemplate.send(message);
            log.info(" UserStatusChangedEvent published successfully for userId: {}", event.getUserId());

        } catch (Exception e) {
            log.error("Failed to publish UserStatusChangedEvent for userId: {}", event.getUserId(), e);
        }
    }
}

