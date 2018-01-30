package ru.hz.ejb.cluster.timer;

import com.hazelcast.core.Member;

/**
 * Created by mvj on 24.01.2017.
 */
public interface IClusterTimerMigration {
    void migrateTimersEmergency(Member fromMember) throws DefaultTimerException;

    void migrateAllTimers(String fromNodeUUID) throws DefaultTimerException;

    void migrateAllTimers(String fromNodeUUID, String toNodeUUID) throws DefaultTimerException;

    void migrateTimer(String timerName) throws DefaultTimerException;

    void migrateTimer(String timerName, String toNodeUUID) throws DefaultTimerException;

    void migrateTimer(TimerDescriptor timerDescriptor, String toNodeUUID) throws DefaultTimerException;
}
