package ru.grfc.crashtest.cluster;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.GroupProperty;
import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.timer.actions.IClusterConfigurationHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mvj on 26.10.2016.
 */
public final class ClusterConfigurationHolder implements IClusterConfigurationHolder {
    private static final Logger logger = Logger.getLogger(ClusterConfigurationHolder.class);
    private static final String CONFIG_HAZELCAST_XML = "ru/grfc/crashtest/hazelcast.xml";
    private static final int DEFAULT_HZ_START_PORT = 5801;
    private static final int DEFAULT_PORT_COUNT = 100;
    private final NetworkConfiguration networkConfiguration;
    public HazelcastInstance hazelcastInstance;

    public ClusterConfigurationHolder(NetworkConfiguration networkConfiguration) {
        this.networkConfiguration = networkConfiguration;
    }

    public void init() {
        INodeRelation nodeRelation = networkConfiguration.getNodeRelation();
        Config config = new ClasspathXmlConfig(CONFIG_HAZELCAST_XML);
        if (nodeRelation == null || nodeRelation.getThisNode() == null || !nodeRelation.isClustered()
                || !nodeRelation.getThisNode().getNodeEnabled()) {
            logger.info("SINGLE SERVER MODE");
            buildSingleConfig(config);
        } else {
            logger.info("CLUSTER MODE");
            buildClusterConfig(config);
        }

        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    }


    public NetworkConfiguration getNetworkConfiguration() {
        return networkConfiguration;
    }

    private Config buildSingleConfig(Config config) {
        INodeRelation nodeRelation = networkConfiguration.getNodeRelation();
        NetworkConfig networkConfig = config.getNetworkConfig();
        Integer hzPort = nodeRelation != null ? nodeRelation.getThisNode() != null ? nodeRelation.getThisNode().getHzPort() : null : null;
        hzPort = hzPort != null ? hzPort : DEFAULT_HZ_START_PORT;
        networkConfig.setPortAutoIncrement(true).setPort(hzPort).setPortCount(DEFAULT_PORT_COUNT);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(false);
        networkConfig.setReuseAddress(true);
        config.getGroupConfig().setName(networkConfiguration.getGroupName()).setPassword(networkConfiguration.getGroupPassword());
        config.getManagementCenterConfig().setEnabled(networkConfiguration.isManCenterEnabled()).setUrl(networkConfiguration.getManCenterURL());
        return config;
    }

    private Config buildClusterConfig(Config config) {
        INodeRelation nodeRelation = networkConfiguration.getNodeRelation();
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPortAutoIncrement(false).setPort(nodeRelation.getThisNode().getHzPort());
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true).setMembers(buildMembersURL(nodeRelation.getRelatedNodes()));
        networkConfig.setReuseAddress(true);
        InterfacesConfig interfacesConfig = networkConfig.getInterfaces();
        interfacesConfig.setEnabled(true);
        interfacesConfig.addInterface(nodeRelation.getThisNode().getNode());
        //By default, Hazelcast binds to all local network interfaces to accept incoming traffic. You can change this
        //behavior using the system property hazelcast.socket.bind.any. If you set this property to false, Hazelcast
        //uses the interfaces specified in the interfaces element.
        //If no interfaces are provided, then it will try to resolve one interface to bind from the member elements.
        config.setProperty(GroupProperty.SOCKET_BIND_ANY.getName(), "false");
        config.getGroupConfig().setName(networkConfiguration.getGroupName()).setPassword(networkConfiguration.getGroupPassword());
        config.getManagementCenterConfig().setEnabled(networkConfiguration.isManCenterEnabled()).setUrl(networkConfiguration.getManCenterURL());
        //Group members in the same physical machine
        PartitionGroupConfig partitionGroupConfig = config.getPartitionGroupConfig();
        partitionGroupConfig.setEnabled(true).setGroupType(PartitionGroupConfig.MemberGroupType.HOST_AWARE);
        return config;
    }

    private List<String> buildMembersURL(List<IClusterNode> relatedNodes) {
        List<String> members = new ArrayList<>();
        for (IClusterNode clusterNode : relatedNodes) {
            if (clusterNode.getNodeEnabled()) {
                members.add(String.format("%s:%s", clusterNode.getNode(), clusterNode.getHzPort()));
            }
        }
        return members;
    }

    @Override
    public void shutdown() {
        logger.debug("Shutdown hazelcast instance.");
        if (hazelcastInstance != null) {
            hazelcastInstance.shutdown();
        }
    }

    @Override
    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

}


