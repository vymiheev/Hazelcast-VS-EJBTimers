package ru.hz.ejb.cluster.timer;

/**
 * Created by mvj on 14.11.2016.
 */
public class ScheduledTimer extends CommonClusterTimer {
    private static final long serialVersionUID = -1084621947466349518L;
    private ScheduleExpressionWrapper scheduleExpression;

    public ScheduleExpressionWrapper getScheduleExpression() {
        return scheduleExpression;
    }

    public void setScheduleExpression(ScheduleExpressionWrapper scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
    }

    @Override
    public TimerType getTimerType() {
        return TimerType.SCHEDULED_TIMER;
    }

}
