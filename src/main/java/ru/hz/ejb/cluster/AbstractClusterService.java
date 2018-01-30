package ru.hz.ejb.cluster;

import com.hazelcast.core.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import ru.hz.ejb.cluster.timer.DefaultTimerException;
import ru.hz.ejb.cluster.timer.actions.CallableClusterWrapper;
import ru.hz.ejb.cluster.timer.actions.IDistributedCallable;
import ru.hz.ejb.cluster.timer.actions.IDistributedRunnable;
import ru.hz.ejb.cluster.timer.actions.RunnableClusterWrapper;
import ru.hz.ejb.cluster.timer.actions.impl.EchoNodesAction;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by mvj on 01.02.2017.
 */
public abstract class AbstractClusterService implements IClusterService {
    private static final Logger logger = Logger.getLogger(AbstractClusterService.class);

    public Member getMemberByUUID(String memberUuid) {
        if (StringUtils.isEmpty(memberUuid)) {
            return null;
        }
        for (Member member : getHazelcastInstance().getCluster().getMembers()) {
            if (member.getUuid().equals(memberUuid)) {
                return member;
            }
        }
        return null;
    }

    public InetSocketAddress getAddressByUUID(String memberUUID) throws UnknownHostException {
        Member member = getMemberByUUID(memberUUID);
        return member.getAddress().getInetSocketAddress();
    }

    public Member getLocalNode() {
        return getHazelcastInstance().getCluster().getLocalMember();
    }

    public String getLocalNodeUUID() {
        return getLocalNode().getUuid();
    }

    @Override
    public void echoNodes() {
        IExecutorService executorService = getExecutorService();
        executorService.executeOnAllMembers(new RunnableClusterWrapper(getServiceRouter(), new EchoNodesAction()));
    }

    public void executeOnAllMembers(IDistributedRunnable clusterAction) {
        IExecutorService executorService = getExecutorService();
        executorService.executeOnAllMembers(new RunnableClusterWrapper(getServiceRouter(), clusterAction));
    }

    public void execute(IDistributedRunnable clusterAction) {
        IExecutorService executorService = getExecutorService();
        executorService.execute(new RunnableClusterWrapper(getServiceRouter(), clusterAction));
    }

    public void executeOnMember(IDistributedRunnable clusterAction, String memberUuid) {
        Member member = getMemberByUUID(memberUuid);
        if (member == null) {
            throw new IllegalArgumentException("Cluster node not found by uuid: " + memberUuid);
        }
        IExecutorService executorService = getExecutorService();
        executorService.executeOnMember(new RunnableClusterWrapper(getServiceRouter(), clusterAction), member);
    }

    public <T> Future<T> submitToMember(IDistributedCallable<T> callable, String memberUuid) {
        Member member = getMemberByUUID(memberUuid);
        if (member == null) {
            throw new IllegalArgumentException("Cluster node not found by uuid: " + memberUuid);
        }
        CallableClusterWrapper<T> clusterWrapper = new CallableClusterWrapper<>(getServiceRouter(), callable);
        IExecutorService executorService = getExecutorService();
        return executorService.submitToMember(clusterWrapper, member);
    }

    public <T> Future<T> submit(IDistributedCallable<T> callable) {
        IExecutorService executorService = getExecutorService();
        CallableClusterWrapper<T> clusterWrapper = new CallableClusterWrapper<>(getServiceRouter(), callable);
        return executorService.submit(clusterWrapper);
    }

    private IExecutorService getExecutorService() {
        HazelcastInstance hazelcastInstance = getHazelcastInstance();
        return hazelcastInstance.getExecutorService("defaultExecutor");
    }

    public boolean isLocal(Member member) {
        return isLocal(member.getUuid());
    }

    public boolean isLocal(String uuid) {
        return getHazelcastInstance().getCluster().getLocalMember().getUuid().equals(uuid);
    }

    public <T> T submitAndGet(IDistributedCallable<T> callable, MemberSelector memberSelector) throws DefaultTimerException {
        IExecutorService executor = getExecutorService();
        CallableClusterWrapper<T> clusterWrapper = new CallableClusterWrapper<>(getServiceRouter(), callable);
        Future<T> future = executor.submit(clusterWrapper, memberSelector);
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (!future.isCancelled()) {
                future.cancel(true);
            }
            throw new DefaultTimerException(e);
        }
    }

    @Override
    public <T> T submitAndGet(IDistributedCallable<T> callable, String memberUuid) throws DefaultTimerException {
        Member member = getMemberByUUID(memberUuid);
        if (member == null) {
            throw new IllegalArgumentException("Cluster node not found by uuid: " + memberUuid);
        }
        IExecutorService executor = getExecutorService();
        CallableClusterWrapper<T> clusterWrapper = new CallableClusterWrapper<>(getServiceRouter(), callable);
        Future<T> future = executor.submitToMember(clusterWrapper, member);
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (!future.isCancelled()) {
                future.cancel(true);
            }
            throw new DefaultTimerException(e);
        }
    }

    public ILock getLock(String lockName) {
        return getHazelcastInstance().getLock(lockName);
    }

    public Set<Member> getMembers() {
        return getHazelcastInstance().getCluster().getMembers();
    }

}
