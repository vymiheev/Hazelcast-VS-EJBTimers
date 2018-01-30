package ru.grfc.crashtest.cluster.timer;

import com.hazelcast.map.EntryProcessor;
import ru.grfc.crashtest.cluster.IClusterService;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Created by mvj on 28.11.2016.
 */
public interface IClusterTimerManager {
    void init();

    IClusterService getClusterService();

    //container API
    Collection<TimerDescriptor> getTimers(String memberUuid);

    Collection<TimerDescriptor> getAllTimers();

    Collection<TimerDescriptor> getTimers(TimerStatus... timerStatuses);

    Collection<TimerDescriptor> getTimersByName(Collection<String> timerNames);

    void edit(EntryProcessor entryProcessor, com.hazelcast.query.Predicate predicate);

    TimerDescriptor getTimerByName(String timerName);

    boolean justAddLocalTimer(IClusteredTimer clusteredTimer, TimerStatus timerStatus);

    boolean justAddTimer(IClusteredTimer clusteredTimer, TimerStatus timerStatus, String nodeUid);

    boolean justRemoveTimer(String timerName);

    boolean isTimersExist(String memberUid);

    boolean justReplaceTimer(IClusteredTimer clusteredTimer, String fromNodeUuid, String toNodeUuid, TimerStatus timerStatus);

    boolean justChangeTimerStatus(TimerDescriptor timerDescriptor, TimerStatus status);

    //analyzer API
    Map<String, Collection<TimerState>> getInvalidTimers() throws DefaultTimerException;

    boolean hasInvalidTimers() throws DefaultTimerException;

    Map<String, Collection<Serializable>> getCommonTimers() throws DefaultTimerException;

    boolean hasCommonTimers() throws DefaultTimerException;

    //migration API
    void migrateAllTimers(String fromNodeUUID) throws DefaultTimerException;

    void migrateAllTimers(String fromNodeUUID, String toNodeUUID) throws DefaultTimerException;

    void migrateTimer(String timerName) throws DefaultTimerException;

    void migrateTimer(String timerName, String toNodeUUID) throws DefaultTimerException;

    void migrateTimer(TimerDescriptor timerDescriptor, String toNodeUUID) throws DefaultTimerException;

    //launcher API
    boolean createTimer(ScheduledTimer clusteredTimer) throws DefaultTimerException;

    boolean createTimer(DatedTimer clusteredTimer) throws DefaultTimerException;

    void startAllTimers() throws DefaultTimerException;

    void startAllTimers(String nodeUUID) throws DefaultTimerException;

    void startTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException;

    void startTimers(Collection<TimerDescriptor> timerDescriptors) throws DefaultTimerException;

    void startTimerByName(String timerName) throws DefaultTimerException;

    void startTimersByName(Collection<String> timerNames) throws DefaultTimerException;

    void justCreateTimer(IClusteredTimer clusteredTimer, String nodeUid) throws DefaultTimerException;

    void justCreateTimers(Map<String, Collection<IClusteredTimer>> timerNodeUids) throws DefaultTimerException;

    //destroyer API
    void stopTimerByName(String timerName) throws DefaultTimerException;

    void stopTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException;

    void stopAllTimers() throws DefaultTimerException;

    void stopAllTimers(String nodeUUID) throws DefaultTimerException;

    Map<TimerDescriptor, Boolean> stopTimers(Collection<TimerDescriptor> timerDescriptors) throws DefaultTimerException;

    Map<TimerDescriptor, Boolean> stopTimersByName(Collection<String> timerNames) throws DefaultTimerException;

    boolean justStopTimer(String timerName) throws DefaultTimerException;

    boolean justStopTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException;

    Map<TimerDescriptor, Boolean> justStopTimers(Collection<String> timerNames);

    Map<TimerDescriptor, Boolean> justStopTimer(Collection<TimerDescriptor> timerDescriptors);

    boolean justStopTimerOnNode(String timerName, String nodeUid);

    Map<TimerNodeInfo, Boolean> justStopTimerOnNode(Map<String, Collection<String>> timerNodes);

    boolean justStopTimerEverywhere(String timerName);

    Map<TimerNodeInfo, Boolean> justStopTimersByNameEverywhere(Collection<String> timerNames);


    void close();

    <T> T unwrap(Class<T> controllerClazz);

}
