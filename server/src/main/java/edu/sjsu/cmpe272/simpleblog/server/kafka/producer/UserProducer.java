package edu.sjsu.cmpe272.simpleblog.server.kafka.producer;

import edu.sjsu.cmpe272.simpleblog.common.request.UserRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserProducer {
    private final KafkaTemplate<String, UserRequest> kafkaTemplate;

    @Value("${user.topic.name}")
    private String userTopic;

    public UserProducer(KafkaTemplate<String, UserRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(UserRequest message, Integer partition) {
        kafkaTemplate.send(userTopic, partition , message.getUser(), message);
    }
}
