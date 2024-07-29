package edu.sjsu.cmpe272.simpleblog.server.zookeeper.util;

import org.springframework.beans.factory.annotation.Value;

public final class ZkDemoUtil {

  public static final String TEAM3 = "/team3";
  public static final String ALL_SERVERS = "/team3/all_servers";
  public static final String LIVE_SERVERS = "/team3/live_servers";
  public static final String LEADER = "/team3/leader";
  public static final String PARTITIONS = "/team3/partitions";
  public static final String PARTITION_LEADER = "/team3/partition_leader";

  @Value("${server.port}")
  private static String serverPort = "8082";

  @Value("${server.ip}")
  private static String ip = "172.27.20.32";

  private static String ipPort = null;

  public static String getHostPostOfServer() {
    if (ipPort != null) {
      return ipPort;
    }
    int port = Integer.parseInt(serverPort);
    ipPort = ip.concat(":").concat(String.valueOf(port));
    return ipPort;
  }

  public static boolean isEmpty(String str) {
    return str == null || str.length() == 0;
  }

  private ZkDemoUtil() {}
}
