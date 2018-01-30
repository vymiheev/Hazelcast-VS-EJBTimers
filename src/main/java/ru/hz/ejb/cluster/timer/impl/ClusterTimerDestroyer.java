package ru.hz.ejb.cluster.timer.impl;

import com.hazelcast.core.Member;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import ru.hz.ejb.cluster.timer.actions.impl.JustStopTimerAction;
import ru.hz.ejb.cluster.timer.actions.impl.JustStopTimerDescriptorAction;
import ru.hz.ejb.cluster.timer.actions.impl.StopBulkTimerAction;
import ru.hz.ejb.cluster.timer.*;

import java.util.*;

/**
 * Created by mvj on 26.01.2017.
 */
public class ClusterTimerDestroyer extends AbstractTimerController implements IClusterTimerDestroyer {
    private static final Logger logger = Logger.getLogger(ClusterTimerDestroyer.class);

    ClusterTimerDestroyer(ClusterTimerManager serverManager) {
        super(serverManager);
    }

    public Map<TimerDescriptor, Boolean> stopLocalTimers(Collection<TimerDescriptor> timerDescriptors) {
        Collection<TimerDescriptor> runningTimers = getTimersState(timerDescriptors, TimerStatus.RUNNING);
        if (CollectionUtils.isEmpty(runningTimers)) {
            logger.debug("Empty running timers param!");
            return new HashMap<>();
        }
        Collection<TimerDescriptor> descriptors = CollectionUtils.subtract(timerDescriptors, runningTimers);
        Map<TimerDescriptor, Boolean> descriptorBooleanMap = new HashMap<>();
        for (TimerDescriptor timerDescriptor : descriptors) {
            descriptorBooleanMap.put(timerDescriptor, false);
        }

        Map<TimerDescriptor, Boolean> result = stopLocalTimersInternallyLock(runningTimers);
        result.putAll(descriptorBooleanMap);
        return result;
    }

    public void stopLocalTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        if (!timerDescriptor.getTimerStatus().equals(TimerStatus.RUNNING)) {
            throw new DefaultTimerException("Timer is in " + timerDescriptor.getTimerStatus().name() + " state!");
        }
        if (!getClusterService().isLocal(timerDescriptor.getNodeUuid())) {
            throw new DefaultTimerException("Timer " + timerDescriptor.getTimerStatus().name() + " is not local");
        }

