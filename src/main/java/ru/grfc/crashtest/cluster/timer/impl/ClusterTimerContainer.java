package ru.grfc.crashtest.cluster.timer.impl;

import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.hazelcast.core.Member;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.query.Predicates;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.timer.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by mvj on 14.11.2016.
 */
public class ClusterTimerContainer implements IClusterTimerContainer {
    private static final Logger logger = Logger.getLogger(ClusterTimerContainer.class);
    //The data will be stored in deserialized form.
    //This configuration is the default choice since the data replication is mostly used for high speed access
    private final IMap<String, TimerDescriptor> timerMemberMap;
    private final ISet<AbstractTimerLauncherResolver> timerLaunchers;

    ClusterTimerContainer(IMap<String, TimerDescriptor> timerMemberMap, ISet<AbstractTimerLauncherResolver> timerLaunchers) {
        this.timerMemberMap = timerMemberMap;
        this.timerLaunchers = timerLaunchers;
    }

    public Set<AbstractTimerLauncherResolver> getTimerLaunchers() {
        return timerLaunchers;
    }

    /**
     * Один и тот же таймер на разные сервера добавить нельзя!
     *
     * @param clusteredTimer инфо таймер
     * @param nodeUid        сервер кластера
     * @return null if timer had already exist
     */
    public boolean addTimer(IClusteredTimer clusteredTimer, TimerStatus timerStatus, String nodeUid) {
        //All ConcurrentMap operations such as put and remove might wait if the key is locked by another thread in the local or remote JVM.
        TimerDescriptor timerDescriptor = new TimerDescriptor(clusteredTimer, nodeUid, timerStatus);
        //The action is performed atomically
        TimerDescriptor descriptor = timerMemberMap.putIfAbsent(clusteredTimer.getTimerInfo().getTimerName(), timerDescriptor);

        timerLaunchers.add(clusteredTimer.getLauncherResolver());
        return descriptor == null;
    }

    public boolean contains(String timerName) {
        return timerMemberMap.containsKey(timerName);
    }

    public boolean removeTimer(String timerName) {
        //All ConcurrentMap operations such as put and remove might wait if the key is locked by another thread in the local or remote JVM.
        return timerMemberMap.remove(timerName) != null;
    }

/*    public void close() {
        logger.debug("Clear timerMemberMap!");
        try {
            timerMemberMap.clear();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }*/

    public boolean isTimersExist(String uuid) {
        Set<String> keySet = timerMemberMap.keySet(Predicates.equal("nodeUuid", uuid));
        return CollectionUtils.isNotEmpty(keySet);
    }

    public Collection<TimerDescriptor> getTimers(TimerStatus... timerStatuses) {
        return timerMemberMap.values(Predicates.in("timerStatus", timerStatuses));
    }

    public Collection<TimerDescriptor> getTimers(Member fromMember) {
        return getTimers(fromMember.getUuid());
    }

    public Collection<TimerDescriptor> getTimers(String uuid) {
        return timerMemberMap.values(Predicates.equal("nodeUuid", uuid));
    }

    public Collection<TimerDescriptor> getTimers(String uuid, TimerStatus... timerStatuses) {
        return timerMemberMap.values(Predicates.and(Predicates.equal("nodeUuid", uuid), Predicates.in("timerStatus", timerStatuses)));
    }

    public Collection<TimerDescriptor> getAllTimers() {
        return timerMemberMap.values();
    }

    public boolean migrateTimer(TimerDescriptor previousDescriptor, String toNodeUuid) {
        //In Hazelcast, you can apply the optimistic locking strategy with the map replace method.
        TimerDescriptor createdDescriptor = new TimerDescriptor(previousDescriptor.getClusteredTimer(),
                toNodeUuid, previousDescriptor.getTimerStatus());
        return timerMemberMap.replace(previousDescriptor.getClusteredTimer().getTimerInfo().getTimerName(), previousDescriptor, createdDescriptor);
    }

    public boolean replaceTimer(IClusteredTimer clusteredTimer, String fromNodeUuid, String toNodeUuid, TimerStatus timerStatus) {
        //In Hazelcast, you can apply the optimistic locking strategy with the map replace method.
        return timerMemberMap.replace(clusteredTimer.getTimerInfo().getTimerName(),
                new TimerDescriptor(clusteredTimer, fromNodeUuid),
                new TimerDescriptor(clusteredTimer, toNodeUuid, timerStatus));
    }

    public boolean changeTimerStatus(TimerDescriptor previousDescriptor, TimerStatus status) {
        //In Hazelcast, you can apply the optimistic locking strategy with the map replace method.
        TimerDescriptor createdDescriptor = new TimerDescriptor(previousDescriptor.getClusteredTimer(), previousDescriptor.getNodeUuid(), status);
        return timerMemberMap.replace(previousDescriptor.getClusteredTimer().getTimerInfo().getTimerName(), previousDescriptor, createdDescriptor);
    }

    public void edit(EntryProcessor entryProcessor, com.hazelcast.query.Predicate predicate) {
        timerMemberMap.executeOnEntries(entryProcessor, predicate);
    }

    public TimerDescriptor getTimerByName(String timerName) {
        return timerMemberMap.get(timerName);
    }

    @Override
    public Collection<TimerDescriptor> getTimersByName(Collection<String> timerNames) {
        List<TimerDescriptor> timerDescriptors = new ArrayList<>();
        for (String timerName : timerNames) {
            TimerDescriptor timerDescriptor = getTimerByName(timerName);
            if (timerDescriptor != null) {
                timerDescriptors.add(timerDescriptor);
            } else {
                logger.warn("Timer: " + timerName + " not found!");
            }
        }
        return timerDescriptors;
    }

    /**
     * Tries to acquire the lock for the specified key for the specified lease time.
     * After lease time, the lock will be released.
     * <p>
     * If the lock is not available, then the current thread becomes disabled for thread scheduling purposes and lies dormant until one of two things happens:
     * <p>
     * the lock is acquired by the current thread, or
     * the specified waiting time elapses.
     *
     * @param key - key to lock in this map.
     * @return true if the lock was acquired and false if the waiting time elapsed before the lock was acquired.
     */
    public boolean tryLock(String key) throws InterruptedException {
        return timerMemberMap.tryLock(key, 5, TimeUnit.SECONDS, 5, TimeUnit.SECONDS);
    }

    public void unlock(String timerName) {
        timerMemberMap.unlock(timerName);
    }
}
