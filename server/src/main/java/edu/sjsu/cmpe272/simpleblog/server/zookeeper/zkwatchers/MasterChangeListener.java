package edu.sjsu.cmpe272.simpleblog.server.zookeeper.zkwatchers;

import edu.sjsu.cmpe272.simpleblog.server.zookeeper.api.ZkService;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.util.ClusterInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import java.util.List;

@Setter
@Slf4j
public class  MasterChangeListener implements IZkChildListener {

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
    if (currentChildren.isEmpty()) {
      log.info("Leader deleted, recreating leader!");
      ClusterInfo.getClusterInfo().setMaster(null);
      try {
        zkService.electForMaster();
      } catch (ZkNodeExistsException e) {
        log.info("leader already created");
      }
    } else {
      String leaderNode = zkService.getLeaderNodeData();
      log.info("updating new leader: {}", leaderNode);
      ClusterInfo.getClusterInfo().setMaster(leaderNode);
    }
  }
}
