/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.weekresolver.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeekResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolver.class);

    private LocalDate date = LocalDate.now();
    private String catalogueCode = "";

    // Easter sundays (source https://ugenr.dk)
    private static List<LocalDate> EasterSundays = new ArrayList<LocalDate>();
    static {
        EasterSundays.add(LocalDate.parse("2016-03-27"));
        EasterSundays.add(LocalDate.parse("2017-04-16"));
        EasterSundays.add(LocalDate.parse("2018-04-01"));
        EasterSundays.add(LocalDate.parse("2019-04-21"));
        EasterSundays.add(LocalDate.parse("2020-04-12"));
        EasterSundays.add(LocalDate.parse("2021-04-04"));
        EasterSundays.add(LocalDate.parse("2022-04-17"));
        EasterSundays.add(LocalDate.parse("2023-04-09"));
        EasterSundays.add(LocalDate.parse("2024-03-31"));
        EasterSundays.add(LocalDate.parse("2025-04-20"));
        EasterSundays.add(LocalDate.parse("2026-04-05"));
        EasterSundays.add(LocalDate.parse("2027-03-28"));
        EasterSundays.add(LocalDate.parse("2028-04-16"));
        EasterSundays.add(LocalDate.parse("2029-04-01"));
        EasterSundays.add(LocalDate.parse("2030-04-21"));
        EasterSundays.add(LocalDate.parse("2031-04-13"));
        EasterSundays.add(LocalDate.parse("2032-03-28"));
        EasterSundays.add(LocalDate.parse("2033-04-17"));
        EasterSundays.add(LocalDate.parse("2034-04-09"));
        EasterSundays.add(LocalDate.parse("2035-03-25"));
        EasterSundays.add(LocalDate.parse("2036-04-13"));
        EasterSundays.add(LocalDate.parse("2037-04-05"));
        EasterSundays.add(LocalDate.parse("2038-04-25"));
        EasterSundays.add(LocalDate.parse("2039-04-10"));
        EasterSundays.add(LocalDate.parse("2040-04-01"));
    }

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

        // Adjust date to match the given cataloguecode
        LocalDate forwardDate;
        switch (catalogueCode.toLowerCase()) {
            case "bpf":
                forwardDate = date.plusWeeks(2);
                break;
            default:
                throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }

        // Adjust the expected release date for christmas, easter and other non-working days
        LocalDate adjustedDate = getDateAdjustedForRestrictions(forwardDate, true);

        // Build final result
        WeekResolverResult result = new WeekResolverResult()
                .withDate(adjustedDate)
                .withCatalogueCode(catalogueCode.toUpperCase());
        return result.build();
    }

    /**
     * Calculate the next possible date for release of this record, taking into account
     * christmas, easter and other restricted dates as well as manually added restricted dates.
     * @param expectedDate The calendar date expected as release date by simple forward adjusting today's date
     * @param allowEndOfYearWeeks If set to true, then week 52 and 53 is allowed, otherwise the day is adjusted past these
     * @return The actual release date
     */
    private LocalDate getDateAdjustedForRestrictions(LocalDate expectedDate, boolean allowEndOfYearWeeks) {
        LocalDate adjustedDate = expectedDate;

        // 1. maj
        if( adjustedDate.getMonth() == Month.MAY && adjustedDate.getDayOfMonth() == 1 ) {
            adjustedDate = adjustedDate.plusDays(1);

            // Check for pinched friday
            if( adjustedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
                adjustedDate = adjustedDate.plusDays(1);
            }
        }

        // Grundlovsdag
        if( adjustedDate.getMonth() == Month.JUNE  && adjustedDate.getDayOfMonth() == 5 ) {
            adjustedDate = adjustedDate.plusDays(1);
        }

        // Christmas
        if( adjustedDate.getMonth() == Month.DECEMBER && adjustedDate.getDayOfMonth() == 24 ) {
            adjustedDate = adjustedDate.plusDays(1);
        }
        if( adjustedDate.getMonth() == Month.DECEMBER && adjustedDate.getDayOfMonth() == 25 ) {
            adjustedDate = adjustedDate.plusDays(1);
        }
        if( adjustedDate.getMonth() == Month.DECEMBER && adjustedDate.getDayOfMonth() == 26 ) {
            adjustedDate = adjustedDate.plusDays(1);
        }
        if( adjustedDate.getMonth() == Month.DECEMBER && adjustedDate.getDayOfMonth() == 27 && adjustedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
            adjustedDate = adjustedDate.plusDays(1);
        }

        // djust for week 52 and 53, which never is used!
        if( allowEndOfYearWeeks ) {
            // New years eve
            if (adjustedDate.getMonth() == Month.DECEMBER && adjustedDate.getDayOfMonth() == 31) {
                adjustedDate = adjustedDate.plusDays(1);
            }
        } else {
            // Spin past week 52 and 53
            DateTimeFormatter weekCodeFormatter = DateTimeFormatter.ofPattern("w");
            while( Integer.parseInt(adjustedDate.format(weekCodeFormatter)) == 52 || Integer.parseInt(adjustedDate.format(weekCodeFormatter)) == 53 ) {
                adjustedDate = adjustedDate.plusDays(1);
            }
        }

        // 1. January
        if( adjustedDate.getMonth() == Month.JANUARY && adjustedDate.getDayOfMonth() == 1 ) {
            adjustedDate = adjustedDate.plusDays(1);

            // Check for pinched friday
            if( adjustedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
                adjustedDate = adjustedDate.plusDays(1);
            }
        }

        // Weekends
        if( adjustedDate.getDayOfWeek() == DayOfWeek.SATURDAY ) {
            adjustedDate = adjustedDate.plusDays(1);
        }
        if( adjustedDate.getDayOfWeek() == DayOfWeek.SUNDAY ) {
            adjustedDate = adjustedDate.plusDays(1);
        }

        // Adjust for easter
        adjustedDate = getDateAdjustedForEaster(adjustedDate);

        // Return adjusted date
        LOGGER.info("Expected release date was {}, adjusted release date is {}", expectedDate, adjustedDate);
        return adjustedDate;
    }

    private LocalDate getDateAdjustedForEaster(LocalDate expectedDate) {

        // Locate easter sunday for current year
        Optional<LocalDate> optionalSunday = EasterSundays.stream().filter(x -> x.getYear() == expectedDate.getYear()).findFirst();
        if( !optionalSunday.isPresent() ) {
            LOGGER.warn("Request for date in the far-off past or future, date will not be adjusted for easter");
            return expectedDate;
        }
        LocalDate easterSunday = optionalSunday.get();
        LOGGER.info("Easter sunday for {} is {}", expectedDate.getYear(), easterSunday);

        // Check if the expected date is before Maundy Thursday
        if( expectedDate.isBefore(easterSunday.minusDays(3)) ) {
            LOGGER.info("{} is before {} (Maundy Thursday)", expectedDate, easterSunday.minusDays(3));
            return expectedDate;
        }

        // Check if the expected date is after or equal to the monday _after_ 2nd day of Easter
        if( expectedDate.isAfter(easterSunday.plusDays(7)) ) {
            LOGGER.info("{} is after {} (2nd day of Easter)", expectedDate, easterSunday.minusDays(3));
            return expectedDate;
        }

        // Expected date is somewhere in the easter period. Set the date to the monday after 2nd day of easter
        LOGGER.info("{} is in the easter/post-easter period, bringing date forward to next monday {}", expectedDate, easterSunday.plusDays(8));
        return easterSunday.plusDays(8);
    }
}
