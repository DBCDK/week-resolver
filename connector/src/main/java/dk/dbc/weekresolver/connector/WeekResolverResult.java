package dk.dbc.weekresolver.connector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Result data from resolving a weeknumber by use of a specific catalogue code
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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

    private WeekResolverResult(Date date, int weekNumber, int year, String weekCode, String catalogueCode) {
        this.date = date;
        this.weekNumber = weekNumber;
        this.year = year;
        this.weekCode = weekCode;
        this.catalogueCode = catalogueCode;
    }

    @SuppressWarnings("unused")
    public static WeekResolverResult create(Date date, int weekNumber, int year, String weekCode, String catalogueCode) {
        return new WeekResolverResult(date, weekNumber, year, weekCode, catalogueCode);
    }

    @SuppressWarnings("unused")
    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    @SuppressWarnings("unused")
    public void setYear(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    @SuppressWarnings("unused")
    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    @SuppressWarnings("unused")
    public void setCatalogueCode(String catalogueCode) {
        this.catalogueCode = catalogueCode;
    }

    public String getCatalogueCode() {
        return catalogueCode;
    }

    @SuppressWarnings("unused")
    public void setWeekCode(String weekCode) {
        this.weekCode = weekCode;
    }

    public String getWeekCode() {
        return weekCode;
    }

}
