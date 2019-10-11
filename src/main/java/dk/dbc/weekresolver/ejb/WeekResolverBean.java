/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.weekresolver.ejb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class WeekResolverBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverBean.class);

    /**
     * Get week id based on type(to be elaborated) and a date.
     *
     * @param catalogueCode Cataloguecode
     * @param date (yyyy-MM-dd)
     * @return a HTTP 200 with the week-code as a string
     * @throws DateTimeParseException if specified date is not parseable
     * @throws UnsupportedOperationException if the specified cataloguecode is unkown or unsupported
     *
     */
    public WeekResolverResult getWeekCode(final String catalogueCode, final String date)
            throws DateTimeParseException, UnsupportedOperationException {
        LOGGER.info("Week-code requested for catalogueCode={} and date={}", catalogueCode, date);

        // Todo: We are not entirely sure that this is the dateformat we want to use.
        //       Adjust when clients are better known.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateForRequestedWeekCode = LocalDate.parse(date, formatter);

        // Calculate the weekcode
        return CalculateWeekCodeForCatalogueCode(catalogueCode, dateForRequestedWeekCode);
    }

    /**
     * Calculate the weekcode for the given date depending on the cataloguecode
     * @param catalogueCode
     * @param date
     * @return a string with the weekcode
     * @throws UnsupportedOperationException if the cataloguecode is not supported
     */
    private WeekResolverResult CalculateWeekCodeForCatalogueCode(final String catalogueCode, final LocalDate date)
        throws UnsupportedOperationException {
        LOGGER.info("Calculating weekcode for catalogueCode={} and date={}", catalogueCode, date);

        // Pick the weeknumber calculator needed by the given cataloguecode.
        // Do note that the calculator must calculate weeknumber AND year, since the rules for selecting a
        // weeknumber may result in a weeknumber BEFORE or AFTER the calendar week, thus possibly referring
        // to the next or previous year.
        WeekResolverResult result;
        switch( catalogueCode.toLowerCase() ) {
            case "bpf":
                result = CalculateCalendarWeekAndYear(date);
                break;
            default:
                throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }

        // Update the result object with the cataloguecode in uppercase and the resulting weekcode. It is very
        // unlikely that 'year' will be anything but 4 digits, but if so, this will break.
        result.CatalogueCode = catalogueCode.toUpperCase();
        result.WeekCode = result.CatalogueCode + result.Year + String.format("%02d", result.WeekNumber);
        LOGGER.info("Calculated weekcode by use of cataloguecode {} is {}", catalogueCode, result.WeekCode);
        return result;
    }

    /**
     * Calculate the year and weeknumber for cataloguecodes using strict ISO weeknumbers
     * @param date Date for the requested weeknumber and year
     * @return a WeekResolverResult with WeekNumber and Year initialized
     */
    private WeekResolverResult CalculateCalendarWeekAndYear(final LocalDate date) {
        LOGGER.info("Using calender week and week-based-year");

        // Get the week number using formatter 'week-of-week-based-year'. Per ISO-8601 a week starts on monday
        // so this number is compatible with the danish weeknumber system.
        WeekResolverResult result = new WeekResolverResult();
        result.WeekNumber = Integer.parseInt(date.format(DateTimeFormatter.ofPattern("w")));
        result.Year = Integer.parseInt(date.format(DateTimeFormatter.ofPattern("YYYY")));
        return result;
    }
}
