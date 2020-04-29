package dk.dbc.weekresolver.service;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class WeekCodeConfiguration {
    private String fixedWeekCode;
    private int addWeeks;
    private DayOfWeek shiftDay;
    private boolean allowEndOfYear;
    private boolean ignoreClosingDays;
    private boolean useMonthNumber;
    private LocalDate date;


    public WeekCodeConfiguration() {
        this.fixedWeekCode = null;
        this.addWeeks = 0;
        this.shiftDay = null;
        this.allowEndOfYear = false;
        this.ignoreClosingDays = false;
        this.useMonthNumber = false;
        this.date = LocalDate.now();

    }

    public WeekCodeConfiguration addWeeks(int addWeeks) {
        this.addWeeks = addWeeks;
        return this;
    }

    public WeekCodeConfiguration withShiftDay(DayOfWeek shiftDay) {
        this.shiftDay = shiftDay;
        return this;
    }

    public WeekCodeConfiguration allowEndOfYear() {
        this.allowEndOfYear = true;
        return this;
    }

    public WeekCodeConfiguration ignoreClosingDays() {
        this.ignoreClosingDays = true;
        return this;
    }

    public WeekCodeConfiguration useMonthNumber() {
        this.useMonthNumber = true;
        return this;
    }

    public WeekCodeConfiguration withFixedWeekCode(String weekCode) {
        this.fixedWeekCode = weekCode;
        return this;
    }

    public WeekCodeConfiguration withDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public DayOfWeek getShiftDay() {
        return this.shiftDay;
    }

    public int getAddWeeks() {
        return this.addWeeks;
    }

    public boolean getAllowEndOfYear() {
        return this.allowEndOfYear;
    }

    public boolean getIgnoreClosingDays() {
        return this.ignoreClosingDays;
    }

    public boolean getUseMonthNumber() {
        return this.useMonthNumber;
    }

    public String getFixedWeekCode() {
        return this.fixedWeekCode;
    }
}
