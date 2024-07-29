package edu.sjsu.cmpe272.simpleblog.server.kafka.producer;

import edu.sjsu.cmpe272.simpleblog.common.request.MessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {
    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    @Value("${message.topic.name}")
    private String messageTopic;

    public MessageProducer(KafkaTemplate<String, MessageRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(MessageRequest message, Integer partition) {
        kafkaTemplate.send(messageTopic, partition , message.getAuthor(), message);
    }
}
