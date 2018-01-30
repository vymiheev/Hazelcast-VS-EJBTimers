package ru.hz.ejb.cluster.timer;

import java.io.Serializable;

/**
 * Created by mvj on 28.11.2016.
 */
public enum TimerStatus implements Serializable {
    STOPPED, RUNNING, FAILED, IDLE
}
