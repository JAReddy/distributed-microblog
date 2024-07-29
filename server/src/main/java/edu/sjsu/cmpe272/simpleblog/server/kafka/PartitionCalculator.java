package edu.sjsu.cmpe272.simpleblog.server.kafka;

import edu.sjsu.cmpe272.simpleblog.server.zookeeper.api.ZkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PartitionCalculator {

    @Autowired
    ZkService zkService;
    public String[] calculatePartitions() {
        Set<String> allServerPartitions = zkService.getAllPartitions();
        String[] partitions = allServerPartitions.toArray(new String[0]);
        return partitions;
    }
}