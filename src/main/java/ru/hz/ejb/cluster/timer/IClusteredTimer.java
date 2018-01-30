package ru.hz.ejb.cluster.timer;

import java.io.Serializable;

/**
 * Created by mvj on 14.11.2016.
 */
public interface IClusteredTimer extends Serializable {
    TimerInfo getTimerInfo();

    TimerType getTimerType();

    AbstractTimerLauncherResolver getLauncherResolver();

}
