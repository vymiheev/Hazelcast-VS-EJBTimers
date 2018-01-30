package ru.grfc.crashtest.cluster.timer.actions.impl;

import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.timer.IClusterTimerManager;
import ru.grfc.crashtest.cluster.timer.actions.AbstractRunnableTimerAction;

/**
 * Created by mvj on 01.12.2016.
 */
public class EchoNodesAction extends AbstractRunnableTimerAction {
    private static final long serialVersionUID = 539118416163622940L;
    private static final Logger logger = Logger.getLogger(EchoNodesAction.class);

    @Override
    public void run(IClusterTimerManager timerManager) {
        logger.info("Echo on server uuid: " + timerManager.getClusterService().getLocalNodeUUID());
    }

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName();
    }
}
