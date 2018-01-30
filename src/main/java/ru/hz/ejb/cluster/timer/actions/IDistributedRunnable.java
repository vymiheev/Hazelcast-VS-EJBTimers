package ru.hz.ejb.cluster.timer.actions;

import ru.hz.ejb.cluster.ILocalServiceResolver;
import ru.hz.ejb.cluster.timer.DefaultTimerException;

import java.io.Serializable;

/**
 * Created by mvj on 01.12.2016.
 */
public interface IDistributedRunnable extends Serializable {
    void run(ILocalServiceResolver serviceRouter) throws DefaultTimerException;
}
