package ru.hz.ejb.cluster.timer.actions.impl;

import ru.hz.ejb.cluster.timer.DefaultTimerException;
import ru.hz.ejb.cluster.timer.IClusterTimerManager;
import ru.hz.ejb.cluster.timer.IClusteredTimer;
import ru.hz.ejb.cluster.timer.actions.AbstractRunnableTimerAction;
import ru.hz.ejb.cluster.timer.impl.ClusterTimerLauncher;

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
