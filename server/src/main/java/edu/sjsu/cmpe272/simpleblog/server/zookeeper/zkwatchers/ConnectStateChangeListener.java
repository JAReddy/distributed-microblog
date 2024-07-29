package edu.sjsu.cmpe272.simpleblog.server.zookeeper.zkwatchers;

import edu.sjsu.cmpe272.simpleblog.server.zookeeper.api.ZkService;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.util.ClusterInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import static edu.sjsu.cmpe272.simpleblog.server.zookeeper.util.ZkDemoUtil.getHostPostOfServer;


@Slf4j
@Setter
public class ConnectStateChangeListener implements IZkStateListener {

  private ZkService zkService;

  @Override
  public void handleStateChanged(KeeperState state) throws Exception {
    log.info(state.name()); // 1. disconnected, 2. expired, 3. SyncConnected
  }

  @Override
  public void handleNewSession() throws Exception {
    log.info("connected to zookeeper");

    // add new znode to /live_nodes to make it live
    zkService.addToLiveNodes(getHostPostOfServer(), "cluster node");
    ClusterInfo.getClusterInfo().getLiveNodes().clear();
    ClusterInfo.getClusterInfo().getLiveNodes().addAll(zkService.getLiveNodes());

    zkService.addToAllPartitions(getHostPostOfServer(), "partition host");
    zkService.electForPartitionLeaders();

    // retry creating znode under /election
    // this is needed, if there is only one server in cluster
    if (!zkService.leaderExists()) {
      zkService.electForMaster();
    } else {
      ClusterInfo.getClusterInfo().setMaster(zkService.getLeaderNodeData());
    }
  }

  @Override
  public void handleSessionEstablishmentError(Throwable error) throws Exception {
    log.info("could not establish session");
  }

}
