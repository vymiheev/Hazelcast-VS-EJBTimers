package ru.grfc.crashtest.cluster.timer.actions;

import ru.grfc.crashtest.cluster.ILocalServiceResolver;
import ru.grfc.crashtest.cluster.timer.DefaultTimerException;

import java.io.Serializable;

/**
 * Created by mvj on 01.12.2016.
 */
public interface IDistributedRunnable extends Serializable {
    void run(ILocalServiceResolver serviceRouter) throws DefaultTimerException;
}
