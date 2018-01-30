package ru.hz.ejb.cluster.timer;

import java.io.Serializable;

/**
 * Created by mvj on 24.01.2017.
 */
public class TimerState implements Serializable {
    private static final long serialVersionUID = 4068289027460991666L;
    private TimerInfo timerInfo;
    private TimerStateType timerState;

    public TimerState() {
    }

    public TimerState(TimerInfo timerInfo, TimerStateType timerState) {
        this.timerInfo = timerInfo;
        this.timerState = timerState;
    }

    public TimerInfo getTimerInfo() {
        return timerInfo;
    }

    public void setTimerInfo(TimerInfo timerInfo) {
        this.timerInfo = timerInfo;
    }

    public TimerStateType getTimerState() {
        return timerState;
    }

    public void setTimerState(TimerStateType timerState) {
        this.timerState = timerState;
    }

    @Override
    public String toString() {
        return "TimerStateUid{" +
                "timerInfo=" + timerInfo +
                ", timerState=" + timerState +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimerState that = (TimerState) o;

        if (timerInfo != null ? !timerInfo.equals(that.timerInfo) : that.timerInfo != null) return false;
        return timerState == that.timerState;

    }

    @Override
    public int hashCode() {
        int result = timerInfo != null ? timerInfo.hashCode() : 0;
        result = 31 * result + (timerState != null ? timerState.hashCode() : 0);
        return result;
    }
}
