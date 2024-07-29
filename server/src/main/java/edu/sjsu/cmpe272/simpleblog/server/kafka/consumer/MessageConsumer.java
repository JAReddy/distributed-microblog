package edu.sjsu.cmpe272.simpleblog.server.kafka.consumer;

import edu.sjsu.cmpe272.simpleblog.common.request.MessageRequest;
import edu.sjsu.cmpe272.simpleblog.server.entity.Message;
import edu.sjsu.cmpe272.simpleblog.server.repository.MessageRepository;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.util.OnStartUpApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class MessageConsumer implements InitializingBean {

    @Autowired
    private OnStartUpApplication onStartUpApplication;

    @Autowired
    private MessageRepository repository;

    @KafkaListener(id = "${message.consumer.listener.id}", topicPartitions = {
            @TopicPartition(topic = "${message.topic.name}", partitions = "#{partitionCalculator.calculatePartitions()}")},
            groupId = "${msg.consumer.group.id}")
    public void consumeFromDynamicPartitions(ConsumerRecord<String, MessageRequest> record) {
        System.out.println("Received message: " + record.value() + " from partition: " + record.partition());
        try {
            MessageRequest request = record.value();
            Message msg = new Message(request);
            msg = repository.save(msg);
            log.info("Message Id {} is created during replication", msg.getMessageId());
        } catch (Exception e) {
            log.error("Error consuming message create request for user {}", record.value().getAuthor());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Wait for required nodes to be created
        onStartUpApplication.onApplicationEvent(null);
    }
}

