package ru.grfc.crashtest.cluster.timer.impl;

import com.hazelcast.core.Member;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.IClusterService;
import ru.grfc.crashtest.cluster.timer.IClusteredTimer;
import ru.grfc.crashtest.cluster.timer.TimerDescriptor;
import ru.grfc.crashtest.cluster.timer.TimerNodeInfo;
import ru.grfc.crashtest.cluster.timer.TimerStatus;

import java.util.*;

/**
 * Created by mvj on 26.01.2017.
 */
public abstract class AbstractTimerController {
    private static final Logger logger = Logger.getLogger(AbstractTimerController.class);
    protected ClusterTimerManager timerManager;

    AbstractTimerController(ClusterTimerManager timerManager) {
        this.timerManager = timerManager;
    }

    Map<Member, Collection<TimerDescriptor>> getStateDescriptorsMap(Collection<TimerDescriptor> timerDescriptors, TimerStatus... timerStatuses) {
        Map<Member, Collection<TimerDescriptor>> timerDescriptorMap = new HashMap<>();
        for (TimerDescriptor timerDescriptor : timerDescriptors) {
            if (ArrayUtils.isEmpty(timerStatuses) || ArrayUtils.contains(timerStatuses, timerDescriptor.getTimerStatus())) {
                Member member = getClusterService().getMemberByUUID(timerDescriptor.getNodeUuid());
                if (timerDescriptorMap.containsKey(member)) {
                    Collection<TimerDescriptor> descriptors = timerDescriptorMap.get(member);
                    descriptors.add(timerDescriptor);
                } else {
                    List<TimerDescriptor> descriptors = new ArrayList<>();
                    descriptors.add(timerDescriptor);
                    timerDescriptorMap.put(member, descriptors);
                }
            }
        }
        return timerDescriptorMap;
    }

    Collection<TimerDescriptor> getTimersState(Collection<TimerDescriptor> timerDescriptors, TimerStatus... timerStatuses) {
        List<TimerDescriptor> runningDescriptors = new ArrayList<>(timerDescriptors);
        Iterator<TimerDescriptor> iterator = runningDescriptors.iterator();
        while (iterator.hasNext()) {
            TimerDescriptor timerDescriptor = iterator.next();
            if (!ArrayUtils.contains(timerStatuses, timerDescriptor.getTimerStatus())) {
                iterator.remove();
            }
        }
        return runningDescriptors;
    }

    String findSuitableNode(String fromNodeUUID) {
        String toNodeUuid = null;
        String localNodeUuid = getClusterService().getLocalNodeUUID();
        if (fromNodeUUID.equals(localNodeUuid)) {
            for (Member member : getClusterService().getMembers()) {
                if (!member.getUuid().equals(fromNodeUUID)) {
                    toNodeUuid = member.getUuid();
                    break;
                }
            }
        } else {
            toNodeUuid = localNodeUuid;
        }
        return toNodeUuid;
    }

    IClusterService getClusterService() {
        return timerManager.getClusterService();
    }

    boolean isContains(Map<TimerNodeInfo, Boolean> map, String timerName) {
        for (TimerNodeInfo timerNodeInfo : map.keySet()) {
            if (timerNodeInfo.getTimerName().equals(timerName)) {
                return true;
            }
        }
        return false;
    }

    ClusterTimerContainer getTimerContainer() {
        return timerManager.getTimerContainer();
    }

    String getTimerName(TimerDescriptor timerDescriptor) {
        return getTimerName(timerDescriptor.getClusteredTimer());
    }

    String getTimerName(IClusteredTimer clusteredTimer) {
        return clusteredTimer.getTimerInfo().getTimerName();
    }
}
