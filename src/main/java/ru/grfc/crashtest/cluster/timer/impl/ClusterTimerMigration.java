package ru.grfc.crashtest.cluster.timer.impl;

import com.hazelcast.core.Member;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.timer.*;

import java.util.*;

/**
 * Created by mvj on 26.01.2017.
 */
public class ClusterTimerMigration extends AbstractTimerController implements IClusterTimerMigration {
    private static final Logger logger = Logger.getLogger(ClusterTimerMigration.class);

    ClusterTimerMigration(ClusterTimerManager timerManager) {
        super(timerManager);
    }

    public void migrateTimersEmergency(Member fromMember) throws DefaultTimerException {
        logger.info("Start migrating timers from node uid: " + fromMember.getUuid() + " with address: " + fromMember.getAddress().getHost() +
                " to node uid: " + getClusterService().getLocalNodeUUID());
        Collection<TimerDescriptor> timerDescriptors = getTimerContainer().getTimers(fromMember);
        if (CollectionUtils.isEmpty(timerDescriptors)) {
            logger.info("Cluster Member uid: " + fromMember.getUuid() + " with address: " + fromMember.getAddress().getHost() + " has no timers!");
            return;
        }
        migrateTimersEmergencyLock(timerDescriptors);
    }

    private void migrateTimersEmergencyLock(Collection<TimerDescriptor> timerDescriptors) throws DefaultTimerException {
        for (TimerDescriptor timerDescriptor : timerDescriptors) {
            if (migrateTimerEmergencyLock(timerDescriptor)) {
                logger.info("ScheduledTimer " + getTimerName(timerDescriptor) + " successfully migrated!");
            } else {
                logger.info("ScheduledTimer " + getTimerName(timerDescriptor) + " had not been created!");
            }
        }
    }

