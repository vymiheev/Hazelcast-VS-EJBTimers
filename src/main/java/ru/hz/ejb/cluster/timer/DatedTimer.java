package ru.hz.ejb.cluster.timer;

import java.util.Date;

/**
 * Created by mvj on 14.11.2016.
 */
public class DatedTimer extends CommonClusterTimer {
    private static final long serialVersionUID = 8623391700813568201L;
    private Date date;

    public Date getDate() {
        return new Date(date.getTime());
    }

    public void setDate(Date date) {
        this.date = new Date(date.getTime());
    }

    @Override
    public TimerType getTimerType() {
        return TimerType.DATED_TIMER;
    }

}
