package edu.sjsu.cmpe272.simpleblog.server.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sjsu.cmpe272.simpleblog.common.request.UserRequest;
import edu.sjsu.cmpe272.simpleblog.common.response.UserSuccess;
import edu.sjsu.cmpe272.simpleblog.server.entity.User;
import edu.sjsu.cmpe272.simpleblog.server.repository.UserRepository;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.util.OnStartUpApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;

import java.io.DataInput;
import java.util.Optional;

@Service
@Slf4j
public class UserConsumer implements InitializingBean {

    @Autowired
    private OnStartUpApplication onStartUpApplication;

    @Autowired
    private UserRepository repository;

    @KafkaListener(id = "${user.consumer.listener.id}", topicPartitions = {
            @TopicPartition(topic = "${user.topic.name}", partitions = "#{partitionCalculator.calculatePartitions()}")},
            groupId = "${user.consumer.group.id}")
    public void consumeFromDynamicPartitions(ConsumerRecord<String, UserRequest> record) {
        System.out.println("Received message: " + record.value() + " from partition: " + record.partition());
        try {
            UserRequest user = record.value();

            Optional<User> existingUser = repository.findById(user.getUser());
            if (existingUser.isPresent()) {
                log.info("Duplicate Id, user {} already exists", user.getUser());
            } else{
                User usr = new User(user);
                repository.save(usr);
                log.info("user {} is created during replication", usr.getUser());
            }
        } catch (Exception e) {
            log.error("Error consuming user create request for {}", record.value().getUser());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Wait for required nodes to be created
        onStartUpApplication.onApplicationEvent(null);
    }
}

