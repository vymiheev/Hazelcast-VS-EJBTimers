package ru.hz.ejb.cluster.timer.actions;

import org.apache.log4j.Logger;
import ru.hz.ejb.cluster.ILocalServiceResolver;
import ru.hz.ejb.cluster.LocalTimerManagerHolder;
import ru.hz.ejb.cluster.timer.DefaultTimerException;
import ru.hz.ejb.cluster.timer.IClusterTimerManager;
import ru.hz.ejb.cluster.timer.ICommonTimerAction;

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
