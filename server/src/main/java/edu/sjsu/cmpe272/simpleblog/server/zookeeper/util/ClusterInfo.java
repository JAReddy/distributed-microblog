package edu.sjsu.cmpe272.simpleblog.server.zookeeper.util;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public final class ClusterInfo {

  @Getter
  private static ClusterInfo clusterInfo = new ClusterInfo();

  /*
  these will be ephemeral znodes
   */
  private List<String> liveNodes = new ArrayList<>();

  /*
  these will be persistent znodes
   */
  private List<String> allNodes = new ArrayList<>();

  private String master;

  private Set<String> partitions = new HashSet<>();
}
