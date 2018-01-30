package ru.grfc.crashtest.cluster.timer;

import com.hazelcast.core.Member;
import com.hazelcast.map.EntryProcessor;

import java.util.Collection;

/**
 * Created by mvj on 25.01.2017.
 */
public interface IClusterTimerContainer {

    Collection<TimerDescriptor> getTimers(String memberUuid);

    Collection<TimerDescriptor> getTimers(Member fromMember);

    Collection<TimerDescriptor> getTimers(TimerStatus... timerStatuses);

    Collection<TimerDescriptor> getAllTimers();

    boolean addTimer(IClusteredTimer clusteredTimer, TimerStatus timerStatus, String nodeUid);

    boolean contains(String timerName);

    boolean removeTimer(String timerName);

    boolean isTimersExist(String member);

    boolean migrateTimer(TimerDescriptor previousDescriptor, String toNodeUuid);

    boolean replaceTimer(IClusteredTimer clusteredTimer, String fromNodeUuid, String toNodeUuid, TimerStatus timerStatus);

    boolean changeTimerStatus(TimerDescriptor previousDescriptor, TimerStatus status);

    void edit(EntryProcessor entryProcessor, com.hazelcast.query.Predicate predicate);

    TimerDescriptor getTimerByName(String timerName);

    Collection<TimerDescriptor> getTimersByName(Collection<String> timerName);

    boolean tryLock(String key) throws InterruptedException;

    void unlock(String timerName);
}
