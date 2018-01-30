package ru.hz.ejb.cluster.timer;

import java.io.Serializable;

/**
 * Created by mvj on 20.02.2017.
 */
public class TimerNodeInfo implements Serializable {
    public static final String UNKNOWN_NODE_UID = "UNKNOWN_NODE_UID";
    private static final long serialVersionUID = 3323123252726351748L;
    private String timerName;
    private String nodeUid;

    public TimerNodeInfo() {
    }

    public TimerNodeInfo(String timerName, String nodeUid) {
        this.timerName = timerName;
        this.nodeUid = nodeUid;
    }

    public String getNodeUid() {
        return nodeUid;
    }

    public void setNodeUid(String nodeUid) {
        this.nodeUid = nodeUid;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimerNodeInfo that = (TimerNodeInfo) o;

        if (timerName != null ? !timerName.equals(that.timerName) : that.timerName != null) return false;
        return !(nodeUid != null ? !nodeUid.equals(that.nodeUid) : that.nodeUid != null);

    }

    @Override
    public int hashCode() {
        int result = timerName != null ? timerName.hashCode() : 0;
        result = 31 * result + (nodeUid != null ? nodeUid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TimerNodeInfo{" +
                "timerName='" + timerName + '\'' +
                ", nodeUid='" + nodeUid + '\'' +
                '}';
    }
}
