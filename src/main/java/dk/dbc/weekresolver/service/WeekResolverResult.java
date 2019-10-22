/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.weekresolver.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * Result data from resolving a weeknumber by use of a specific catalogue code
 */
public class WeekResolverResult {
    // Calculated week number
    private int weekNumber;

    // Calculated year
    private int year;

    // Given cataloguecode, always in upper case
    private String catalogueCode;

    // Calculated weekcode
    private String weekCode;


    // The first possible date of release, adjusted for weeks into the future, shiftday and closing days
    // This is the date that is used to give the weeknumber and year - NOT the date that should be used
    // for other date fields, it only relates to the weekcode being calculated
    private Date date;

    public WeekResolverResult() {}

    private WeekResolverResult(LocalDate localDate, ZoneId zoneId, String catalogueCode) {
        this.date = Date.from(localDate.atStartOfDay(zoneId).toInstant());
        this.weekNumber=Integer.parseInt(localDate.format(DateTimeFormatter.ofPattern("w")));
        this.year=Integer.parseInt(localDate.format(DateTimeFormatter.ofPattern("YYYY")));
        this.weekCode=catalogueCode+year+String.format("%02d", weekNumber);
        this.catalogueCode=catalogueCode;
    }

    public static WeekResolverResult create(LocalDate localDate, ZoneId zoneId, String catalogueCode) {
        return new WeekResolverResult(localDate, zoneId, catalogueCode);
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public int getYear() {
        return year;
    }

    public Date getDate() {
        return date;
    }

    public String getCatalogueCode() {
        return catalogueCode;
    }

    public String getWeekCode() { return weekCode; }

}
