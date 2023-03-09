package dk.dbc.weekresolver.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

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

    private WeekDescription description;

    @SuppressWarnings("unused")
    public WeekResolverResult() {}

    public WeekResolverResult(Date date, int weekNumber, int year, String weekCode, String catalogueCode, WeekDescription description) {
        this.date = date;
        this.weekNumber=weekNumber;
        this.year=year;
        this.weekCode=weekCode;
        this.catalogueCode=catalogueCode;
        this.description = description;
    }

    public WeekResolverResult(WeekCodeConfiguration configuration, ZoneId zoneId, Locale locale, String catalogueCode, LocalDate finalDate, WeekDescription description) {
        if( configuration.getFixedWeekCode() != null ) {
            this.date = Date.from(finalDate.atStartOfDay(zoneId).toInstant());
            this.weekNumber = 0;
            this. year = 0;
            this.weekCode = catalogueCode.toUpperCase() + configuration.getFixedWeekCode();
            this.catalogueCode = catalogueCode.toUpperCase();
        } else {
            this.weekNumber = Integer.parseInt(finalDate.format(DateTimeFormatter.ofPattern("w", locale).withZone(zoneId)));
            //noinspection SuspiciousDateFormat
            this.year = Integer.parseInt(finalDate.format(DateTimeFormatter.ofPattern("YYYY", locale))); // MUST be 'week year' (upper case 'YYYY') format, NOT 'year' (lower case 'yyyy')
            int month = Integer.parseInt(finalDate.format(DateTimeFormatter.ofPattern("MM", locale)));
            this.weekCode = catalogueCode.toUpperCase() + year + String.format("%02d", configuration.getUseMonthNumber() ? month : weekNumber);
            this.date = Date.from(finalDate.atStartOfDay(zoneId).toInstant());
            this.catalogueCode = catalogueCode.toUpperCase();
        }
        this.description = description;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public WeekResolverResult withWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
        return this;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public WeekResolverResult withYear(int year) {
        this.year = year;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public WeekResolverResult withDate(Date date) {
        this.date = date;
        return this;
    }

    public String getCatalogueCode() {
        return catalogueCode;
    }

    public void setCatalogueCode(String catalogueCode) {
        this.catalogueCode = catalogueCode;
    }

    public WeekResolverResult withCatalogueCode(String catalogueCode) {
        this.catalogueCode = catalogueCode;
        return this;
    }

    public String getWeekCode() { return weekCode; }

    public void setWeekCode(String weekCode) {
        this.weekCode = weekCode;
    }

    public WeekResolverResult withWeekCode(String weekCode) {
        this.weekCode = weekCode;
        return this;
    }

    public WeekDescription getDescription() {
        return description;
    }

    public void setDescription(WeekDescription description) {
        this.description = description;
    }

    public WeekResolverResult withWeekDescription(WeekDescription weekDescription) {
        this.description = weekDescription;
        return this;
    }

    @Override
    public String toString() {
        return "WeekResolverResult{" +
                "weekNumber=" + weekNumber +
                ", year=" + year +
                ", catalogueCode='" + catalogueCode + '\'' +
                ", weekCode='" + weekCode + '\'' +
                ", date=" + date +
                ", description=" + description +
                '}';
    }
}
