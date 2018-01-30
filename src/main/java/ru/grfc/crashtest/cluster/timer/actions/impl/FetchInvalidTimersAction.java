package ru.grfc.crashtest.cluster.timer.actions.impl;

import ru.grfc.crashtest.cluster.timer.DefaultTimerException;
import ru.grfc.crashtest.cluster.timer.IClusterTimerManager;
import ru.grfc.crashtest.cluster.timer.TimerState;
import ru.grfc.crashtest.cluster.timer.actions.AbstractCallableTimerAction;
import ru.grfc.crashtest.cluster.timer.impl.ClusterTimerAnalyzer;

import java.util.Collection;

/**
 * Created by mvj on 24.01.2017.
 */
public class FetchInvalidTimersAction extends AbstractCallableTimerAction<Collection<TimerState>> {
    private static final long serialVersionUID = 2113529947640704908L;

    @Override
    public Collection<TimerState> call(IClusterTimerManager clusterTimerManager) throws DefaultTimerException {
        return clusterTimerManager.unwrap(ClusterTimerAnalyzer.class).getLocalInvalidTimers();
    }

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName();
    }
}
