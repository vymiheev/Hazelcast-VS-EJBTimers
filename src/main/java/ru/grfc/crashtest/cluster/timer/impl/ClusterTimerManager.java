package ru.grfc.crashtest.cluster.timer.impl;

import com.hazelcast.core.*;
import com.hazelcast.map.EntryProcessor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ru.grfc.crashtest.cluster.ClusterService;
import ru.grfc.crashtest.cluster.ClusterTimershipListener;
import ru.grfc.crashtest.cluster.IClusterService;
import ru.grfc.crashtest.cluster.timer.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mvj on 26.10.2016.
 */
public class ClusterTimerManager implements IClusterTimerManager {
    private static final Logger logger = Logger.getLogger(ClusterTimerManager.class);
    private ClusterTimerAnalyzer timerAnalyzer;
    private ClusterTimerMigration timerMigration;
    private ClusterTimerLauncher timerLauncher;
    private ClusterTimerDestroyer timerDestroyer;
    private ClusterTimerContainer timerContainer;
    private IClusterService clusterService;
    private AtomicBoolean isConstructed = new AtomicBoolean(false);

    public ClusterTimerManager(ClusterService clusterService) {
        this.clusterService = clusterService;
        this.timerAnalyzer = new ClusterTimerAnalyzer(this);
        this.timerMigration = new ClusterTimerMigration(this);
        this.timerLauncher = new ClusterTimerLauncher(this);
        this.timerDestroyer = new ClusterTimerDestroyer(this);
    }

