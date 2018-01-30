package ru.grfc.crashtest.cluster.timer.actions.impl;

import ru.grfc.crashtest.cluster.timer.DefaultTimerException;
import ru.grfc.crashtest.cluster.timer.IClusterTimerManager;
import ru.grfc.crashtest.cluster.timer.actions.AbstractCallableTimerAction;
import ru.grfc.crashtest.cluster.timer.impl.ClusterTimerDestroyer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mvj on 24.01.2017.
 */
public class JustStopTimerAction extends AbstractCallableTimerAction<Map<String, Boolean>> {
    private static final long serialVersionUID = -8112588682749822986L;
    private List<String> timerToDestroy = new ArrayList<>();

    public List<String> getTimerToDestroy() {
        return timerToDestroy;
    }

    public void setTimerToDestroy(List<String> timerToDestroy) {
        this.timerToDestroy = timerToDestroy;
    }

    @Override
    public Map<String, Boolean> call(IClusterTimerManager timerManager) throws DefaultTimerException {
        Map<String, Boolean> result = new HashMap<>();
        for (String timerName : timerToDestroy) {
            boolean isStopped = timerManager.unwrap(ClusterTimerDestroyer.class).justStopLocalTimerEverywhere(timerName);
            result.put(timerName, isStopped);
        }
        return result;
    }

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName();
    }
}
