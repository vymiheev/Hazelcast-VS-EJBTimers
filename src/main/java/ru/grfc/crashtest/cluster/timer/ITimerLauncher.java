package ru.grfc.crashtest.cluster.timer;

import java.util.Collection;

/**
 * Created by mvj on 28.01.2017.
 */
public interface ITimerLauncher<T extends IClusteredTimer> {
    void initTimer(T clusteredTimer);

    boolean cancelTimer(TimerInfo timerInfo);

    Collection<ISimpleTimer> getTimers();

    Collection<TimerInfo> getTimersInfo();
}