        ITimerLauncher timerLauncher = timerDescriptor.getClusteredTimer().getLauncherResolver().lookup();
        stopLocalTimerInternallyLock(timerLauncher, timerDescriptor);
    }

    public void stopTimerByName(String timerName) throws DefaultTimerException {
        TimerDescriptor timerDescriptor = getTimerContainer().getTimerByName(timerName);
        if (timerDescriptor == null) {
            throw new DefaultTimerException("There is no timer with name: " + timerName);
        }
        stopTimer(timerDescriptor);
    }

    public void stopTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        if (!timerDescriptor.getTimerStatus().equals(TimerStatus.RUNNING)) {
            throw new DefaultTimerException("Timer is in " + timerDescriptor.getTimerStatus().name() + " state!");
        }

        if (getClusterService().isLocal(timerDescriptor.getNodeUuid())) {
            ITimerLauncher timerLauncher = timerDescriptor.getClusteredTimer().getLauncherResolver().lookup();
            stopLocalTimerInternallyLock(timerLauncher, timerDescriptor);
        } else {
            stopBulkRemoteTimersInternally(Collections.singletonList(timerDescriptor), timerDescriptor.getNodeUuid());
        }
    }

    public Map<TimerDescriptor, Boolean> stopAllTimers() throws DefaultTimerException {
        Collection<TimerDescriptor> timerDescriptors = getTimerContainer().getTimers(TimerStatus.RUNNING);
        if (CollectionUtils.isEmpty(timerDescriptors)) {
            logger.debug("Empty running timers param!");
            return new HashMap<>();
        }
        return stopTimers(timerDescriptors);
    }

    public Map<TimerDescriptor, Boolean> stopAllTimers(String nodeUUID) throws DefaultTimerException {
        Collection<TimerDescriptor> timerDescriptors = getTimerContainer().getTimers(nodeUUID, TimerStatus.RUNNING);
        if (CollectionUtils.isEmpty(timerDescriptors)) {
            logger.debug("Empty running timers param!");
            return new HashMap<>();
        }
        return stopTimers(timerDescriptors);
    }

    public Map<TimerDescriptor, Boolean> stopTimersByName(Collection<String> timerNames) throws DefaultTimerException {
        List<TimerDescriptor> timerDescriptors = new ArrayList<>();
        for (String timerName : timerNames) {
            TimerDescriptor timerDescriptor = getTimerContainer().getTimerByName(timerName);
            if (timerDescriptor != null) {
                timerDescriptors.add(timerDescriptor);
            }
        }
        return stopTimers(timerDescriptors);
    }

    public Map<TimerDescriptor, Boolean> stopTimers(Collection<TimerDescriptor> timerDescriptors) throws DefaultTimerException {
        Map<Member, Collection<TimerDescriptor>> timerDescriptorMap = getStateDescriptorsMap(timerDescriptors);
        if (MapUtils.isEmpty(timerDescriptorMap)) {
            throw new DefaultTimerException("There are no timers to stop!");
        }

        return stopBulkTimerInternally(timerDescriptorMap);
    }

    private Map<TimerDescriptor, Boolean> stopBulkTimerInternally(Map<Member, Collection<TimerDescriptor>> timerDescriptors) {
        Map<TimerDescriptor, Boolean> descriptorBooleanMap = new HashMap<>();
        for (Map.Entry<Member, Collection<TimerDescriptor>> entry : timerDescriptors.entrySet()) {
            try {
                Map<TimerDescriptor, Boolean> resultMap;
                if (getClusterService().isLocal(entry.getKey())) {
                    resultMap = stopLocalTimersInternallyLock(entry.getValue());
                } else {
                    resultMap = stopBulkRemoteTimersInternally(entry.getValue(), entry.getKey().getUuid());
                }
                descriptorBooleanMap.putAll(resultMap);
            } catch (DefaultTimerException e) {
                logger.error("Some errors were detected while stopping timers on cluster member: " + entry.getKey().getUuid());
                logger.error(e.getMessage(), e);
                for (TimerDescriptor timerDescriptor : entry.getValue()) {
                    descriptorBooleanMap.put(timerDescriptor, false);
                }
            }
        }

        return descriptorBooleanMap;
    }

    private Map<TimerDescriptor, Boolean> stopBulkRemoteTimersInternally(Collection<TimerDescriptor> timerDescriptors, String nodeUuid) throws DefaultTimerException {
        logger.debug("Stop timers remotely...");
        StopBulkTimerAction timerAction = new StopBulkTimerAction();
        timerAction.setTimers(timerDescriptors);
        return getClusterService().submitAndGet(timerAction, it -> it.getUuid().equals(nodeUuid));
    }

    public Map<TimerDescriptor, Boolean> stopLocalTimersInternallyLock(Collection<TimerDescriptor> timerDescriptors) {
        Map<TimerDescriptor, Boolean> descriptorBooleanMap = new HashMap<>();

        Map<AbstractTimerLauncherResolver, ITimerLauncher> resolverNameLauncherMap = new HashMap<>();
        for (TimerDescriptor timerDescriptor : timerDescriptors) {
            try {
                AbstractTimerLauncherResolver launcherResolver = timerDescriptor.getClusteredTimer().getLauncherResolver();
                ITimerLauncher timerLauncher;
                if (resolverNameLauncherMap.containsKey(launcherResolver)) {
                    timerLauncher = resolverNameLauncherMap.get(launcherResolver);
                } else {
                    timerLauncher = launcherResolver.lookup();
                    resolverNameLauncherMap.put(launcherResolver, timerLauncher);
                }
                stopLocalTimerInternallyLock(timerLauncher, timerDescriptor);
                descriptorBooleanMap.put(timerDescriptor, true);
            } catch (DefaultTimerException e) {
                logger.error("Failed to stop: " + getTimerName(timerDescriptor));
                logger.error(e.getMessage(), e);
                descriptorBooleanMap.put(timerDescriptor, false);
            }
        }
        return descriptorBooleanMap;
    }

    private void stopLocalTimerInternallyLock(ITimerLauncher timerLauncher, TimerDescriptor timerDescriptor) throws DefaultTimerException {
        boolean isAcquired = false;
        try {
            //Используется пессимистическая блокировка т.к. остановка таймера и errorHandling должны выполняться атомарно
            isAcquired = getTimerContainer().tryLock(getTimerName(timerDescriptor));
            if (isAcquired) {
                stopLocalTimerInternally(timerLauncher, timerDescriptor);
            } else {
                throw new DefaultTimerException("Key: " + getTimerName(timerDescriptor) + " is locked!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (e instanceof DefaultTimerException) {
                throw (DefaultTimerException) e;
            }
            throw new DefaultTimerException(e);
        } finally {
            if (isAcquired) {
                getTimerContainer().unlock(getTimerName(timerDescriptor));
            }
        }
    }

    private void stopLocalTimerInternally(ITimerLauncher timerLauncher, TimerDescriptor timerDescriptor) throws DefaultTimerException {
        logger.info("Stop timer " + getTimerName(timerDescriptor) + " on local node.");
        boolean isStatusChanged = false;
        try {
            isStatusChanged = getTimerContainer().changeTimerStatus(timerDescriptor, TimerStatus.STOPPED);
            if (isStatusChanged) {
                if (!justStopLocalTimer(timerLauncher, timerDescriptor.getClusteredTimer().getTimerInfo())) {
                    throw new DefaultTimerException("Timer with name: " + getTimerName(timerDescriptor) + " had not been found locally.");
                }
            } else {
                throw new DefaultTimerException("Failed to change timer status.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (isStatusChanged) {
                try {
                    TimerDescriptor descriptor = new TimerDescriptor(timerDescriptor.getClusteredTimer(), timerDescriptor.getNodeUuid(), TimerStatus.STOPPED);
                    if (getTimerContainer().changeTimerStatus(descriptor, timerDescriptor.getTimerStatus())) {
                        logger.debug("Timer change status to previous: " + timerDescriptor.getTimerStatus().name());
                    } else {
                        logger.error("Timer status had not been changed!");
                    }
                } catch (Exception ee) {
                    logger.error(ee.getMessage(), ee);
                }
            }
            if (e instanceof DefaultTimerException) {
                throw (DefaultTimerException) e;
            }
            throw new DefaultTimerException(e);
        }
    }


    //Without Locks ---------------------------------------------------------------------------------------------------

    public boolean justStopTimer(String timerName) throws DefaultTimerException {
        TimerDescriptor timerDescriptor = getTimerContainer().getTimerByName(timerName);
        return justStopTimer(timerDescriptor);
    }

    /**
     * Send 'stop' signal to timerDescriptor's node
     *
     * @param timerDescriptor to stop
     * @return is success
     * @throws DefaultTimerException
     */
    public boolean justStopTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        if (getClusterService().isLocal(timerDescriptor.getNodeUuid())) {
            return justStopLocalTimer(timerDescriptor.getClusteredTimer());
        } else {
            JustStopTimerDescriptorAction timerAction = new JustStopTimerDescriptorAction();
            timerAction.getTimerToDestroy().add(timerDescriptor);
            logger.debug("Trying to stop timer: " + getTimerName(timerDescriptor) + " on node: " + timerDescriptor.getNodeUuid());
            Map<TimerDescriptor, Boolean> result = getClusterService().submitAndGet(timerAction, member -> member.getUuid().equals(timerDescriptor.getNodeUuid()));
            return result.get(timerDescriptor);
        }
    }

    public Map<TimerDescriptor, Boolean> justStopTimers(Collection<String> timerNames) {
        Collection<TimerDescriptor> timerDescriptors = getTimerContainer().getTimersByName(timerNames);
        return justStopTimer(timerDescriptors);
    }

    public Map<TimerDescriptor, Boolean> justStopTimer(Collection<TimerDescriptor> timerDescriptors) {
        Map<Member, Collection<TimerDescriptor>> timerDescriptorMap = getStateDescriptorsMap(timerDescriptors);
        Map<TimerDescriptor, Boolean> descriptorBooleanMap = new HashMap<>();
        for (Map.Entry<Member, Collection<TimerDescriptor>> entry : timerDescriptorMap.entrySet()) {
            try {
                Map<TimerDescriptor, Boolean> resultMap;
                if (getClusterService().isLocal(entry.getKey())) {
                    resultMap = justStopLocalTimers(entry.getValue());
                } else {
                    JustStopTimerDescriptorAction timerAction = new JustStopTimerDescriptorAction();
                    timerAction.getTimerToDestroy().addAll(entry.getValue());
                    resultMap = getClusterService().submitAndGet(timerAction, member -> member.getUuid().equals(entry.getKey().getUuid()));
                }
                descriptorBooleanMap.putAll(resultMap);
            } catch (DefaultTimerException e) {
                logger.error("Some errors were detected while stopping timers on cluster member: " + entry.getKey().getUuid());
                logger.error(e.getMessage(), e);
                for (TimerDescriptor timerDescriptor : entry.getValue()) {
                    descriptorBooleanMap.put(timerDescriptor, false);
                }
            }
        }
        return descriptorBooleanMap;
    }

    //Stop timers ignoring HZ Container layer -------------------------------------------------------------------------

    public boolean justStopTimerOnNode(String timerName, String nodeUid) {
        if (getClusterService().isLocal(nodeUid)) {
            return justStopLocalTimerEverywhere(timerName);
        } else {
            return justStopTimerOnNodeInternally(timerName, Collections.singletonList(nodeUid));
        }
    }

    public Map<TimerNodeInfo, Boolean> justStopTimerOnNode(Map<String, Collection<String>> timerNodes) {
        Map<TimerNodeInfo, Boolean> map = new HashMap<>();
        for (Map.Entry<String, Collection<String>> entry : timerNodes.entrySet()) {
            Map<TimerNodeInfo, Boolean> resultMap = justStopTimerOnNodeInternally(entry.getValue(), Collections.singleton(entry.getKey()));
            map.putAll(resultMap);
        }
        return map;
    }

    public boolean justStopTimerEverywhere(String timerName) {
        Collection<String> membersUid = CollectionUtils.collect(getClusterService().getMembers(), o -> {
            return ((Member) o).getUuid();
        });
        return justStopTimerOnNodeInternally(timerName, membersUid);
    }

    public Map<TimerNodeInfo, Boolean> justStopTimersByNameEverywhere(Collection<String> timerNames) {
        Collection<String> membersUid = CollectionUtils.collect(getClusterService().getMembers(), o -> {
            return ((Member) o).getUuid();
        });
        return justStopTimerOnNodeInternally(timerNames, membersUid);
    }

    private boolean justStopTimerOnNodeInternally(String timerName, Collection<String> membersUid) {
        JustStopTimerAction timerAction = new JustStopTimerAction();
        timerAction.getTimerToDestroy().add(timerName);

        for (String uid : membersUid) {
            logger.debug("Trying to stop timer: " + timerName + " on node: " + uid);
            try {
                Map<String, Boolean> result = getClusterService().submitAndGet(timerAction, it -> it.getUuid().equals(uid));
                Boolean isStopped = result.get(timerName);
                if (isStopped != null && isStopped) {
                    logger.debug("Timer: " + timerName + " was successfully stopped on node: " + uid);
                    return true;
                } else {
                    logger.debug("Timer: " + timerName + " was not stopped on node: " + uid);
                }
            } catch (DefaultTimerException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    private Map<TimerNodeInfo, Boolean> justStopTimerOnNodeInternally(Collection<String> timerNames, Collection<String> membersUids) {
        JustStopTimerAction timerAction = new JustStopTimerAction();
        timerAction.getTimerToDestroy().addAll(timerNames);

        Map<TimerNodeInfo, Boolean> map = new HashMap<>();
        for (String memberUid : membersUids) {
            if (CollectionUtils.isEmpty(timerAction.getTimerToDestroy())) {
                break;
            }
            logger.debug("Trying to stop timers on node: " + memberUid);
            try {
                Map<String, Boolean> resultMap = getClusterService().submitAndGet(timerAction, it -> it.getUuid().equals(memberUid));
                for (Map.Entry<String, Boolean> entry : resultMap.entrySet()) {
                    if (entry != null && entry.getValue()) {
                        timerAction.getTimerToDestroy().remove(entry.getKey());
                        //Класс TimerNodeInfo введен чтобы избежать коллизии таймеров с одинаковым названием.
                        //Имя таймера уникально в пределах ноды где он запущен
                        TimerNodeInfo timerNodeInfo = new TimerNodeInfo(entry.getKey(), memberUid);
                        map.put(timerNodeInfo, entry.getValue());
                    }
                }
            } catch (DefaultTimerException e) {
                logger.error(e.getMessage(), e);
            }
        }
        for (String timerName : timerNames) {
            if (!isContains(map, timerName)) {
                TimerNodeInfo timerNodeInfo = new TimerNodeInfo(timerName, TimerNodeInfo.UNKNOWN_NODE_UID);
                map.put(timerNodeInfo, false);
            }
        }
        return map;
    }

    /**
     * Stop local timer using just only timerService resource
     */
    public boolean justStopLocalTimer(IClusteredTimer clusteredTimer) {
        return justStopLocalTimer(clusteredTimer.getLauncherResolver(), clusteredTimer.getTimerInfo());
    }

    public boolean justStopLocalTimer(ITimerLauncherResolver launcherResolver, TimerInfo timerInfo) {
        return launcherResolver.lookup().cancelTimer(timerInfo);
    }

    private boolean justStopLocalTimer(ITimerLauncher timerLauncher, TimerInfo timerInfo) {
        return timerLauncher.cancelTimer(timerInfo);
    }

    public Map<TimerDescriptor, Boolean> justStopLocalTimers(Collection<TimerDescriptor> timerDescriptors) {
        Map<TimerDescriptor, Boolean> result = new HashMap<>();

        Map<AbstractTimerLauncherResolver, ITimerLauncher> resolverNameLauncherMap = new HashMap<>();
        for (TimerDescriptor timerDescriptor : timerDescriptors) {
            AbstractTimerLauncherResolver launcherResolver = timerDescriptor.getClusteredTimer().getLauncherResolver();
            ITimerLauncher timerLauncher;
            if (resolverNameLauncherMap.containsKey(launcherResolver)) {
                timerLauncher = resolverNameLauncherMap.get(launcherResolver);
            } else {
                timerLauncher = launcherResolver.lookup();
                resolverNameLauncherMap.put(launcherResolver, timerLauncher);
            }

            boolean isStopped = false;
            try {
                isStopped = justStopLocalTimer(timerLauncher, timerDescriptor.getClusteredTimer().getTimerInfo());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                result.put(timerDescriptor, isStopped);
            }
        }
        return result;
    }

    public boolean justStopLocalTimerEverywhere(String timerName) {
        for (AbstractTimerLauncherResolver launcherResolver : getTimerContainer().getTimerLaunchers()) {
            logger.debug("Trying to stop timer on :" + launcherResolver.getName());
            ITimerLauncher timerLauncher = launcherResolver.lookup();
            boolean isCanceled = false;
            try {
                isCanceled = timerLauncher.cancelTimer(new TimerInfo(timerName, new DummyTimerAction()));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            if (isCanceled) {
                logger.debug("Timer: " + timerName + " had been canceled!");
                return true;
            }
        }
        logger.debug("Timer: " + timerName + " had not been canceled!");
        return false;
    }
}
