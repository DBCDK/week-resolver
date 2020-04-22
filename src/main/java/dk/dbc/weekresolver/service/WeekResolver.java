/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.weekresolver.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeekResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolver.class);

    private LocalDate date = LocalDate.now();
    private String catalogueCode = "";
    private ZoneId zoneId;

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

    public WeekResolver(String timezone) {
        this.zoneId = ZoneId.of(timezone);
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

        // Get the current date
        LocalDate expectedDate = date;

        // Select configuration of weekcode calculation
        switch (catalogueCode.toUpperCase()) {

            case "DPF":
            case "FPF":
            case "GPF":
                return BuildForConfiguration(expectedDate, 2, DayOfWeek.FRIDAY, true, false);
            case "EMO":
            case "EMS":
                return BuildForConfiguration(expectedDate, 1, DayOfWeek.SUNDAY, true, true);

            default:
                throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }
    }

    /**
     * Calculate the weekcode for the given date with the given configuration
     *
     * @param expectedDate The initial date for which the weekcode should be calculated
     * @param addWeeks The number of weeks to add to the actual week number
     * @param shiftDay The day of the week where the current week number should shift to the next week
     * @param allowEndOfYearWeeks If true then allow weekcodes for the last weeks of the year (christmas period)
     * @param ignoreClosingDays If true then ignore closing days when calculating the weekcode
     * @return a string with the weekcode
     */
    private WeekResolverResult BuildForConfiguration(LocalDate expectedDate, int addWeeks, DayOfWeek shiftDay, boolean allowEndOfYearWeeks, boolean ignoreClosingDays) {

        // Algorithm:
        //   step 1: Shift to next working day for weekends - unless shiftday is in the weekend
        //   step 2: take date and add [0,1,2,...] weeks
        //   step 3: if closingday then add 1 week
        //   step 4: if shiftday => add 1 week
        //
        // Allthough it is a cornercase, we need to check that a shifted date does not end up
        // being a closing day.., 1. may and 5. june could in rare cases give this result. So:
        //
        //   step 5: while day is a closingday => add 1 week

        // Step 1: Automated systems may request weekcodes on Weekend days.
        //         Push the date forward to the comming monday unless shiftday is actually in the weekend
        if( shiftDay != DayOfWeek.SATURDAY && shiftDay != DayOfWeek.SUNDAY ) {
            if(expectedDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                expectedDate = expectedDate.plusDays(2);
                LOGGER.info("Date {} is a saturday. Moving date to next monday {}", date, expectedDate);
            }
            if(expectedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                expectedDate = expectedDate.plusDays(1);
                LOGGER.info("Date {} is a sunday. Moving date to next monday {}", date, expectedDate);
            }
        } else {
            LOGGER.info("Date {} is a {} but shiftday is {}. Ignoring weekends", date, date.getDayOfWeek(), shiftDay);
        }

        // Step 2: add the selected number of weeks
        expectedDate = expectedDate.plusWeeks(addWeeks);
        LOGGER.info("date shifted {} week(s) {}", addWeeks, expectedDate);

        // Step 3: Is this a closing day ?
        if( !ignoreClosingDays && isClosingDay(expectedDate, allowEndOfYearWeeks) ) {
            expectedDate = expectedDate.plusWeeks(1);
            LOGGER.info("date shifted 1 week due to closing day to {}", expectedDate);
        }

        // Step 4: Is this on or after the shiftday ?
        if( expectedDate.getDayOfWeek().getValue() >= shiftDay.getValue() ) {
            expectedDate = expectedDate.plusWeeks(1);
            LOGGER.info("date shifted 1 week due to shiftday to {}", expectedDate);
        }

        // Step 5: Make sure the resulting date is not also a closing day
        while( ignoreClosingDays && isClosingDay(expectedDate, allowEndOfYearWeeks) ) {
            expectedDate = expectedDate.plusDays(1);
            LOGGER.info("date shifted 1 day due to final date being a closing day {}", expectedDate);
        }

        // Build final result. Hardwire the local to da_DK since we may encounter different locales (badly configured dev. machines etc.)
        Locale locale = new Locale("da", "DK");
        LOGGER.info("Date {} pushed to final date {} with weeknumber {}", date, expectedDate, Integer.parseInt(expectedDate.format(DateTimeFormatter.ofPattern("w", locale))));

        int weekNumber = Integer.parseInt(expectedDate.format(DateTimeFormatter.ofPattern("w", locale)));
        int year = Integer.parseInt(expectedDate.format(DateTimeFormatter.ofPattern("YYYY")));
        String weekCode = catalogueCode.toUpperCase() + year + String.format("%02d", weekNumber);
        Date date = Date.from(expectedDate.atStartOfDay(zoneId).toInstant());
        WeekResolverResult result = WeekResolverResult.create(date, weekNumber, year, weekCode, catalogueCode.toUpperCase());
        return result;
    }

    /**
     * Check if the given date is a closed date
     * @param expectedDate The calendar date expected as release date
     * @param allowEndOfYearWeeks If set to true, then week 52 and 53 is allowed, otherwise these days are also closing days
     * @return True if the date is a closing day, otherwise false
     */
    private boolean isClosingDay(LocalDate expectedDate, boolean allowEndOfYearWeeks) {

        // 1. maj and pinched friday
        if( expectedDate.getMonth() == Month.MAY && expectedDate.getDayOfMonth() == 1 ) {
            LOGGER.info("{} is 1. may", expectedDate);
            return true;
        }
        if( expectedDate.getMonth() == Month.MAY && expectedDate.getDayOfMonth() == 2 && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
            LOGGER.info("{} is pinched friday after 1. may", expectedDate);
            return true;
        }

        // Grundlovsdag and pinched friday
        if( expectedDate.getMonth() == Month.JUNE  && expectedDate.getDayOfMonth() == 5 ) {
            LOGGER.info("{} is constitution day (grundlovsdag)", expectedDate);
            return true;
        }
        if( expectedDate.getMonth() == Month.JUNE && expectedDate.getDayOfMonth() == 6 && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
            LOGGER.info("{} is pinched friday af constitution day (grundlovsdag)", expectedDate);
            return true;
        }

        // Christmas and pinched friday
        if( expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 24 ||
                expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 25 ||
                expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 26) {
            LOGGER.info("{} is a christmas day", expectedDate);
            return true;
        }
        if( expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 27 && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
            LOGGER.info("{} is pinched friday after christmas", expectedDate);
            return true;
        }

        // Check for week 52 and 53, which never is used!
        if( allowEndOfYearWeeks ) {
            // New years eve
            if (expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 31) {
                LOGGER.info("{} is new years eve", expectedDate);
                return true;
            }
        } else {
            // Check for week 52 or 53
            DateTimeFormatter weekCodeFormatter = DateTimeFormatter.ofPattern("w");
            if( Integer.parseInt(expectedDate.format(weekCodeFormatter)) == 52 || Integer.parseInt(expectedDate.format(weekCodeFormatter)) == 53 ) {
                LOGGER.info("{} is within week 52 or 53", expectedDate);
                return true;
            }
        }

        // 1. January and pinched friday
        if( expectedDate.getMonth() == Month.JANUARY && expectedDate.getDayOfMonth() == 1 ) {
            LOGGER.info("{} is 1. jan (hangover day)", expectedDate);
            return true;
        }
        if( expectedDate.getMonth() == Month.JANUARY && expectedDate.getDayOfMonth() == 2 && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
            LOGGER.info("{} is pinched friday after 1. jan (hangover day for older people)", expectedDate);
            return true;
        }

        // Check for easter, pentecost and ascension day
        if( isEasterAndRelatedClosingDay(expectedDate) ) {
            LOGGER.info("{} is within easter, pentecost or ascension day", expectedDate);
            return true;
        }

        // Not a closing day
        LOGGER.info("{} is not a closing day", expectedDate);
        return false;
    }

    /**
     * Check if the given date is a date within the easter or related closing days
     * (pentecost, ascension Day)
     * @param expectedDate The date to check
     * @return True if the date is within the easter, pentecost or ascension days, otherwise false
     */
    private boolean isEasterAndRelatedClosingDay(LocalDate expectedDate) {

        // Locate easter sunday for current year
        Optional<LocalDate> optionalSunday = EasterSundays.stream().filter(x -> x.getYear() == expectedDate.getYear()).findFirst();
        if( !optionalSunday.isPresent() ) {
            LOGGER.warn("Request for date in the far-off past or future, date will not be checked for easter");
            return false;
        }
        LocalDate easterSunday = optionalSunday.get();
        LOGGER.info("Easter sunday for {} is {}", expectedDate.getYear(), easterSunday);

        // Check if the expected date is before Maundy Thursday
        if( expectedDate.isAfter(easterSunday.minusDays(4)) && expectedDate.isBefore(easterSunday.plusDays(2)) ) {
            LOGGER.info("{} is within easter", expectedDate);
            return true;
        }

        // Pentecost. First sunday after 50 days after easter sunday
        LocalDate pentecost =  easterSunday.plusDays(50);
        while( pentecost.getDayOfWeek() != DayOfWeek.SUNDAY ) {
            pentecost = pentecost.plusDays(1);
        }
        if( expectedDate == pentecost.plusDays(1) ) { // Check for withsun
            LOGGER.info("{} is withsun", expectedDate);
            return true;
        }

        // Ascension day. 40 days after easter sunday. Check also for pinched freday
        LocalDate ascensionDay = easterSunday.plusDays(40);
        if( expectedDate == ascensionDay ) {
            LOGGER.info("{} is ascension day", expectedDate);
            return true;
        }
        if( expectedDate == ascensionDay.plusDays(1) && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
            LOGGER.info("{} is pinched friday after ascension day");
            return true;
        }

        // Check for 'store bededag', tounge-in-cheek english name 'prayers day'
        LocalDate prayersDay = easterSunday.plusDays(26);
        if( expectedDate == prayersDay ) {
            LOGGER.info("{} is prayers day ('store bededag')", expectedDate);
            return true;
        }
        if( expectedDate == prayersDay.plusDays(1) && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
            LOGGER.info("{} is pinched friday after prayers day ('store bededag')");
            return true;
        }

        // Expected date is somewhere in the easter period.
        LOGGER.info("{} is not easter, pentecost or ascension day", expectedDate);
        return false;
    }
}
