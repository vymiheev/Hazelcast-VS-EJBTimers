package ru.grfc.crashtest.cluster.timer.actions;

import com.hazelcast.core.HazelcastInstance;
import ru.grfc.crashtest.cluster.NetworkConfiguration;

/**
 * Created by mvj on 31.01.2017.
 */
public interface IClusterConfigurationHolder {
    HazelcastInstance getHazelcastInstance();

    void init();

    NetworkConfiguration getNetworkConfiguration();

    void shutdown();
}