    public void init() {
        if (isConstructed.get()) {
            logger.debug("Timer manager had already been constructed!");
        } else {
            logger.debug("Timer manager initializing...");
            HazelcastInstance hazelcastInstance = clusterService.getHazelcastInstance();
            IMap<String, TimerDescriptor> timerMap = hazelcastInstance.getMap("timerMemberMap");
            ISet<AbstractTimerLauncherResolver> launchersSet = hazelcastInstance.getSet("timerLaunchersSet");
            timerMap.addLocalEntryListener(new TimerMapListener());
            this.timerContainer = new ClusterTimerContainer(timerMap, launchersSet);

            hazelcastInstance.getCluster().addMembershipListener(new ClusterTimershipListener() {
                @Override
                public void memberRemoved(MembershipEvent membershipEvent) {
                    super.memberRemoved(membershipEvent);
                    Member member = membershipEvent.getMember();
                    try {
                        timerMigration.migrateTimersEmergency(member);
                    } catch (DefaultTimerException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
            logger.debug("Finished!");
            isConstructed.set(true);
        }
    }

    public IClusterService getClusterService() {
        return clusterService;
    }

    public ClusterTimerAnalyzer getTimerAnalyzer() {
        return timerAnalyzer;
    }

    public ClusterTimerMigration getTimerMigration() {
        return timerMigration;
    }

    public ClusterTimerLauncher getTimerLauncher() {
        return timerLauncher;
    }

    public ClusterTimerDestroyer getTimerDestroyer() {
        return timerDestroyer;
    }

    public ClusterTimerContainer getTimerContainer() {
        return timerContainer;
    }

    //Timer Container API-----------------------------------------------------------------------------------------------
    public Collection<TimerDescriptor> getTimers(String memberUuid) {
        if (StringUtils.isEmpty(memberUuid)) {
            throw new IllegalArgumentException("Empty 'memberUuid' param!");
        }
        return timerContainer.getTimers(memberUuid);
    }

    public Collection<TimerDescriptor> getTimers(TimerStatus... timerStatuses) {
        return timerContainer.getTimers(timerStatuses);
    }

    public Collection<TimerDescriptor> getAllTimers() {
        return timerContainer.getAllTimers();
    }

    public void edit(EntryProcessor entryProcessor, com.hazelcast.query.Predicate predicate) {
        if (entryProcessor == null || predicate == null) {
            throw new IllegalArgumentException("Empty 'containerClosure' param!");
        }
        timerContainer.edit(entryProcessor, predicate);
    }

    public TimerDescriptor getTimerByName(String timerName) {
        if (StringUtils.isEmpty(timerName)) {
            throw new IllegalArgumentException("Empty 'timerName' param!");
        }
        return timerContainer.getTimerByName(timerName);
    }

    public Collection<TimerDescriptor> getTimersByName(Collection<String> timerNames) {
        if (CollectionUtils.isEmpty(timerNames)) {
            throw new IllegalArgumentException("Empty 'timerNames' param!");
        }
        return timerContainer.getTimersByName(timerNames);
    }

    public boolean justAddLocalTimer(IClusteredTimer clusteredTimer, TimerStatus timerStatus) {
        return this.justAddTimer(clusteredTimer, timerStatus, clusterService.getLocalNodeUUID());
    }

    public boolean justAddTimer(IClusteredTimer clusteredTimer, TimerStatus timerStatus, String nodeUid) {
        if (clusteredTimer == null) {
            throw new IllegalArgumentException("Empty param 'clusteredTimer'");
        }
        if (timerStatus == null) {
            throw new IllegalArgumentException("Empty param 'timerStatus'");
        }
        if (StringUtils.isEmpty(nodeUid)) {
            throw new IllegalArgumentException("Empty param 'nodeUid'");
        }
        return timerContainer.addTimer(clusteredTimer, timerStatus, nodeUid);
    }

    public boolean justRemoveTimer(String timerName) {
        if (StringUtils.isEmpty(timerName)) {
            throw new IllegalArgumentException("Empty param 'timerName'");
        }
        return timerContainer.removeTimer(timerName);
    }

    public boolean justReplaceTimer(IClusteredTimer clusteredTimer, String fromNodeUuid, String toNodeUuid, TimerStatus timerStatus) {
        if (clusteredTimer == null) {
            throw new IllegalArgumentException("Empty param 'clusteredTimer'");
        }
        if (timerStatus == null) {
            throw new IllegalArgumentException("Empty param 'timerStatus'");
        }
        if (StringUtils.isEmpty(fromNodeUuid)) {
            throw new IllegalArgumentException("Empty param 'fromNodeUuid'");
        }
        if (StringUtils.isEmpty(toNodeUuid)) {
            throw new IllegalArgumentException("Empty param 'toNodeUuid'");
        }
        return timerContainer.replaceTimer(clusteredTimer, fromNodeUuid, toNodeUuid, timerStatus);
    }

    public boolean isTimersExist(String memberUid) {
        if (StringUtils.isEmpty(memberUid)) {
            throw new IllegalArgumentException("Empty param 'memberUid'");
        }
        return timerContainer.isTimersExist(memberUid);
    }

    public boolean justChangeTimerStatus(TimerDescriptor timerDescriptor, TimerStatus status) {
        if (timerDescriptor == null) {
            throw new IllegalArgumentException("Empty param 'timerDescriptor'");
        }
        if (status == null) {
            throw new IllegalArgumentException("Empty param 'status'");
        }
        return timerContainer.changeTimerStatus(timerDescriptor, status);
    }

    //Timer Analyzer API------------------------------------------------------------------------------------------------
    public Map<String, Collection<TimerState>> getInvalidTimers() throws DefaultTimerException {
        return timerAnalyzer.getInvalidTimers();
    }

    public boolean hasInvalidTimers() throws DefaultTimerException {
        return timerAnalyzer.hasInvalidTimers();
    }

    public Map<String, Collection<Serializable>> getCommonTimers() throws DefaultTimerException {
        return timerAnalyzer.getCommonTimers();
    }

    public boolean hasCommonTimers() throws DefaultTimerException {
        return timerAnalyzer.hasCommonTimers();
    }

    //Timer Migration API-----------------------------------------------------------------------------------------------
    public void migrateAllTimers(String fromNodeUUID) throws DefaultTimerException {
        if (StringUtils.isEmpty(fromNodeUUID)) {
            throw new IllegalArgumentException("Empty 'fromNodeUUID' param!");
        }
        timerMigration.migrateAllTimers(fromNodeUUID);
    }

    public void migrateAllTimers(String fromNodeUUID, String toNodeUUID) throws DefaultTimerException {
        if (StringUtils.isEmpty(fromNodeUUID)) {
            throw new IllegalArgumentException("Empty 'fromNodeUUID' param!");
        }
        if (StringUtils.isEmpty(toNodeUUID)) {
            throw new IllegalArgumentException("Empty 'toNodeUUID' param!");
        }
        timerMigration.migrateAllTimers(fromNodeUUID, toNodeUUID);
    }

    public void migrateTimer(String timerName) throws DefaultTimerException {
        if (StringUtils.isEmpty(timerName)) {
            throw new IllegalArgumentException("Empty 'timerName' param!");
        }
        timerMigration.migrateTimer(timerName);
    }


    public void migrateTimer(String timerName, String toNodeUUID) throws DefaultTimerException {
        if (StringUtils.isEmpty(timerName)) {
            throw new IllegalArgumentException("Empty 'timerName' param!");
        }
        if (StringUtils.isEmpty(toNodeUUID)) {
            throw new IllegalArgumentException("Empty 'toNodeUUID' param!");
        }
        timerMigration.migrateTimer(timerName);
    }

    public void migrateTimer(TimerDescriptor timerDescriptor, String toNodeUUID) throws DefaultTimerException {
        if (timerDescriptor == null) {
            throw new IllegalArgumentException("Empty 'timerDescriptor' param!");
        }
        if (StringUtils.isEmpty(toNodeUUID)) {
            throw new IllegalArgumentException("Empty 'toNodeUUID' param!");
        }
        timerMigration.migrateTimer(timerDescriptor, toNodeUUID);
    }

    //Timer Launcher API------------------------------------------------------------------------------------------------

    public boolean createTimer(ScheduledTimer clusteredTimer) throws DefaultTimerException {
        if (clusteredTimer == null) {
            throw new IllegalArgumentException("Empty 'clusteredTimer' param!");
        }
        return timerLauncher.createTimer(clusteredTimer);
    }

    public boolean createTimer(DatedTimer clusteredTimer) throws DefaultTimerException {
        if (clusteredTimer == null) {
            throw new IllegalArgumentException("Empty 'clusteredTimer' param!");
        }
        return timerLauncher.createTimer(clusteredTimer);
    }

    public void startAllTimers() throws DefaultTimerException {
        timerLauncher.startAllTimers();
    }

    public void startAllTimers(String nodeUUID) throws DefaultTimerException {
        if (nodeUUID == null) {
            throw new IllegalArgumentException("Empty 'nodeUUID' param!");
        }
        timerLauncher.startAllTimers(nodeUUID);
    }

    public void startTimers(Collection<TimerDescriptor> timerDescriptors) throws DefaultTimerException {
        if (CollectionUtils.isEmpty(timerDescriptors)) {
            throw new IllegalArgumentException("Empty 'timerDescriptors' param!");
        }
        timerLauncher.startTimers(timerDescriptors);
    }

    public void startTimerByName(String timerName) throws DefaultTimerException {
        if (StringUtils.isEmpty(timerName)) {
            throw new IllegalArgumentException("Empty 'timerName' param!");
        }
        timerLauncher.startTimerByName(timerName);
    }

    public void startTimersByName(Collection<String> timerNames) throws DefaultTimerException {
        if (CollectionUtils.isEmpty(timerNames)) {
            throw new IllegalArgumentException("Empty 'timerNames' param!");
        }
        timerLauncher.startTimersByName(timerNames);
    }

    public void startTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        if (timerDescriptor == null) {
            throw new IllegalArgumentException("Empty 'timerDescriptor' param!");
        }
        timerLauncher.startTimer(timerDescriptor);
    }


    public void justCreateTimer(IClusteredTimer clusteredTimer, String nodeUid) throws DefaultTimerException {
        if (clusteredTimer == null) {
            throw new IllegalArgumentException("Empty 'clusteredTimer' param!");
        }
        if (StringUtils.isEmpty(nodeUid)) {
            throw new IllegalArgumentException("Empty 'nodeUid' param!");
        }
        timerLauncher.justCreateTimer(clusteredTimer, nodeUid);
    }

    public void justCreateTimers(Map<String, Collection<IClusteredTimer>> timerNodeUids) throws DefaultTimerException {
        if (MapUtils.isEmpty(timerNodeUids)) {
            throw new IllegalArgumentException("Empty 'timerNodeUids' param!");
        }
        timerLauncher.justCreateTimers(timerNodeUids);
    }


    //Timer Destroyer API

    public void stopTimerByName(String timerName) throws DefaultTimerException {
        if (StringUtils.isEmpty(timerName)) {
            throw new IllegalArgumentException("Empty 'timerName' param!");
        }
        timerDestroyer.stopTimerByName(timerName);
    }

    public void stopTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        if (timerDescriptor == null) {
            throw new IllegalArgumentException("Empty 'timerDescriptor' param!");
        }
        timerDestroyer.stopTimer(timerDescriptor);
    }

    public void stopAllTimers() throws DefaultTimerException {
        timerDestroyer.stopAllTimers();
    }

    public void stopAllTimers(String nodeUUID) throws DefaultTimerException {
        if (StringUtils.isEmpty(nodeUUID)) {
            throw new IllegalArgumentException("Empty 'nodeUUID' param!");
        }
        timerDestroyer.stopAllTimers(nodeUUID);
    }

    public Map<TimerDescriptor, Boolean> stopTimers(Collection<TimerDescriptor> timerDescriptors) throws DefaultTimerException {
        if (CollectionUtils.isEmpty(timerDescriptors)) {
            throw new IllegalArgumentException("Empty 'timerDescriptors' param!");
        }
        return timerDestroyer.stopTimers(timerDescriptors);
    }

    public Map<TimerDescriptor, Boolean> stopTimersByName(Collection<String> timerNames) throws DefaultTimerException {
        if (CollectionUtils.isEmpty(timerNames)) {
            throw new IllegalArgumentException("Empty 'timerNames' param!");
        }
        return timerDestroyer.stopTimersByName(timerNames);
    }

    public boolean justStopTimer(String timerName) throws DefaultTimerException {
        if (StringUtils.isEmpty(timerName)) {
            throw new IllegalArgumentException("Empty 'timerName' param!");
        }
        return timerDestroyer.justStopTimer(timerName);
    }

    public boolean justStopTimer(TimerDescriptor timerDescriptor) throws DefaultTimerException {
        if (timerDescriptor == null) {
            throw new IllegalArgumentException("Empty 'timerDescriptor' param!");
        }
        return timerDestroyer.justStopTimer(timerDescriptor);
    }

    public Map<TimerDescriptor, Boolean> justStopTimers(Collection<String> timerNames) {
        if (CollectionUtils.isEmpty(timerNames)) {
            throw new IllegalArgumentException("Empty 'timerNames' param!");
        }
        return timerDestroyer.justStopTimers(timerNames);
    }

    public Map<TimerDescriptor, Boolean> justStopTimer(Collection<TimerDescriptor> timerDescriptors) {
        if (CollectionUtils.isEmpty(timerDescriptors)) {
            throw new IllegalArgumentException("Empty 'timerDescriptors' param!");
        }
        return timerDestroyer.justStopTimer(timerDescriptors);
    }


    public boolean justStopTimerOnNode(String timerName, String nodeUid) {
        if (StringUtils.isEmpty(timerName)) {
            throw new IllegalArgumentException("Empty 'timerName' param!");
        }
        if (StringUtils.isEmpty(nodeUid)) {
            throw new IllegalArgumentException("Empty 'nodeUid' param!");
        }
        return timerDestroyer.justStopTimerOnNode(timerName, nodeUid);
    }

    public Map<TimerNodeInfo, Boolean> justStopTimerOnNode(Map<String, Collection<String>> timerNodes) {
        if (MapUtils.isEmpty(timerNodes)) {
            throw new IllegalArgumentException("Empty 'timerNodes' param!");
        }
        return timerDestroyer.justStopTimerOnNode(timerNodes);
    }

    public boolean justStopTimerEverywhere(String timerName) {
        if (StringUtils.isEmpty(timerName)) {
            throw new IllegalArgumentException("Empty 'timerName' param!");
        }
        return timerDestroyer.justStopTimerEverywhere(timerName);
    }

    public Map<TimerNodeInfo, Boolean> justStopTimersByNameEverywhere(Collection<String> timerNames) {
        if (CollectionUtils.isEmpty(timerNames)) {
            throw new IllegalArgumentException("Empty 'timerNames' param!");
        }
        return timerDestroyer.justStopTimersByNameEverywhere(timerNames);
    }

    //---------

    public void close() {
        logger.debug("Timer manager closed.");
    }

    public <T> T unwrap(Class<T> controllerClazz) {
        if (controllerClazz.isInterface()) {
            throw new IllegalArgumentException("Controller class can not be an interface!");
        }
        if (controllerClazz.equals(ClusterTimerMigration.class)) {
            return (T) timerMigration;
        } else if (controllerClazz.equals(ClusterTimerLauncher.class)) {
            return (T) timerLauncher;
        } else if (controllerClazz.equals(ClusterTimerDestroyer.class)) {
            return (T) timerDestroyer;
        } else if (controllerClazz.equals(ClusterTimerAnalyzer.class)) {
            return (T) timerAnalyzer;
        } else if (controllerClazz.equals(ClusterTimerContainer.class)) {
            return (T) timerContainer;
        } else {
            logger.error("Could not find controller class by param: " + controllerClazz);
            return null;
        }
    }

}
