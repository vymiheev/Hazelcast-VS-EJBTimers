package ru.hz.ejb.cluster.timer;

import org.apache.log4j.Logger;

/**
 * Created by mvj on 25.01.2017.
 */
public class DummyTimerAction implements ITimerAction {
    private static final long serialVersionUID = -4773472366012357925L;
    private static final Logger logger = Logger.getLogger(DummyTimerAction.class);

    @Override
    public void run() {

    }
}
