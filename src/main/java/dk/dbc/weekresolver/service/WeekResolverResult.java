/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.weekresolver.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Result data from resolving a weeknumber by use of a specific catalogue code
 */
public class WeekResolverResult {

    private int weekNumber;
    private int year;
    private String catalogueCode;
    private String weekCode;
    private LocalDate date;

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCatalogueCode() {
        return catalogueCode;
    }

    public void setCatalogueCode(String catalogueCode) {
        this.catalogueCode = catalogueCode;
    }

    public String getWeekCode() {
        return weekCode;
    }

    public void setWeekCode(String weekCode) {
        this.weekCode = weekCode;
    }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public WeekResolverResult withDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public WeekResolverResult withCatalogueCode(String catalogueCode) {
        this.catalogueCode = catalogueCode;
        return this;
    }

    public WeekResolverResult build() {
        setWeekNumber(Integer.parseInt(date.format(DateTimeFormatter.ofPattern("w"))));
        setYear(Integer.parseInt(date.format(DateTimeFormatter.ofPattern("YYYY"))));
        setWeekCode(getCatalogueCode() + getYear() + String.format("%02d", getWeekNumber()));
        return this;
    }
}
