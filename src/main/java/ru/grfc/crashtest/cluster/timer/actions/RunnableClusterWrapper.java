package ru.grfc.crashtest.cluster.timer.actions;

import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.ILocalServiceResolver;

import java.io.Serializable;

/**
 * Created by mvj on 01.12.2016.
 */
public class RunnableClusterWrapper implements Serializable, Runnable {
    private static final long serialVersionUID = -2155715074502174681L;
    private static final Logger logger = Logger.getLogger(RunnableClusterWrapper.class);
    private ILocalServiceResolver serviceRouter;
    private IDistributedRunnable runnableAction;

    public RunnableClusterWrapper() {
    }

    public RunnableClusterWrapper(ILocalServiceResolver serviceRouter, IDistributedRunnable runnableAction) {
        this.serviceRouter = serviceRouter;
        this.runnableAction = runnableAction;
    }

    public ILocalServiceResolver getServiceRouter() {
        return serviceRouter;
    }

    public void setServiceRouter(ILocalServiceResolver serviceRouter) {
        this.serviceRouter = serviceRouter;
    }

    public IDistributedRunnable getRunnableAction() {
        return runnableAction;
    }

    public void setRunnableAction(IDistributedRunnable runnableAction) {
        this.runnableAction = runnableAction;
    }

    @Override
    public final void run() {
        try {
            runnableAction.run(serviceRouter);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
