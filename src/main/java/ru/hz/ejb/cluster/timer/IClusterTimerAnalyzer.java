package ru.hz.ejb.cluster.timer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Created by mvj on 28.01.2017.
 */
public interface IClusterTimerAnalyzer {

    Collection<TimerState> getLocalInvalidTimers();

    Map<String, Collection<TimerState>> getInvalidTimers() throws DefaultTimerException;

    boolean hasInvalidTimers() throws DefaultTimerException;

    Collection<Serializable> getLocalCommonTimers();

    Map<String, Collection<Serializable>> getCommonTimers() throws DefaultTimerException;

    boolean hasCommonTimers() throws DefaultTimerException;
}
