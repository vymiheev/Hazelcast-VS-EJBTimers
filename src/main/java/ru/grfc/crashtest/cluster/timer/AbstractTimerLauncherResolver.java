package ru.grfc.crashtest.cluster.timer;

/**
 * Created by mvj on 26.10.2017.
 */
public abstract class AbstractTimerLauncherResolver implements ITimerLauncherResolver {
    private static final long serialVersionUID = -3528978210289833921L;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractTimerLauncherResolver that = (AbstractTimerLauncherResolver) o;

        return !(getName() != null ? !getName().equals(that.getName()) : that.getName() != null);

    }

    @Override
    public final int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }
}
