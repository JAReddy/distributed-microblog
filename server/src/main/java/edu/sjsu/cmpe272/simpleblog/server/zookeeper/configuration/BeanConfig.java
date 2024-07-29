package edu.sjsu.cmpe272.simpleblog.server.zookeeper.configuration;

import edu.sjsu.cmpe272.simpleblog.server.zookeeper.api.ZkService;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.impl.ZkServiceImpl;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.zkwatchers.AllNodesChangeListener;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.zkwatchers.ConnectStateChangeListener;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.zkwatchers.LiveNodeChangeListener;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.zkwatchers.MasterChangeListener;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.zkwatchers.PartitionChangeListener;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.zkwatchers.PartitionLeaderChangeListener;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class BeanConfig {

  @Value("${zookeeper.host}")
  private String zookeeperHost;

  @Bean(name = "zkService")
  @Scope("singleton")
  public ZkService zkService() {
    return new ZkServiceImpl(zookeeperHost);
  }

  @Bean(name = "allNodesChangeListener")
  @Scope("singleton")
  public IZkChildListener allNodesChangeListener() {
    return new AllNodesChangeListener();
  }

  @Bean(name = "liveNodeChangeListener")
  @Scope("singleton")
  public IZkChildListener liveNodeChangeListener() {
    return new LiveNodeChangeListener();
  }

  @Bean(name = "masterChangeListener")
  @Scope("singleton")
  public IZkChildListener masterChangeListener() {
    MasterChangeListener masterChangeListener = new MasterChangeListener();
    masterChangeListener.setZkService(zkService());
    return masterChangeListener;
  }

  @Bean(name = "partitionChangeListener")
  @Scope("singleton")
  public IZkChildListener partitionChangeListener() {
    PartitionChangeListener partitionChangeListener = new PartitionChangeListener();
    partitionChangeListener.setZkService(zkService());
    return partitionChangeListener;
  }

  @Bean(name = "partitionLeaderChangeListener")
  @Scope("singleton")
  public IZkChildListener partitionLeaderChangeListener() {
    PartitionLeaderChangeListener partitionLeaderChangeListener = new PartitionLeaderChangeListener();
    partitionLeaderChangeListener.setZkService(zkService());
    return partitionLeaderChangeListener;
  }

  @Bean(name = "connectStateChangeListener")
  @Scope("singleton")
  public IZkStateListener connectStateChangeListener() {
    ConnectStateChangeListener connectStateChangeListener = new ConnectStateChangeListener();
    connectStateChangeListener.setZkService(zkService());
    return connectStateChangeListener;
  }
}
