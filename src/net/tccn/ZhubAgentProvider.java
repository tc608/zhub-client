package net.tccn;

import org.redkale.boot.Application;
import org.redkale.boot.NodeServer;
import org.redkale.cluster.CacheClusterAgent;
import org.redkale.cluster.ClusterAgent;
import org.redkale.service.Service;
import org.redkale.util.ResourceEvent;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class ZhubAgentProvider extends ClusterAgent {

    @Override
    public void onResourceChange(ResourceEvent[] events) {

    }

    @Override
    public void register(Application application) {

    }

    @Override
    public void deregister(Application application) {

    }

    @Override
    public CompletableFuture<Map<String, Set<InetSocketAddress>>> queryMqtpAddress(String protocol, String module, String resname) {
        return null;
    }

    @Override
    public CompletableFuture<Set<InetSocketAddress>> queryHttpAddress(String protocol, String module, String resname) {
        return null;
    }

    @Override
    public CompletableFuture<Set<InetSocketAddress>> querySncpAddress(String protocol, String restype, String resname) {
        return null;
    }

    @Override
    protected CompletableFuture<Set<InetSocketAddress>> queryAddress(ClusterEntry entry) {
        return null;
    }

    @Override
    protected ClusterEntry register(NodeServer ns, String protocol, Service service) {
        deregister(ns, protocol, service);
        ClusterEntry clusterEntry = new ClusterEntry(ns, protocol, service);
        CacheClusterAgent.AddressEntry entry = new CacheClusterAgent.AddressEntry();
        entry.addr = clusterEntry.address;
        entry.resname = clusterEntry.resourceName;
        entry.nodeid = this.nodeid;
        entry.time = System.currentTimeMillis();
        //source.hset(clusterEntry.serviceName, clusterEntry.serviceid, CacheClusterAgent.AddressEntry.class, entry);
        return clusterEntry;
    }

    @Override
    protected void deregister(NodeServer ns, String protocol, Service service) {

    }
}
