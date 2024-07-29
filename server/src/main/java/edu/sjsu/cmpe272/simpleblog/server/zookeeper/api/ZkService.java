package edu.sjsu.cmpe272.simpleblog.server.zookeeper.api;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public interface ZkService {

  String getLeaderNodeData();

  Set<String> getAllPartitions();

  String getPartitionLeader(Integer partition);

  void electForMaster();

  boolean leaderExists();

  void addToLiveNodes(String nodeName, String data);

  List<String> getLiveNodes();

  void addToAllServers(String nodeName, String data);

  List<String> getAllNodes();

  void createAllParentNodes();

  void registerChildrenChangeWatcher(String path, IZkChildListener iZkChildListener);

  void registerZkSessionStateListener(IZkStateListener iZkStateListener);

  void addToAllPartitions(String host, String data);

  void electForPartitionLeaders();
}
