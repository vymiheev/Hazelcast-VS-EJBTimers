package ru.grfc.crashtest.cluster.timer;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mvj on 25.10.2017.
 */
public class ScheduleExpressionWrapper implements Serializable {
    private static final long serialVersionUID = 6540291616421613102L;
    private String second_ = "0";
    private String minute_ = "0";
    private String hour_ = "0";
    private String dayOfMonth_ = "*";
    private String month_ = "*";
    private String dayOfWeek_ = "*";
    private String year_ = "*";
    private String timezoneID_ = null;
    private Date start_ = null;
    private Date end_ = null;

    public ScheduleExpressionWrapper() {
    }

    public ScheduleExpressionWrapper second(String s) {
        this.second_ = s;
        return this;
    }

    public ScheduleExpressionWrapper second(int s) {
        this.second_ = s + "";
        return this;
    }

    public String getSecond() {
        return this.second_;
    }

    public ScheduleExpressionWrapper minute(String m) {
        this.minute_ = m;
        return this;
    }

    public ScheduleExpressionWrapper minute(int m) {
        this.minute_ = m + "";
        return this;
    }

    public String getMinute() {
        return this.minute_;
    }

    public ScheduleExpressionWrapper hour(String h) {
        this.hour_ = h;
        return this;
    }

    public ScheduleExpressionWrapper hour(int h) {
        this.hour_ = h + "";
        return this;
    }

    public String getHour() {
        return this.hour_;
    }

    public ScheduleExpressionWrapper dayOfMonth(String d) {
        this.dayOfMonth_ = d;
        return this;
    }

    public ScheduleExpressionWrapper dayOfMonth(int d) {
        this.dayOfMonth_ = d + "";
        return this;
    }

    public String getDayOfMonth() {
        return this.dayOfMonth_;
    }

    public ScheduleExpressionWrapper month(String m) {
        this.month_ = m;
        return this;
    }

    public ScheduleExpressionWrapper month(int m) {
        this.month_ = m + "";
        return this;
    }

    public String getMonth() {
        return this.month_;
    }

    public ScheduleExpressionWrapper dayOfWeek(String d) {
        this.dayOfWeek_ = d;
        return this;
    }

    public ScheduleExpressionWrapper dayOfWeek(int d) {
        this.dayOfWeek_ = d + "";
        return this;
    }

    public String getDayOfWeek() {
        return this.dayOfWeek_;
    }

    public ScheduleExpressionWrapper year(String y) {
        this.year_ = y;
        return this;
    }

    public ScheduleExpressionWrapper year(int y) {
        this.year_ = y + "";
        return this;
    }

    public String getYear() {
        return this.year_;
    }

    public ScheduleExpressionWrapper timezone(String timezoneID) {
        this.timezoneID_ = timezoneID;
        return this;
    }

    public String getTimezone() {
        return this.timezoneID_;
    }

    public ScheduleExpressionWrapper start(Date s) {
        this.start_ = s == null ? null : new Date(s.getTime());
        return this;
    }

    public Date getStart() {
        return this.start_ == null ? null : new Date(this.start_.getTime());
    }

    public ScheduleExpressionWrapper end(Date e) {
        this.end_ = e == null ? null : new Date(e.getTime());
        return this;
    }

    public Date getEnd() {
        return this.end_ == null ? null : new Date(this.end_.getTime());
    }

    public String toString() {
        return "ScheduleExpressionWrapper [second=" + this.second_ + ";minute=" + this.minute_ + ";hour=" + this.hour_ + ";dayOfMonth=" + this.dayOfMonth_ + ";month=" + this.month_ + ";dayOfWeek=" + this.dayOfWeek_ + ";year=" + this.year_ + ";timezoneID=" + this.timezoneID_ + ";start=" + this.start_ + ";end=" + this.end_ + "]";
    }
}
