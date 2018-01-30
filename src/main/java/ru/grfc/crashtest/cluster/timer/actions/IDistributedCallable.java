package ru.grfc.crashtest.cluster.timer.actions;

import ru.grfc.crashtest.cluster.ILocalServiceResolver;
import ru.grfc.crashtest.cluster.timer.DefaultTimerException;

import java.io.Serializable;


/**
 * Created by mvj on 03.12.2016.
 */
public interface IDistributedCallable<V> extends Serializable {
    V call(ILocalServiceResolver serviceRouter) throws DefaultTimerException;
}
