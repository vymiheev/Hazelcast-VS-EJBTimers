package ru.grfc.crashtest.cluster.timer.actions.impl;

import ru.grfc.crashtest.cluster.timer.DefaultTimerException;
import ru.grfc.crashtest.cluster.timer.IClusterTimerManager;
import ru.grfc.crashtest.cluster.timer.TimerDescriptor;
import ru.grfc.crashtest.cluster.timer.actions.AbstractRunnableTimerAction;
import ru.grfc.crashtest.cluster.timer.impl.ClusterTimerLauncher;

/**
 * Created by mvj on 02.12.2016.
 */
public class CreateTimerAction extends AbstractRunnableTimerAction {
    private static final long serialVersionUID = -1814165590594712552L;
    protected TimerDescriptor timer;

    public TimerDescriptor getTimer() {
        return timer;
    }

    public void setTimer(TimerDescriptor timer) {
        this.timer = timer;
    }

    @Override
    public void run(IClusterTimerManager timerManager) throws DefaultTimerException {
        timerManager.unwrap(ClusterTimerLauncher.class).justCreateLocalTimer(timer.getClusteredTimer());
    }

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName();
    }
}
