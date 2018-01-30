package ru.grfc.crashtest.cluster.timer;

import java.io.Serializable;

/**
 * Created by mvj on 27.10.2016.
 */
public class TimerInfo implements Serializable {
    private static final long serialVersionUID = 639909921082142532L;
    private String timerName;
    private ITimerAction action;

    public TimerInfo() {
    }

    public TimerInfo(String timerName, ITimerAction action) {
        this.timerName = timerName;
        this.action = action;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public ITimerAction getAction() {
        return action;
    }

    public void setAction(ITimerAction action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimerInfo info = (TimerInfo) o;

        return !(timerName != null ? !timerName.equals(info.timerName) : info.timerName != null);

    }

    @Override
    public int hashCode() {
        return timerName != null ? timerName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TimerInfo{" +
                "timerName='" + timerName + '\'' +
                '}';
    }
}
