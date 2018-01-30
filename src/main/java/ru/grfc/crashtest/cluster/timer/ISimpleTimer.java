package ru.grfc.crashtest.cluster.timer;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mvj on 25.10.2017.
 */
public interface ISimpleTimer extends Serializable {
    Date getNextTimeout();

    boolean isPersistent();

    Serializable getInfo();

}