    private boolean migrateTimerEmergencyLock(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        boolean isAcquired = false;
        try {
            //Используется пессимистическая блокировка т.к. старт таймера и errorHandling должны выполняться атомарно
            isAcquired = getTimerContainer().tryLock(getTimerName(timerDescriptor));
            if (isAcquired) {
                return migrateTimerEmergency(timerDescriptor);
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

    /**
     * Initiated when node of the cluster is shutting down
     * Timer on failed node don't stop and start on first chosen node
     *
     * @param timerDescriptor timerDescriptor
     * @return true or false epta
     */
    private boolean migrateTimerEmergency(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        boolean isReplaced = false;
        try {
            Map<AbstractTimerLauncherResolver, ITimerLauncher> resolverNameLauncherMap = new HashMap<>();
            if (isReplaced = (getTimerContainer().migrateTimer(timerDescriptor, getClusterService().getLocalNodeUUID()))) {
                if (timerDescriptor.getTimerStatus().equals(TimerStatus.RUNNING)) {
                    AbstractTimerLauncherResolver launcherResolver = timerDescriptor.getClusteredTimer().getLauncherResolver();
                    ITimerLauncher timerLauncher;
                    if (resolverNameLauncherMap.containsKey(launcherResolver)) {
                        timerLauncher = resolverNameLauncherMap.get(launcherResolver);
                    } else {
                        timerLauncher = launcherResolver.lookup();
                        resolverNameLauncherMap.put(launcherResolver, timerLauncher);
                    }
                    timerManager.getTimerLauncher().justCreateLocalTimer(timerLauncher, timerDescriptor.getClusteredTimer());
                } else {
                    logger.info("Timer " + getTimerName(timerDescriptor) +
                            " ignored, because it has status: " + timerDescriptor.getTimerStatus().name());
                }
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //rollback replaced timer
            if (isReplaced) {
                try {
                    TimerDescriptor descriptor = new TimerDescriptor(timerDescriptor.getClusteredTimer(),
                            getClusterService().getLocalNodeUUID(), timerDescriptor.getTimerStatus());
                    if (getTimerContainer().changeTimerStatus(descriptor, TimerStatus.FAILED)) {
                        logger.debug("Timer had been replaced!");
                    } else {
                        logger.error("Timer had not been replaced!");
                    }
                } catch (Exception ee) {
                    logger.error(ee.getMessage(), ee);
                }
            }
            throw new DefaultTimerException(e);
        }
        return false;
    }

    public void migrateAllTimers(String fromNodeUUID) throws DefaultTimerException {
        String toNodeUuid = findSuitableNode(fromNodeUUID);
        if (toNodeUuid == null) {
            throw new DefaultTimerException("There is no suitable node for migration!");
        }

        migrateAllTimers(fromNodeUUID, toNodeUuid);
    }

    public void migrateAllTimers(String fromNodeUUID, String toNodeUUID) throws DefaultTimerException {
        if (fromNodeUUID.equals(toNodeUUID)) {
            throw new DefaultTimerException("Node uis are equal!");
        }

        Collection<TimerDescriptor> timerDescriptors = getTimerContainer().getTimers(fromNodeUUID);
        if (CollectionUtils.isEmpty(timerDescriptors)) {
            logger.info("There is not timers to migrate!");
            return;
        }
        logger.info("Start migrating timers from node uid: " + fromNodeUUID + " to: " + toNodeUUID);
        migrateAllTimersInternally(timerDescriptors, toNodeUUID);
    }

    private void migrateAllTimersInternally(Collection<TimerDescriptor> timerDescriptors, String toNodeUUID) throws DefaultTimerException {
        List<TimerDescriptor> failedDescriptors = new ArrayList<>();
        for (TimerDescriptor timerDescriptor : timerDescriptors) {
            try {
                migrateTimerInternallyLock(timerDescriptor, toNodeUUID);
            } catch (DefaultTimerException e) {
                failedDescriptors.add(timerDescriptor);
            }
        }

        if (!failedDescriptors.isEmpty()) {
            logger.error("Invalid timers: " + failedDescriptors.size());
            for (TimerDescriptor timerDescriptor : failedDescriptors) {
                logger.error("Failed to migrate timer :" + getTimerName(timerDescriptor));
            }
            throw new DefaultTimerException("Have " + failedDescriptors.size() + " failed timer/s");
        }
    }

    public void migrateTimer(String timerName) throws DefaultTimerException {
        TimerDescriptor timerDescriptor = getTimerContainer().getTimerByName(timerName);
        if (timerDescriptor == null) {
            throw new DefaultTimerException("There is no timer with name: " + timerName);
        }
        String toNodeUuid = findSuitableNode(timerDescriptor.getNodeUuid());
        if (toNodeUuid == null) {
            throw new DefaultTimerException("There is no suitable node for migration!");
        }
        migrateTimer(timerDescriptor, toNodeUuid);
    }

    public void migrateTimer(String timerName, String toNodeUUID) throws DefaultTimerException {
        TimerDescriptor timerDescriptor = getTimerContainer().getTimerByName(timerName);
        if (timerDescriptor == null) {
            throw new DefaultTimerException("There is no timer with name: " + timerName);
        }
        migrateTimer(timerDescriptor, toNodeUUID);
    }

    public void migrateTimer(TimerDescriptor timerDescriptor, String toNodeUUID) throws DefaultTimerException {
        if (toNodeUUID.equals(timerDescriptor.getNodeUuid())) {
            throw new DefaultTimerException("Node uis are equal!");
        }

        logger.info("Start migrating timer from node uid: " + timerDescriptor.getNodeUuid() + " to: " + toNodeUUID);
        migrateTimerInternallyLock(timerDescriptor, toNodeUUID);
    }

    private void migrateTimerInternallyLock(TimerDescriptor timerDescriptor, String toNodeUUID) throws DefaultTimerException {
        boolean isAcquired = false;
        try {
            //Используется пессимистическая блокировка т.к. старт таймера и errorHandling должны выполняться атомарно
            isAcquired = getTimerContainer().tryLock(getTimerName(timerDescriptor));
            if (isAcquired) {
                migrateTimerInternally(timerDescriptor, toNodeUUID);
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

    private void migrateTimerInternally(TimerDescriptor timerDescriptor, String toNodeUUID) throws DefaultTimerException {
        boolean isReplaced = false;
        boolean isStopped = false;
        try {
            isStopped = timerManager.getTimerDestroyer().justStopTimer(timerDescriptor);
            if (!isStopped) {
                throw new DefaultTimerException("Could not stop timer: " + getTimerName(timerDescriptor));
            }
            if (isReplaced = (getTimerContainer().migrateTimer(timerDescriptor, toNodeUUID))) {
                if (timerDescriptor.getTimerStatus().equals(TimerStatus.RUNNING)) {
                    timerManager.getTimerLauncher().justCreateTimer(timerDescriptor.getClusteredTimer(), toNodeUUID);
                } else {
                    logger.info("Timer " + getTimerName(timerDescriptor) +
                            " ignored, because it has status: " + timerDescriptor.getTimerStatus().name());
                }
            } else {
                throw new DefaultTimerException("Failed to migrate timer: " + getTimerName(timerDescriptor));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //rollback replaced timer
            if (isReplaced) {
                try {
                    if (getTimerContainer().changeTimerStatus(timerDescriptor, TimerStatus.STOPPED)) {
                        logger.debug("Timer had been replaced!");
                    } else {
                        logger.error("Timer had not been replaced!");
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

}
