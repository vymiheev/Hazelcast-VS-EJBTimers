package ru.grfc.crashtest.cluster;

import ru.grfc.crashtest.cluster.timer.impl.ClusterTimerManager;

/**
 * Created by mvj on 11.10.2017.
 */
public class LocalTimerManagerHolder {
    private ClusterTimerManager timerManager;

    private LocalTimerManagerHolder() {
    }

    public static ClusterTimerManager getInstance() {
        return SingletonHolder.instance.timerManager;
    }

    static ClusterTimerManager setInstance(ClusterTimerManager timerManager) {
        return SingletonHolder.instance.timerManager = timerManager;
    }

    private static class SingletonHolder {
        private static final LocalTimerManagerHolder instance = new LocalTimerManagerHolder();
    }
}
