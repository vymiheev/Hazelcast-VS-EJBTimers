package ru.hz.ejb.cluster.timer;

import java.io.Serializable;

/**
 * Created by mvj on 18.11.2016.
 */
public class TimerDescriptor implements Serializable {
    private static final long serialVersionUID = 2856406546011314339L;
    private IClusteredTimer clusteredTimer;
    private String nodeUuid;
    private TimerStatus timerStatus = TimerStatus.IDLE;

    public TimerDescriptor() {
    }

    public TimerDescriptor(IClusteredTimer clusteredTimer, String nodeUuid) {
        this.clusteredTimer = clusteredTimer;
        this.nodeUuid = nodeUuid;
    }

    public TimerDescriptor(IClusteredTimer clusteredTimer, String nodeUuid, TimerStatus timerStatus) {
        this(clusteredTimer, nodeUuid);
        this.timerStatus = timerStatus;
    }

    public IClusteredTimer getClusteredTimer() {
        return clusteredTimer;
    }

    public void setClusteredTimer(IClusteredTimer clusteredTimer) {
        this.clusteredTimer = clusteredTimer;
    }

    public String getNodeUuid() {
        return nodeUuid;
    }

    public void setNodeUuid(String nodeUuid) {
        this.nodeUuid = nodeUuid;
    }

    public TimerStatus getTimerStatus() {
        return timerStatus;
    }

    public void setTimerStatus(TimerStatus timerStatus) {
        this.timerStatus = timerStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimerDescriptor that = (TimerDescriptor) o;

        if (clusteredTimer != null ? !clusteredTimer.equals(that.clusteredTimer) : that.clusteredTimer != null)
            return false;
        if (nodeUuid != null ? !nodeUuid.equals(that.nodeUuid) : that.nodeUuid != null) return false;
        return timerStatus == that.timerStatus;

    }

    @Override
    public int hashCode() {
        int result = clusteredTimer != null ? clusteredTimer.hashCode() : 0;
        result = 31 * result + (nodeUuid != null ? nodeUuid.hashCode() : 0);
        result = 31 * result + (timerStatus != null ? timerStatus.hashCode() : 0);
        return result;
    }
}
