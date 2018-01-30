package ru.grfc.crashtest.cluster.timer.actions.impl;

import ru.grfc.crashtest.cluster.timer.DefaultTimerException;
import ru.grfc.crashtest.cluster.timer.IClusterTimerManager;
import ru.grfc.crashtest.cluster.timer.IClusteredTimer;
import ru.grfc.crashtest.cluster.timer.actions.AbstractRunnableTimerAction;
import ru.grfc.crashtest.cluster.timer.impl.ClusterTimerLauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mvj on 27.01.2017.
 */
public class JustCreateTimerAction extends AbstractRunnableTimerAction {
    private static final long serialVersionUID = -4936705286395039509L;
    private List<IClusteredTimer> timerToCreate = new ArrayList<>();

    public List<IClusteredTimer> getTimerToCreate() {
        return timerToCreate;
    }

    public void setTimerToCreate(List<IClusteredTimer> timerToCreate) {
        this.timerToCreate = timerToCreate;
    }

    @Override
    public void run(IClusterTimerManager timerManager) throws DefaultTimerException {
        timerManager.unwrap(ClusterTimerLauncher.class).justCreateLocalTimers(timerToCreate);
    }

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName();
    }
}
