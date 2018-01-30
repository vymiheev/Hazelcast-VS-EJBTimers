package ru.grfc.crashtest.cluster.timer;

import java.util.Collection;
import java.util.Map;

/**
 * Created by mvj on 24.01.2017.
 */
public interface IClusterTimerLauncher {
    boolean isAllowed(IClusteredTimer clusteredTimer);

    boolean createTimer(IClusteredTimer clusteredTimer) throws DefaultTimerException;

    Map<TimerDescriptor, Boolean> startAllTimers() throws DefaultTimerException;

    Map<TimerDescriptor, Boolean> startAllTimers(String nodeUUID) throws DefaultTimerException;

    void startTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException;

    Map<TimerDescriptor, Boolean> startTimers(Collection<TimerDescriptor> timerDescriptors) throws DefaultTimerException;

    void startTimerByName(String timerName) throws DefaultTimerException;

    Map<TimerDescriptor, Boolean> startTimersByName(Collection<String> timerNames) throws DefaultTimerException;

    void justCreateTimer(IClusteredTimer clusteredTimer, String nodeUid) throws DefaultTimerException;

    void justCreateTimers(Map<String, Collection<IClusteredTimer>> timerNodeUids) throws DefaultTimerException;

    Map<TimerDescriptor, Boolean> startLocalTimers(Collection<TimerDescriptor> timerDescriptors) throws DefaultTimerException;

    void startLocalTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException;

    Map<IClusteredTimer, Boolean> justCreateLocalTimers(Collection<IClusteredTimer> timerToCreate);

    void justCreateLocalTimer(IClusteredTimer clusteredTimer) throws DefaultTimerException;

    void justCreateLocalTimer(ITimerLauncherResolver launcherResolver, IClusteredTimer clusteredTimer);
}
