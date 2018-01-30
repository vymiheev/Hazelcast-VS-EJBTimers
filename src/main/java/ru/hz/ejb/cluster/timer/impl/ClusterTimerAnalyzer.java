package ru.hz.ejb.cluster.timer.impl;

import com.hazelcast.core.Member;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import ru.hz.ejb.cluster.timer.actions.impl.FetchCommonTimersAction;
import ru.hz.ejb.cluster.timer.actions.impl.FetchInvalidTimersAction;
import ru.hz.ejb.cluster.timer.*;

import java.io.Serializable;
import java.util.*;

/**
 * Created by mvj on 28.01.2017.
 */
public class ClusterTimerAnalyzer extends AbstractTimerController implements IClusterTimerAnalyzer {
    private static final Logger logger = Logger.getLogger(ClusterTimerAnalyzer.class);

    ClusterTimerAnalyzer(ClusterTimerManager timerManager) {
        super(timerManager);
    }

    public Collection<TimerState> getLocalInvalidTimers() {
        Collection<TimerInfo> commonTimers = getLocalTimersInfo();
        Collection<TimerDescriptor> timerDescriptors = getTimerContainer().getTimers(getClusterService().getLocalNodeUUID());

        Collection<TimerInfo> containerTimers = new ArrayList<>();
        for (TimerDescriptor timerDescriptor : timerDescriptors) {
            if (timerDescriptor.getTimerStatus() == TimerStatus.RUNNING) {
                containerTimers.add(timerDescriptor.getClusteredTimer().getTimerInfo());
            }
        }

        List<TimerState> timerStates = new ArrayList<>();
        Collection<TimerInfo> timerInfos = CollectionUtils.subtract(containerTimers, commonTimers);
        for (TimerInfo timerInfo : timerInfos) {
            timerStates.add(new TimerState(timerInfo, TimerStateType.INSIDE_CONTAINER));
        }
        timerInfos = CollectionUtils.subtract(commonTimers, containerTimers);
        for (TimerInfo timerInfo : timerInfos) {
            timerStates.add(new TimerState(timerInfo, TimerStateType.OUTSIDE_CONTAINER));
        }

        return timerStates;
    }

    public Map<String, Collection<TimerState>> getInvalidTimers() throws DefaultTimerException {
        String localNodeUUID = getClusterService().getLocalNodeUUID();
        Map<String, Collection<TimerState>> timerStates = new HashMap<>();
        Collection<TimerState> localInvalidTimers = getLocalInvalidTimers();
        if (CollectionUtils.isNotEmpty(localInvalidTimers)) {
            timerStates.put(localNodeUUID, localInvalidTimers);
        }

        FetchInvalidTimersAction timerAction = new FetchInvalidTimersAction();
        for (Member member : getClusterService().getMembers()) {
            if (member.getUuid().equals(localNodeUUID)) {
                continue;
            }
            try {
                Collection<TimerState> invalidTimers =
                        getClusterService().submitAndGet(timerAction, it -> it.getUuid().equals(member.getUuid()));
                if (CollectionUtils.isNotEmpty(invalidTimers)) {
                    timerStates.put(member.getUuid(), invalidTimers);
                }
            } catch (DefaultTimerException e) {
                logger.error(e.getMessage(), e);
                throw e;
            }
        }

        return timerStates;
    }

    public boolean hasInvalidTimers() throws DefaultTimerException {
        return !getInvalidTimers().isEmpty();
    }

    public Collection<Serializable> getLocalCommonTimers() {
        Collection<ISimpleTimer> timers = getLocalTimers();
        List<Serializable> serializableList = new ArrayList<>();
        if (CollectionUtils.isEmpty(timers)) {
            return serializableList;
        }
        for (ISimpleTimer tim : timers) {
            if (tim.getInfo() != null && !(tim.getInfo() instanceof TimerInfo)) {
                serializableList.add(tim.getInfo());
            }
        }
        return serializableList;
    }

    public Map<String, Collection<Serializable>> getCommonTimers() throws DefaultTimerException {
        Collection<Serializable> timers = getLocalCommonTimers();
        Map<String, Collection<Serializable>> map = new HashMap<>();
        String localNodeUUID = getClusterService().getLocalNodeUUID();
        if (CollectionUtils.isNotEmpty(timers)) {
            map.put(localNodeUUID, timers);
        }

        FetchCommonTimersAction timerAction = new FetchCommonTimersAction();

        for (Member member : getClusterService().getMembers()) {
            if (member.getUuid().equals(localNodeUUID)) {
                continue;
            }
            try {
                Collection<Serializable> resultList = getClusterService().submitAndGet(timerAction,
                        it -> it.getUuid().equals(member.getUuid()));
                if (CollectionUtils.isNotEmpty(resultList)) {
                    map.put(member.getUuid(), resultList);
                }
            } catch (DefaultTimerException e) {
                logger.error(e.getMessage(), e);
                throw e;
            }
        }
        return map;
    }

    public boolean hasCommonTimers() throws DefaultTimerException {
        return !getCommonTimers().isEmpty();
    }

    public Collection<ISimpleTimer> getLocalTimers() {
        Collection<ISimpleTimer> all = new ArrayList<>();
        for (ITimerLauncherResolver launcherResolver : getTimerContainer().getTimerLaunchers()) {
            ITimerLauncher timerLauncher = launcherResolver.lookup();
            Collection<ISimpleTimer> timers = timerLauncher.getTimers();
            all.addAll(timers);
        }
        return all;
    }

    public Collection<TimerInfo> getLocalTimersInfo() {
        Collection<TimerInfo> all = new ArrayList<>();
        for (ITimerLauncherResolver launcherResolver : getTimerContainer().getTimerLaunchers()) {
            ITimerLauncher timerLauncher = launcherResolver.lookup();
            Collection<TimerInfo> timerInfos = timerLauncher.getTimersInfo();
            all.addAll(timerInfos);
        }
        return all;
    }
}
