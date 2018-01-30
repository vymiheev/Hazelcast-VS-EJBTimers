package ru.hz.ejb.cluster;

import com.hazelcast.core.HazelcastInstance;
import ru.hz.ejb.cluster.timer.IClusterTimerManager;
import ru.hz.ejb.cluster.timer.impl.ClusterTimerManager;

/**
 * Created by mvj on 02.02.2017.
 */
public class ClusterService extends AbstractClusterService {
    private ILocalServiceResolver serviceRouter;
    private ClusterConfigurationHolder configurationHolder;
    private ClusterTimerManager timerManager;

    public ClusterService(ILocalServiceResolver serviceRouter) {
        this.serviceRouter = serviceRouter;
        this.timerManager = new ClusterTimerManager(this);
        LocalTimerManagerHolder.setInstance(timerManager);
    }

    @Override
    public ILocalServiceResolver getServiceRouter() {
        return serviceRouter;
    }

    @Override
    public IClusterTimerManager getClusterTimerManager() {
        return timerManager;
    }

    @Override
    public void init(NetworkConfiguration networkConfiguration) {
        //init hazelCast
        configurationHolder = new ClusterConfigurationHolder(networkConfiguration);
        configurationHolder.init();
        timerManager.init();
    }

    @Override
    public NetworkConfiguration getNetworkConfiguration() {
        return configurationHolder.getNetworkConfiguration();
    }

    @Override
    public void shutdown() {
        configurationHolder.shutdown();
        timerManager.close();
    }

    @Override
    public HazelcastInstance getHazelcastInstance() {
        return configurationHolder.getHazelcastInstance();
    }
}
