package ru.hz.ejb.cluster.timer.actions;

import ru.hz.ejb.cluster.ILocalServiceResolver;
import ru.hz.ejb.cluster.timer.DefaultTimerException;

import java.io.Serializable;


/**
 * Created by mvj on 03.12.2016.
 */
public interface IDistributedCallable<V> extends Serializable {
    V call(ILocalServiceResolver serviceRouter) throws DefaultTimerException;
}
