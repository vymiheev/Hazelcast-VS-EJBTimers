package ru.grfc.crashtest.cluster.timer;

import java.io.Serializable;

/**
 * Created by mvj on 25.10.2017.
 */
public interface ITimerLauncherResolver extends Serializable {
    ITimerLauncher lookup();

    String getName();
}
