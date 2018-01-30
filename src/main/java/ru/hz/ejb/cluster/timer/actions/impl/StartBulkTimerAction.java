package ru.hz.ejb.cluster.timer.actions.impl;

import ru.hz.ejb.cluster.timer.DefaultTimerException;
import ru.hz.ejb.cluster.timer.IClusterTimerManager;
import ru.hz.ejb.cluster.timer.TimerDescriptor;
import ru.hz.ejb.cluster.timer.actions.AbstractCallableTimerAction;
import ru.hz.ejb.cluster.timer.impl.ClusterTimerLauncher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by mvj on 27.12.2016.
 */
public class StartBulkTimerAction extends AbstractCallableTimerAction<Map<TimerDescriptor, Boolean>> {
    private static final long serialVersionUID = -7431544564800130048L;
    private Collection<TimerDescriptor> timers = new ArrayList<>();

    public Collection<TimerDescriptor> getTimers() {
        return timers;
    }

    public void setTimers(Collection<TimerDescriptor> timers) {
        this.timers = timers;
    }

    @Override
    public Map<TimerDescriptor, Boolean> call(IClusterTimerManager timerManager) throws DefaultTimerException {
        return timerManager.unwrap(ClusterTimerLauncher.class).startLocalTimersInternallyLock(timers);
    }

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName();
    }
}