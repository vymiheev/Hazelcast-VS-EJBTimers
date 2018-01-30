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
public abstract class AbstractCallableTimerAction<T> implements IDistributedCallable<T>, ICommonTimerAction {
    private static final Logger logger = Logger.getLogger(AbstractCallableTimerAction.class);
    protected ILocalServiceResolver serviceRouter;

    @Override
    public final T call(ILocalServiceResolver serviceRouter) throws DefaultTimerException {
        this.serviceRouter = serviceRouter;
        logger.debug("Start action name: " + getActionName());
        T result = this.call(LocalTimerManagerHolder.getInstance());
        logger.debug("Action " + getActionName() + " completed!");
        return result;
    }

    public abstract T call(IClusterTimerManager timerManager) throws DefaultTimerException;
}
