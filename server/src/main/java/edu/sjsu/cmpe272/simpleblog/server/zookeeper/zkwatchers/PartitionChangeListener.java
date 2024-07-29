package edu.sjsu.cmpe272.simpleblog.server.zookeeper.zkwatchers;

import edu.sjsu.cmpe272.simpleblog.server.zookeeper.api.ZkService;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.util.ClusterInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;

import java.util.List;
import java.util.Set;

@Setter
@Slf4j
public class PartitionChangeListener implements IZkChildListener {

  private ZkService zkService;

  /**
   * listens for creation/deletion of znode "master" under /election znode and updates the
   * clusterinfo
   *
   * @param parentPath
   * @param currentChildren
   */
  @Override
  public void handleChildChange(String parentPath, List<String> currentChildren) {
    if (!currentChildren.isEmpty()) {
      Set<String> partitions = zkService.getAllPartitions();
      log.info("updating partitions of server to: {}", partitions.toString());
      ClusterInfo.getClusterInfo().setPartitions(partitions);
    }
  }
}
