package ru.hz.ejb.cluster.timer;

import java.io.Serializable;

/**
 * Created by mvj on 27.10.2016.
 */
public interface ITimerAction extends Serializable {
    void run();
}
