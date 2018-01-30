package ru.grfc.crashtest.cluster.timer;

import java.util.Collection;
import java.util.Map;

/**
 * Created by mvj on 24.01.2017.
 */
public interface IClusterTimerDestroyer {
    Map<TimerDescriptor, Boolean> stopLocalTimers(Collection<TimerDescriptor> timerDescriptors);

    void stopLocalTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException;


    void stopTimerByName(String timerName) throws DefaultTimerException;

    void stopTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException;

    Map<TimerDescriptor, Boolean> stopAllTimers() throws DefaultTimerException;

    Map<TimerDescriptor, Boolean> stopAllTimers(String nodeUUID) throws DefaultTimerException;

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


    boolean justStopLocalTimer(IClusteredTimer clusteredTimer);

    boolean justStopLocalTimer(ITimerLauncherResolver launcherResolver, TimerInfo timerInfo);

    Map<TimerDescriptor, Boolean> justStopLocalTimers(Collection<TimerDescriptor> timerDescriptors);

    boolean justStopLocalTimerEverywhere(String timerName);
}
