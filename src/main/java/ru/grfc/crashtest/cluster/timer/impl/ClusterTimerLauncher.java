package ru.grfc.crashtest.cluster.timer.impl;

import com.hazelcast.core.Member;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.IClusterNode;
import ru.grfc.crashtest.cluster.INodeRelation;
import ru.grfc.crashtest.cluster.IStartupTimer;
import ru.grfc.crashtest.cluster.timer.*;
import ru.grfc.crashtest.cluster.timer.actions.impl.JustCreateTimerAction;
import ru.grfc.crashtest.cluster.timer.actions.impl.StartBulkTimerAction;

import java.util.*;

/**
 * Created by mvj on 26.01.2017.
 */
public class ClusterTimerLauncher extends AbstractTimerController implements IClusterTimerLauncher {
    private static final Logger logger = Logger.getLogger(ClusterTimerLauncher.class);

    ClusterTimerLauncher(ClusterTimerManager timerManager) {
        super(timerManager);
    }

    public boolean isAllowed(IClusteredTimer clusteredTimer) {
        IStartupTimer timerDefinition = getTimerDefinition(clusteredTimer);
        if (timerDefinition != null && timerDefinition.getIsActual()) { // следует ли его настрйоки вообще учитывать
            return timerDefinition.getIsEnabled() && isLocalTimerDefinition(timerDefinition); //включен ли в локальную ноду
        } else {
            //если таймер не задекларирован то по умолчанию он включен
            logger.debug("Timer not defined. Enabled by default.");
            return true;
        }
    }

    private boolean isLocalTimerDefinition(IStartupTimer timerDefinition) {
        INodeRelation nodeRelation = getClusterService().getNetworkConfiguration().getNodeRelation();
        if (nodeRelation == null || nodeRelation.getThisNode() == null) {
            return false;
        }
        IClusterNode clusterNode = nodeRelation.getThisNode();
        return clusterNode.getStartupTimers().contains(timerDefinition);
    }

    private IStartupTimer getTimerDefinition(IClusteredTimer clusteredTimer) {
        INodeRelation nodeRelation = getClusterService().getNetworkConfiguration().getNodeRelation();
        if (nodeRelation == null || nodeRelation.getThisNode() == null) {
            return null;
        }
        IClusterNode clusterNode = nodeRelation.getThisNode();
        List<IStartupTimer> startupTimers = clusterNode.getClusterWrapper().getStartupTimers();
        for (IStartupTimer startupTimer : startupTimers) {
            if (startupTimer.getTimerName().equals(getTimerName(clusteredTimer))) {
                return startupTimer;
            }
        }
        return null;
    }

    public boolean createTimer(IClusteredTimer clusteredTimer) throws DefaultTimerException {
        if (!isAllowed(clusteredTimer)) {
            logger.info(getTimerName(clusteredTimer) + " is not allowed on this node.");
            return false;
        }
        logger.info("Creating timer: " + getTimerName(clusteredTimer));
        return createTimerInternally(clusteredTimer);
    }

