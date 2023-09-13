package TextGen;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class LoadBalancer {
    // ConcurrentHashMap for storing server and their loads
    private static Map<String, Integer> servers = new ConcurrentHashMap<>();

    // ConcurrentHashMap for storing servers that are unreachable along with the
    // time they became unreachable
    private static Map<String, Long> unreachableServers = new ConcurrentHashMap<>();

    // Singleton instance
    private static LoadBalancer instance;

    private LoadBalancer(List<String> serverList) {
        for (String server : serverList) {
            servers.put(server, 0);
        }
    }

    // Public method to get the instance
    public static LoadBalancer getInstance(List<String> serverList) {
        if (instance == null) {
            instance = new LoadBalancer(serverList);
        }
        return instance;
    }

    public synchronized String getLeastBusyServer() {
        String leastBusyServer = null;
        int minLoad = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : servers.entrySet()) {
            // Check if server is marked as unreachable and has been for less than 1 minutes
            // (60000 milliseconds)
            if (unreachableServers.containsKey(entry.getKey()) &&
                    System.currentTimeMillis() - unreachableServers.get(entry.getKey()) < 60000) {
                continue;
            } else {
                // Remove the server from the unreachableServers list as it is now reachable
                unreachableServers.remove(entry.getKey());
            }

            if (entry.getValue() < minLoad) {
                minLoad = entry.getValue();
                leastBusyServer = entry.getKey();
            }
        }

        if (leastBusyServer != null) {
            servers.put(leastBusyServer, minLoad + 1);
            SimpleDateFormat sdf = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss]");
            System.out.println(sdf.format(new Date()) + " [" + (minLoad + 1) + "] Selected: " + leastBusyServer);
        }

        return leastBusyServer;
    }

    public synchronized void releaseServer(String server) {
        if (server != null) {
            servers.put(server, servers.get(server) - 1);
            SimpleDateFormat sdf = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss]");
            System.out.println(sdf.format(new Date()) + " [" + servers.get(server) + "] Released: " + server);
        }
    }

    // Method to mark server as unreachable
    public synchronized void markAsUnreachable(String server) {
        unreachableServers.put(server, System.currentTimeMillis());
    }
}
