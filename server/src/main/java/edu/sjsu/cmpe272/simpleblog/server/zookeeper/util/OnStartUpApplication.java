package edu.sjsu.cmpe272.simpleblog.server.zookeeper.util;

import edu.sjsu.cmpe272.simpleblog.server.zookeeper.api.ZkService;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;


import static edu.sjsu.cmpe272.simpleblog.server.zookeeper.util.ZkDemoUtil.*;


@Component
public class OnStartUpApplication implements ApplicationListener<ContextRefreshedEvent> {

  @Autowired
  private ZkService zkService;

  @Autowired
  private IZkChildListener allNodesChangeListener;

  @Autowired
  private IZkChildListener liveNodeChangeListener;

  @Autowired
  private IZkChildListener masterChangeListener;

  @Autowired
  private IZkChildListener partitionChangeListener;

  @Autowired
  private IZkChildListener partitionLeaderChangeListener;

  @Autowired
  private IZkStateListener connectStateChangeListener;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    try {

      // create all parent nodes /team3, /all_servers, /live_servers, /leader, /partitions, /partition_leader
      zkService.createAllParentNodes();

      // add this server to cluster by creating znode under /all_servers, with name as "host:port"
      zkService.addToAllServers(getHostPostOfServer(), "cluster node");
      ClusterInfo.getClusterInfo().getAllNodes().clear();
      ClusterInfo.getClusterInfo().getAllNodes().addAll(zkService.getAllNodes());

      // elect leader
      if (!zkService.leaderExists()) {
        zkService.electForMaster();
      } else {
        ClusterInfo.getClusterInfo().setMaster(zkService.getLeaderNodeData());
      }

      // add child znode under /live_servers, to tell other servers that this server is ready to serve read request
      zkService.addToLiveNodes(getHostPostOfServer(), "cluster node");
      ClusterInfo.getClusterInfo().getLiveNodes().clear();
      ClusterInfo.getClusterInfo().getLiveNodes().addAll(zkService.getLiveNodes());

      // add this server to all the partitions
      zkService.addToAllPartitions(getHostPostOfServer(), "partition host");

      // elect partition leader
      zkService.electForPartitionLeaders();

      // set all server partitions for kafka
      ClusterInfo.getClusterInfo().getPartitions().clear();
      ClusterInfo.getClusterInfo().getPartitions().addAll(zkService.getAllPartitions());

      // register watchers for leader change, live servers change, all servers change, partitions change zk session state change
      zkService.registerChildrenChangeWatcher(ALL_SERVERS, allNodesChangeListener);
      zkService.registerChildrenChangeWatcher(LIVE_SERVERS, liveNodeChangeListener);
      zkService.registerChildrenChangeWatcher(LEADER, masterChangeListener);
      for(int i=0;i<10;i++) {
        zkService.registerChildrenChangeWatcher(PARTITIONS.concat("/").concat(String.valueOf(i)), partitionChangeListener);
        zkService.registerChildrenChangeWatcher(PARTITION_LEADER.concat("/").concat(String.valueOf(i)), partitionLeaderChangeListener);
      }
      zkService.registerChildrenChangeWatcher(PARTITIONS, partitionChangeListener);
      zkService.registerChildrenChangeWatcher(PARTITION_LEADER, partitionLeaderChangeListener);
      zkService.registerZkSessionStateListener(connectStateChangeListener);
    } catch (Exception e) {
      throw new RuntimeException("Startup failed!!", e);
    }
  }

}
