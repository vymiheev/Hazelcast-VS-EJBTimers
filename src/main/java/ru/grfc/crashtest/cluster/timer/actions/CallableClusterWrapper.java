package ru.grfc.crashtest.cluster.timer.actions;

import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.ILocalServiceResolver;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Created by mvj on 03.12.2016.
 */
public class CallableClusterWrapper<V> implements Serializable, Callable<V> {
    private static final long serialVersionUID = 9030286914211753857L;
    private static final Logger logger = Logger.getLogger(CallableClusterWrapper.class);
    private ILocalServiceResolver serviceRouter;
    private IDistributedCallable<V> callableAction;

    public CallableClusterWrapper(ILocalServiceResolver serviceRouter, IDistributedCallable<V> callableAction) {
        this.serviceRouter = serviceRouter;
        this.callableAction = callableAction;
    }

    public CallableClusterWrapper() {
    }

    public ILocalServiceResolver getServiceRouter() {
        return serviceRouter;
    }

    public void setServiceRouter(ILocalServiceResolver serviceRouter) {
        this.serviceRouter = serviceRouter;
    }

    public IDistributedCallable<V> getCallableAction() {
        return callableAction;
    }

    public void setCallableAction(IDistributedCallable<V> callableAction) {
        this.callableAction = callableAction;
    }

    @Override
    public V call() throws Exception {
        try {
            return callableAction.call(serviceRouter);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }
}
