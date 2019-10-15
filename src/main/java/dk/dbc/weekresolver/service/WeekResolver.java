/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.weekresolver.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeekResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolver.class);

    private LocalDate date;
    private String catalogueCode;

    public WeekResolver withDate(String date) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.date = LocalDate.parse(date, formatter);
        return this;
    }

    public WeekResolver withCatalogueCode(String catalogueCode) {
        this.catalogueCode = catalogueCode;
        return this;
    }

    /**
     * Calculate the weekcode for the given date depending on the cataloguecode
     *
     * @return a string with the weekcode
     * @throws UnsupportedOperationException if the cataloguecode is not supported
     */
    public WeekResolverResult build() throws UnsupportedOperationException {
        LOGGER.info("Calculating weekcode for catalogueCode={} and date={}", catalogueCode, date);

        // Pick the weeknumber calculator needed by the given cataloguecode.
        // Do note that the calculator must calculate weeknumber AND year, since the rules for selecting a
        // weeknumber may result in a weeknumber BEFORE or AFTER the calendar week, thus possibly referring
        // to the next or previous year.
        WeekResolverResult result;
        switch (catalogueCode.toLowerCase()) {
            case "bpf":
                result = CalculateCalendarWeekAndYear(date);
                break;
            default:
                throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }

        // Update the result object with the cataloguecode in uppercase and the resulting weekcode. It is very
        // unlikely that 'year' will be anything but 4 digits, but if so, this will break.
        result.setCatalogueCode(catalogueCode.toUpperCase());
        result.setWeekCode(result.getCatalogueCode() + result.getYear() + String.format("%02d", result.getWeekNumber()));
        LOGGER.info("Calculated weekcode by use of cataloguecode {} is {}", catalogueCode, result.getWeekCode());
        return result;
    }

    /**
     * Calculate the year and weeknumber for cataloguecodes using strict ISO weeknumbers
     *
     * @param date Date for the requested weeknumber and year
     * @return a WeekResolverResult with WeekNumber and Year initialized
     */
    private WeekResolverResult CalculateCalendarWeekAndYear(final LocalDate date) {
        LOGGER.info("Using calender week and week-based-year");

        // Get the week number using formatter 'week-of-week-based-year'. Per ISO-8601 a week starts on monday
        // so this number is compatible with the danish weeknumber system.
        WeekResolverResult result = new WeekResolverResult();
        result.setWeekNumber(Integer.parseInt(date.format(DateTimeFormatter.ofPattern("w"))));
        result.setYear(Integer.parseInt(date.format(DateTimeFormatter.ofPattern("YYYY"))));
        return result;
    }
}