    private boolean createTimerInternally(IClusteredTimer clusteredTimer) throws DefaultTimerException {
        try {
            //Есть вероятность возникновения исключительной ситуации в случае когда операция создания(вызв этого метода)
            //и остановка указанного таймера выполняются одновременно. Это связано с тем что нет возможности поставить
            //пессемистическую блокировку на создание таймера, т.к. его на этот момент ещё нет в хранилище.
            //optimistic locking
            if (getTimerContainer().addTimer(clusteredTimer, TimerStatus.RUNNING, getClusterService().getLocalNodeUUID())) {
                //pessimistic locking
                justCreateLocalTimerInternallyLock(clusteredTimer);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DefaultTimerException(e);
        }
    }

    private void justCreateLocalTimerInternallyLock(IClusteredTimer clusteredTimer) throws DefaultTimerException {
        boolean isAcquired = false;
        try {
            //Используется пессимистическая блокировка т.к. создание таймера и errorHandling должны выполняться атомарно
            isAcquired = getTimerContainer().tryLock(getTimerName(clusteredTimer));
            if (isAcquired) {
                justCreateLocalTimerInternally(clusteredTimer);
            } else {
                throw new DefaultTimerException("Key: " + getTimerName(clusteredTimer) + " is locked!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (e instanceof DefaultTimerException) {
                throw (DefaultTimerException) e;
            }
            throw new DefaultTimerException(e);
        } finally {
            if (isAcquired) {
                getTimerContainer().unlock(getTimerName(clusteredTimer));
            }
        }
    }

    private void justCreateLocalTimerInternally(IClusteredTimer clusteredTimer) {
        try {
            justCreateLocalTimer(clusteredTimer);
        } catch (Exception e) {
            try {
                if (getTimerContainer().removeTimer(getTimerName(clusteredTimer))) {
                    logger.debug("Timer removed!");
                } else {
                    logger.error("Timer had not been removed!");
                }
            } catch (Exception ee) {
                logger.error(ee.getMessage(), ee);
            }
        }
    }

    public void startTimerByName(String timerName) throws DefaultTimerException {
        TimerDescriptor timerDescriptor = getTimerContainer().getTimerByName(timerName);
        if (timerDescriptor == null) {
            throw new DefaultTimerException("There is no timer with name: " + timerName);
        }
        startTimer(timerDescriptor);
    }

    public Map<TimerDescriptor, Boolean> startTimersByName(Collection<String> timerNames) throws DefaultTimerException {
        List<TimerDescriptor> timerDescriptors = new ArrayList<>();
        for (String timerName : timerNames) {
            TimerDescriptor timerDescriptor = getTimerContainer().getTimerByName(timerName);
            if (timerDescriptor == null) {
                logger.error("There is no timer with name: " + timerName);
            }
            timerDescriptors.add(timerDescriptor);
        }
        return startTimers(timerDescriptors);
    }

    public void startTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        if (timerDescriptor.getTimerStatus().equals(TimerStatus.RUNNING)) {
            throw new DefaultTimerException("Timer is in " + timerDescriptor.getTimerStatus().name() + " state!");
        }

        if (getClusterService().isLocal(timerDescriptor.getNodeUuid())) {
            startLocalTimerInternallyLock(timerDescriptor.getClusteredTimer().getLauncherResolver().lookup(), timerDescriptor);
        } else {
            startRemoteTimerInternally(timerDescriptor);
        }
    }

    public Map<TimerDescriptor, Boolean> startAllTimers() throws DefaultTimerException {
        Collection<TimerDescriptor> timerDescriptors = getTimerContainer().getTimers(TimerStatus.FAILED, TimerStatus.STOPPED, TimerStatus.IDLE);
        return startTimers(timerDescriptors);
    }

    public Map<TimerDescriptor, Boolean> startAllTimers(String nodeUUID) throws DefaultTimerException {
        Collection<TimerDescriptor> timerDescriptors = getTimerContainer().getTimers(nodeUUID);
        return startTimers(timerDescriptors);
    }

    public Map<TimerDescriptor, Boolean> startTimers(Collection<TimerDescriptor> timerDescriptors) throws DefaultTimerException {
        Map<Member, Collection<TimerDescriptor>> timerDescriptorMap = getStateDescriptorsMap(timerDescriptors);
        if (MapUtils.isEmpty(timerDescriptorMap)) {
            throw new DefaultTimerException("There are no timers to start!");
        }

        return startBulkTimerInternally(timerDescriptorMap);
    }

    private Map<TimerDescriptor, Boolean> startBulkTimerInternally(Map<Member, Collection<TimerDescriptor>> timerDescriptors) {
        Map<TimerDescriptor, Boolean> descriptorBooleanMap = new HashMap<>();
        for (Map.Entry<Member, Collection<TimerDescriptor>> entry : timerDescriptors.entrySet()) {
            try {
                Map<TimerDescriptor, Boolean> result;
                if (getClusterService().isLocal(entry.getKey())) {
                    result = startLocalTimersInternallyLock(entry.getValue());
                } else {
                    result = startBulkRemoteTimersInternally(entry.getValue(), entry.getKey());
                }
                descriptorBooleanMap.putAll(result);
            } catch (DefaultTimerException e) {
                logger.error("Some errors were detected while starting timers on cluster member: " + entry.getKey().getUuid());
                logger.error(e.getMessage(), e);
                for (TimerDescriptor timerDescriptor : entry.getValue()) {
                    descriptorBooleanMap.put(timerDescriptor, false);
                }
            }
        }
        return descriptorBooleanMap;
    }

    private Map<TimerDescriptor, Boolean> startBulkRemoteTimersInternally(Collection<TimerDescriptor> timerDescriptors, Member member) throws DefaultTimerException {
        logger.debug("Start timer remotely...");
        StartBulkTimerAction timerAction = new StartBulkTimerAction();
        timerAction.setTimers(timerDescriptors);
        return getClusterService().submitAndGet(timerAction, it -> it.getUuid().equals(member.getUuid()));
    }

    public void startRemoteTimerInternally(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        logger.debug("Start timers remotely...");
        StartBulkTimerAction timerAction = new StartBulkTimerAction();
        timerAction.getTimers().add(timerDescriptor);
        getClusterService().submitAndGet(timerAction, member -> member.getUuid().equals(timerDescriptor.getNodeUuid()));
    }

    public Map<TimerDescriptor, Boolean> startLocalTimers(Collection<TimerDescriptor> timerDescriptors) throws DefaultTimerException {
        Collection<TimerDescriptor> stoppedTimers = getTimersState(timerDescriptors, TimerStatus.STOPPED, TimerStatus.IDLE, TimerStatus.FAILED);
        if (CollectionUtils.isEmpty(stoppedTimers)) {
            throw new DefaultTimerException("There are no timers to start!");
        }
        String localUUID = getClusterService().getLocalNodeUUID();
        Iterator<TimerDescriptor> iterator = stoppedTimers.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().getNodeUuid().equals(localUUID)) {
                iterator.remove();
            }
        }
        if (CollectionUtils.isEmpty(stoppedTimers)) {
            throw new DefaultTimerException("No local timers exist!");
        }

