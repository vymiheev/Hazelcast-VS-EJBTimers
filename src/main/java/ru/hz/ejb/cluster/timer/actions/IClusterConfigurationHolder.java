package ru.hz.ejb.cluster.timer.actions;

import com.hazelcast.core.HazelcastInstance;
import ru.hz.ejb.cluster.NetworkConfiguration;

/**
 * Created by mvj on 31.01.2017.
 */
public interface IClusterConfigurationHolder {
    HazelcastInstance getHazelcastInstance();

    void init();

    NetworkConfiguration getNetworkConfiguration();

    void shutdown();
}
