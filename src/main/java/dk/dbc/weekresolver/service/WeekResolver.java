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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeekResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolver.class);

    private LocalDate date = LocalDate.now();
    private String catalogueCode = "";
    private ZoneId zoneId = ZoneId.of("Europe/Copenhagen");
    private Locale locale = new Locale("da", "DK");

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

    private static HashMap<String, WeekCodeConfiguration> codes = new HashMap<>();
    static {
        // No shiftday, no added weeks, allowing end-of-year and closingdays
        codes.put("ACC", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("ACE", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("ACF", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("ACT", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("ACM", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("ARK", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("BLG", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());

        // Shiftday friday, add 1 week
        codes.put("BKX", new WeekCodeConfiguration().addWeeks(1).withShiftDay(DayOfWeek.FRIDAY)); // = BMK-1

        // Shiftday friday, add 2 weeks, allow end-of-year
        codes.put("DPF", new WeekCodeConfiguration().withShiftDay(DayOfWeek.FRIDAY).addWeeks(2).allowEndOfYear());
        codes.put("FPF", new WeekCodeConfiguration().withShiftDay(DayOfWeek.FRIDAY).addWeeks(2).allowEndOfYear());
        codes.put("GPF", new WeekCodeConfiguration().withShiftDay(DayOfWeek.FRIDAY).addWeeks(2).allowEndOfYear());

        // Shiftday friday, add 1 week, allow end-of-year and ignore closing days
        codes.put("EMO", new WeekCodeConfiguration().addWeeks(1).allowEndOfYear().ignoreClosingDays());
        codes.put("EMS", new WeekCodeConfiguration().addWeeks(1).allowEndOfYear().ignoreClosingDays());

        // No shiftday , add 1 week
        codes.put("DAN", new WeekCodeConfiguration().addWeeks(1));
        codes.put("DAR", new WeekCodeConfiguration().addWeeks(1));

        // Shiftday friday, add 1 week
        codes.put("UTI", new WeekCodeConfiguration().addWeeks(1).withShiftDay(DayOfWeek.FRIDAY));

        // Shiftday friday, add 2 weeks
        codes.put("DLR", new WeekCodeConfiguration().addWeeks(2));
        codes.put("BKM", new WeekCodeConfiguration().addWeeks(2));
        codes.put("DBI", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("FSB", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("NLL", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("NLY", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));

        // No shiftday, add 3 weeks
        codes.put("DBF", new WeekCodeConfiguration().addWeeks(3));

        // Shiftday friday, add 3 weeks
        codes.put("DLF", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("DMO", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("ERL", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("FSC", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("IDU", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("SNE", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY));

        // Shiftday friday, add 1 week. Will be modified when the record is being edited
        codes.put("BKR", new WeekCodeConfiguration().addWeeks(1).withShiftDay(DayOfWeek.FRIDAY));

        // Greenland
        codes.put("GBF", new WeekCodeConfiguration().addWeeks(5).withShiftDay(DayOfWeek.FRIDAY));

        // Use default, most often used, value DIS197605. This may have to be corrected if the record
        // is indeed an old unowned KB record or the like
        codes.put("DIS", new WeekCodeConfiguration().withFixedWeekCode("197605"));

        // Use code for incomplete record. Finished record may become 197604 or 197607
        codes.put("OPR", new WeekCodeConfiguration().withFixedWeekCode("197601"));

        // Use the month number instead of the week number
        codes.put("PLA", new WeekCodeConfiguration().useMonthNumber().withShiftDay(DayOfWeek.FRIDAY));
        codes.put("PLN", new WeekCodeConfiguration().useMonthNumber().withShiftDay(DayOfWeek.FRIDAY));

        // Use fixed codes for these cataloguecodes
        codes.put("DBR", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("DBT", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("SDT", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("DIG", new WeekCodeConfiguration().withFixedWeekCode("198507"));
        codes.put("ERA", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("ERE", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("FFK", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("FSF", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("HOB", new WeekCodeConfiguration().withFixedWeekCode("197300"));
    }

    public WeekResolver() {}

    public WeekResolver(String timezone) {
        this.zoneId = ZoneId.of(timezone);
    }

    public WeekResolver(String timezone, Locale locale) {
        this.zoneId = ZoneId.of(timezone);
        this.locale = locale;
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

    public WeekResolver withTimeZone(String timezone) {
        this.zoneId = ZoneId.of(timezone);
        return this;
    }

    public WeekResolver withLocale(Locale locale) {
        this.locale = locale;
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
        if( !codes.containsKey(catalogueCode.toUpperCase()) ){
            throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }

        return BuildForConfiguration(expectedDate, codes.get(catalogueCode.toUpperCase()));
    }

    /**
     * Calculate the weekcode for the given date with the given configuration
     *
     * @param configuration The configuration to use when generating the weekcode
     * @return a WeekResolverResult
     */
    private WeekResolverResult BuildForConfiguration(LocalDate expectedDate, WeekCodeConfiguration configuration) {

        // If the configuration has a fixed weekcode, return this
        if( configuration.getFixedWeekCode() != null ) {
            LOGGER.info("Return fixed code {}", catalogueCode.toUpperCase() + configuration.getFixedWeekCode(), catalogueCode.toUpperCase());
            return new WeekResolverResult(Date.from(expectedDate.atStartOfDay(zoneId).toInstant()),
                    0, 0, catalogueCode.toUpperCase() + configuration.getFixedWeekCode(), catalogueCode.toUpperCase());
        }

        // Algorithm:
        //   step 1: If on or after shiftday, shift to first day in the next week
        //   step 2: take date and add [0,1,2,...] weeks
        //   step 3: if closingday then add 1 day untill we reach a working day

        // Step 1: Is this on or after the shiftday ?
        while( configuration.getShiftDay() != null && expectedDate.getDayOfWeek().getValue() >= configuration.getShiftDay().getValue() ) {
            expectedDate = expectedDate.plusDays(1);
            LOGGER.info("date shifted 1 day due to shiftday to {}", expectedDate);
        }

        // Step 2: add the selected number of weeks
        expectedDate = expectedDate.plusWeeks(configuration.getAddWeeks());
        LOGGER.info("date shifted {} week(s) {}", configuration.getAddWeeks(), expectedDate);

        // Step 3: Is this a closing day ?
        while( !configuration.getIgnoreClosingDays() && isClosingDay(expectedDate, configuration.getAllowEndOfYear()) ) {
            expectedDate = expectedDate.plusDays(1);
            LOGGER.info("date shifted 1 day due to closing day to {}", expectedDate);
        }

        // Build final result.
        LOGGER.info("Date {} pushed to final date {} with weeknumber {}", date, expectedDate, Integer.parseInt(expectedDate.format(DateTimeFormatter.ofPattern("w", locale))));

        int weekNumber = Integer.parseInt(expectedDate.format(DateTimeFormatter.ofPattern("w", locale)));
        int year = Integer.parseInt(expectedDate.format(DateTimeFormatter.ofPattern("YYYY")));
        int month = Integer.parseInt(expectedDate.format(DateTimeFormatter.ofPattern("MM")));
        String weekCode = catalogueCode.toUpperCase() + year + String.format("%02d", configuration.getUseMonthNumber() ? month : weekNumber);
        Date date = Date.from(expectedDate.atStartOfDay(zoneId).toInstant());
        return new WeekResolverResult(date, weekNumber, year, weekCode, catalogueCode.toUpperCase());
    }

    /**
     * Check if the given date is a closed date
     * @param expectedDate The calendar date expected as release date
     * @param allowEndOfYearWeeks If set to true, then week 52 and 53 is allowed, otherwise these days are also closing days
     * @return True if the date is a closing day, otherwise false
     */
    private boolean isClosingDay(LocalDate expectedDate, boolean allowEndOfYearWeeks) {

        // Weekends
        if( expectedDate.getDayOfWeek() == DayOfWeek.SATURDAY || expectedDate.getDayOfWeek() == DayOfWeek.SUNDAY ) {
            LOGGER.info("{} is saturday or sunday", expectedDate);
            return true;
        }

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
