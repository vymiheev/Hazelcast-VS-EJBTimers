package ru.grfc.crashtest.cluster.timer;

import java.util.Date;

/**
 * Created by mvj on 14.11.2016.
 */
public class DatedTimer extends CommonClusterTimer {
    private static final long serialVersionUID = 8623391700813568201L;
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public TimerType getTimerType() {
        return TimerType.DATED_TIMER;
    }

}
