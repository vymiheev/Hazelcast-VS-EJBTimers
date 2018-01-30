package ru.hz.ejb.cluster.timer.actions.impl;

import ru.hz.ejb.cluster.timer.DefaultTimerException;
import ru.hz.ejb.cluster.timer.IClusterTimerManager;
import ru.hz.ejb.cluster.timer.actions.AbstractCallableTimerAction;
import ru.hz.ejb.cluster.timer.impl.ClusterTimerAnalyzer;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by mvj on 24.01.2017.
 */
public class FetchCommonTimersAction extends AbstractCallableTimerAction<Collection<Serializable>> {
    private static final long serialVersionUID = 1191130549658431683L;

    @Override
    public Collection<Serializable> call(IClusterTimerManager timerManager) throws DefaultTimerException {
        return timerManager.unwrap(ClusterTimerAnalyzer.class).getLocalCommonTimers();
    }

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName();
    }
}
