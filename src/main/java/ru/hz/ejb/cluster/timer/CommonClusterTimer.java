package ru.hz.ejb.cluster.timer;

/**
 * Created by mvj on 25.10.2017.
 */
public abstract class CommonClusterTimer implements IClusteredTimer {
    protected TimerInfo timerInfo;
    protected AbstractTimerLauncherResolver launcherResolver;

    @Override
    public TimerInfo getTimerInfo() {
        return timerInfo;
    }

    public void setTimerInfo(TimerInfo timerInfo) {
        this.timerInfo = timerInfo;
    }

    @Override
    public AbstractTimerLauncherResolver getLauncherResolver() {
        return launcherResolver;
    }

    public void setLauncherResolver(AbstractTimerLauncherResolver launcherResolver) {
        this.launcherResolver = launcherResolver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommonClusterTimer that = (CommonClusterTimer) o;

        return !(timerInfo != null ? !timerInfo.equals(that.timerInfo) : that.timerInfo != null);

    }

    @Override
    public int hashCode() {
        return timerInfo != null ? timerInfo.hashCode() : 0;
    }
}
