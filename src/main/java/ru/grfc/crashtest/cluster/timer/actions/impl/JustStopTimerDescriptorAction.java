package ru.grfc.crashtest.cluster.timer.actions.impl;

import ru.grfc.crashtest.cluster.timer.DefaultTimerException;
import ru.grfc.crashtest.cluster.timer.IClusterTimerManager;
import ru.grfc.crashtest.cluster.timer.TimerDescriptor;
import ru.grfc.crashtest.cluster.timer.actions.AbstractCallableTimerAction;
import ru.grfc.crashtest.cluster.timer.impl.ClusterTimerDestroyer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mvj on 24.01.2017.
 */
public class JustStopTimerDescriptorAction extends AbstractCallableTimerAction<Map<TimerDescriptor, Boolean>> {
    private static final long serialVersionUID = -8112588682749822986L;
    private List<TimerDescriptor> timerToDestroy = new ArrayList<>();

    public List<TimerDescriptor> getTimerToDestroy() {
        return timerToDestroy;
    }

    public void setTimerToDestroy(List<TimerDescriptor> timerToDestroy) {
        this.timerToDestroy = timerToDestroy;
    }

    @Override
    public Map<TimerDescriptor, Boolean> call(IClusterTimerManager timerManager) throws DefaultTimerException {
        return timerManager.unwrap(ClusterTimerDestroyer.class).justStopLocalTimers(timerToDestroy);
    }

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName();
    }
}