        return startLocalTimersInternallyLock(stoppedTimers);
    }

    public void startLocalTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        if (timerDescriptor.getTimerStatus().equals(TimerStatus.RUNNING)) {
            throw new DefaultTimerException("Timer is in " + timerDescriptor.getTimerStatus().name() + " state!");
        }
        if (!getClusterService().isLocal(timerDescriptor.getNodeUuid())) {
            throw new DefaultTimerException("Timer " + timerDescriptor.getTimerStatus().name() + " is not local");
        }

        startLocalTimerInternallyLock(timerDescriptor.getClusteredTimer().getLauncherResolver().lookup(), timerDescriptor);
    }

    public Map<TimerDescriptor, Boolean> startLocalTimersInternallyLock(Collection<TimerDescriptor> timerDescriptors) {
        Map<AbstractTimerLauncherResolver, ITimerLauncher> resolverNameLauncherMap = new HashMap<>();
        Map<TimerDescriptor, Boolean> descriptorBooleanMap = new HashMap<>();

        for (TimerDescriptor timerDescriptor : timerDescriptors) {
            AbstractTimerLauncherResolver launcherResolver = timerDescriptor.getClusteredTimer().getLauncherResolver();
            ITimerLauncher timerLauncher;
            if (resolverNameLauncherMap.containsKey(launcherResolver)) {
                timerLauncher = resolverNameLauncherMap.get(launcherResolver);
            } else {
                timerLauncher = launcherResolver.lookup();
                resolverNameLauncherMap.put(launcherResolver, timerLauncher);
            }
            try {
                startLocalTimerInternallyLock(timerLauncher, timerDescriptor);
                descriptorBooleanMap.put(timerDescriptor, true);
            } catch (DefaultTimerException e) {
                logger.error("Failed to start timer :" + getTimerName(timerDescriptor.getClusteredTimer()));
                descriptorBooleanMap.put(timerDescriptor, false);
            }
        }

        return descriptorBooleanMap;
    }

    public void startLocalTimerInternallyLock(ITimerLauncher timerLauncher, TimerDescriptor timerDescriptor) throws DefaultTimerException {
        boolean isAcquired = false;
        try {
            //Используется пессимистическая блокировка т.к. старт таймера и errorHandling должны выполняться атомарно
            isAcquired = getTimerContainer().tryLock(getTimerName(timerDescriptor));
            if (isAcquired) {
                startLocalTimerInternally(timerLauncher, timerDescriptor);
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

    private void startLocalTimerInternally(ITimerLauncher timerLauncher, TimerDescriptor timerDescriptor) throws DefaultTimerException {
        logger.info("Start timer " + getTimerName(timerDescriptor) + " on local node.");
        boolean isStatusChanged = false;
        try {
            isStatusChanged = getTimerContainer().changeTimerStatus(timerDescriptor, TimerStatus.RUNNING);
            if (isStatusChanged) {
                justCreateLocalTimer(timerLauncher, timerDescriptor.getClusteredTimer());
            } else {
                throw new DefaultTimerException("Failed to change timer status.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (isStatusChanged) {
                try {
                    TimerDescriptor descriptor = new TimerDescriptor(timerDescriptor.getClusteredTimer(), timerDescriptor.getNodeUuid(), TimerStatus.RUNNING);
                    if (getTimerContainer().changeTimerStatus(descriptor, timerDescriptor.getTimerStatus())) {
                        logger.info("Timer change status to previous: " + timerDescriptor.getTimerStatus().name());
                    } else {
                        logger.error("Timer status had not been changed!");
                    }
                } catch (Exception ee) {
                    logger.error(ee.getMessage(), ee);
                }
            }
            if (e instanceof DefaultTimerException) {
                throw (DefaultTimerException) e;
            } else {
                throw new DefaultTimerException(e);
            }
        }
    }

    //Without Locks
    public Map<IClusteredTimer, Boolean> justCreateLocalTimers(Collection<IClusteredTimer> timerToCreate) {
        Map<IClusteredTimer, Boolean> descriptorBooleanMap = new HashMap<>();

        Map<AbstractTimerLauncherResolver, ITimerLauncher> resolverNameLauncherMap = new HashMap<>();
        for (IClusteredTimer clusteredTimer : timerToCreate) {
            AbstractTimerLauncherResolver launcherResolver = clusteredTimer.getLauncherResolver();
            ITimerLauncher timerLauncher;
            if (resolverNameLauncherMap.containsKey(launcherResolver)) {
                timerLauncher = resolverNameLauncherMap.get(launcherResolver);
            } else {
                timerLauncher = launcherResolver.lookup();
                resolverNameLauncherMap.put(launcherResolver, timerLauncher);
            }
            try {
                justCreateLocalTimer(timerLauncher, clusteredTimer);
            } catch (Exception e) {
                logger.error("Failed to start timer :" + getTimerName(clusteredTimer));
                descriptorBooleanMap.put(clusteredTimer, false);
            }
        }

        return descriptorBooleanMap;
    }

    public void justCreateTimer(IClusteredTimer clusteredTimer, String nodeUid) throws DefaultTimerException {
        if (getClusterService().isLocal(nodeUid)) {
            justCreateLocalTimer(clusteredTimer);
        } else {
            JustCreateTimerAction timerAction = new JustCreateTimerAction();
            timerAction.getTimerToCreate().add(clusteredTimer);
            getClusterService().executeOnMember(timerAction, nodeUid);
        }
    }

    public void justCreateLocalTimer(IClusteredTimer clusteredTimer) throws DefaultTimerException {
        try {
            justCreateLocalTimer(clusteredTimer.getLauncherResolver(), clusteredTimer);
            logger.info("Create timer: " + getTimerName(clusteredTimer));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DefaultTimerException(e);
        }
    }

    public void justCreateLocalTimer(ITimerLauncherResolver launcherResolver, IClusteredTimer clusteredTimer) {
        justCreateLocalTimer(launcherResolver.lookup(), clusteredTimer);
    }

    public void justCreateLocalTimer(ITimerLauncher timerLauncher, IClusteredTimer clusteredTimer) {
        timerLauncher.initTimer(clusteredTimer);
    }

    public void justCreateTimers(Map<String, Collection<IClusteredTimer>> timerNodeUids) throws DefaultTimerException {
        for (Map.Entry<String, Collection<IClusteredTimer>> entry : timerNodeUids.entrySet()) {
            if (getClusterService().isLocal(entry.getKey())) {
                justCreateLocalTimers(entry.getValue());
            } else {
                JustCreateTimerAction timerAction = new JustCreateTimerAction();
                timerAction.getTimerToCreate().addAll(entry.getValue());
                getClusterService().executeOnMember(timerAction, entry.getKey());
            }
        }
    }
}
