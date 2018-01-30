package ru.grfc.crashtest.cluster.timer.actions;

import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.ILocalServiceResolver;
import ru.grfc.crashtest.cluster.LocalTimerManagerHolder;
import ru.grfc.crashtest.cluster.timer.DefaultTimerException;
import ru.grfc.crashtest.cluster.timer.IClusterTimerManager;
import ru.grfc.crashtest.cluster.timer.ICommonTimerAction;

/**
 * Created by mvj on 02.02.2017.
 */
public abstract class AbstractRunnableTimerAction implements IDistributedRunnable, ICommonTimerAction {
    private static final Logger logger = Logger.getLogger(AbstractRunnableTimerAction.class);
    protected ILocalServiceResolver serviceRouter;

    @Override
    public void run(ILocalServiceResolver serviceRouter) throws DefaultTimerException {
        this.serviceRouter = serviceRouter;
        logger.debug("Start action name: " + getActionName());
        run(LocalTimerManagerHolder.getInstance());
        logger.debug("Action " + getActionName() + " completed!");
    }

    public abstract void run(IClusterTimerManager timerManager) throws DefaultTimerException;
}
