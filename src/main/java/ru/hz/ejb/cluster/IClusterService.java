package ru.hz.ejb.cluster;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberSelector;
import ru.hz.ejb.cluster.timer.DefaultTimerException;
import ru.hz.ejb.cluster.timer.IClusterTimerManager;
import ru.hz.ejb.cluster.timer.actions.IDistributedCallable;
import ru.hz.ejb.cluster.timer.actions.IDistributedRunnable;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by mvj on 31.01.2017.
 */
public interface IClusterService {
    HazelcastInstance getHazelcastInstance();

    Member getMemberByUUID(String memberUuid);

    InetSocketAddress getAddressByUUID(String memberUUID) throws UnknownHostException;

    Member getLocalNode();

    String getLocalNodeUUID();

    void echoNodes();

    void executeOnAllMembers(IDistributedRunnable clusterAction);

    void execute(IDistributedRunnable clusterAction);

    void executeOnMember(IDistributedRunnable clusterAction, String memberUuid);

    <T> Future<T> submitToMember(IDistributedCallable<T> callable, String memberUuid);

    <T> Future<T> submit(IDistributedCallable<T> callable);

    ILocalServiceResolver getServiceRouter();

    boolean isLocal(Member member);

    boolean isLocal(String uuid);

    <T> T submitAndGet(IDistributedCallable<T> callable, MemberSelector memberSelector) throws DefaultTimerException;

    <T> T submitAndGet(IDistributedCallable<T> callable, String memberUuid) throws DefaultTimerException;

    Set<Member> getMembers();

    ILock getLock(String lockName);

    IClusterTimerManager getClusterTimerManager();

    void init(NetworkConfiguration networkConfiguration);

    NetworkConfiguration getNetworkConfiguration();

    void shutdown();
}
