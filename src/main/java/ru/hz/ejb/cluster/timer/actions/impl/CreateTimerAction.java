package ru.hz.ejb.cluster.timer.actions.impl;

import ru.hz.ejb.cluster.timer.DefaultTimerException;
import ru.hz.ejb.cluster.timer.IClusterTimerManager;
import ru.hz.ejb.cluster.timer.TimerDescriptor;
import ru.hz.ejb.cluster.timer.actions.AbstractRunnableTimerAction;
import ru.hz.ejb.cluster.timer.impl.ClusterTimerLauncher;

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
