package dk.dbc.weekresolver.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import dk.dbc.weekresolver.model.WeekCodeConfiguration;
import dk.dbc.weekresolver.model.WeekDescription;
import dk.dbc.weekresolver.model.WeekResolverResult;
import dk.dbc.weekresolver.model.YearPlanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.BadRequestException;

public class WeekResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolver.class);

    private LocalDate date = LocalDate.now();
    private String catalogueCode = "";
    private ZoneId zoneId = ZoneId.of("Europe/Copenhagen");
    private Locale locale = new Locale("da", "DK");

    // Easter sundays (source https://ugenr.dk)
    private static final List<LocalDate> EASTER_SUNDAYS = new ArrayList<>();
    static {
        EASTER_SUNDAYS.add(LocalDate.parse("2016-03-27"));
        EASTER_SUNDAYS.add(LocalDate.parse("2017-04-16"));
        EASTER_SUNDAYS.add(LocalDate.parse("2018-04-01"));
        EASTER_SUNDAYS.add(LocalDate.parse("2019-04-21"));
        EASTER_SUNDAYS.add(LocalDate.parse("2020-04-12"));
        EASTER_SUNDAYS.add(LocalDate.parse("2021-04-04"));
        EASTER_SUNDAYS.add(LocalDate.parse("2022-04-17"));
        EASTER_SUNDAYS.add(LocalDate.parse("2023-04-09"));
        EASTER_SUNDAYS.add(LocalDate.parse("2024-03-31"));
        EASTER_SUNDAYS.add(LocalDate.parse("2025-04-20"));
        EASTER_SUNDAYS.add(LocalDate.parse("2026-04-05"));
        EASTER_SUNDAYS.add(LocalDate.parse("2027-03-28"));
        EASTER_SUNDAYS.add(LocalDate.parse("2028-04-16"));
        EASTER_SUNDAYS.add(LocalDate.parse("2029-04-01"));
        EASTER_SUNDAYS.add(LocalDate.parse("2030-04-21"));
        EASTER_SUNDAYS.add(LocalDate.parse("2031-04-13"));
        EASTER_SUNDAYS.add(LocalDate.parse("2032-03-28"));
        EASTER_SUNDAYS.add(LocalDate.parse("2033-04-17"));
        EASTER_SUNDAYS.add(LocalDate.parse("2034-04-09"));
        EASTER_SUNDAYS.add(LocalDate.parse("2035-03-25"));
        EASTER_SUNDAYS.add(LocalDate.parse("2036-04-13"));
        EASTER_SUNDAYS.add(LocalDate.parse("2037-04-05"));
        EASTER_SUNDAYS.add(LocalDate.parse("2038-04-25"));
        EASTER_SUNDAYS.add(LocalDate.parse("2039-04-10"));
        EASTER_SUNDAYS.add(LocalDate.parse("2040-04-01"));
    }

    private static final HashMap<String, WeekCodeConfiguration> codes = new HashMap<>();
    static {
        // No shiftday, no added weeks, allowing end-of-year and closingdays
        codes.put("ACC", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays()); // DMatV2
        codes.put("ACE", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays()); // DMatV2
        codes.put("ACF", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("ACK", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("ACM", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("ACN", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("ACP", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("ACT", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays()); // DMatV2
        codes.put("ARK", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        codes.put("BLG", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());

        // Shiftday friday, add 1 week
        codes.put("BKX", new WeekCodeConfiguration().addWeeks(1).withShiftDay(DayOfWeek.FRIDAY));

        // Shiftday friday, add 3 weeks, allow end-of-year
        codes.put("DPF", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear()); // DataIO (automarc)
        codes.put("FPF", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear());
        codes.put("GPF", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear()); // DataIO (periodicJobs)

        // Shiftday friday, add 1 week, allow end-of-year and ignore closing days
        codes.put("EMO", new WeekCodeConfiguration().addWeeks(1).allowEndOfYear().ignoreClosingDays());
        codes.put("EMS", new WeekCodeConfiguration().addWeeks(1).allowEndOfYear().ignoreClosingDays());

        // No shiftday , add 1 week
        codes.put("DAN", new WeekCodeConfiguration().addWeeks(1));
        codes.put("DAR", new WeekCodeConfiguration().addWeeks(1));

        // Shiftday friday, add 1 week
        codes.put("UTI", new WeekCodeConfiguration().addWeeks(1).withShiftDay(DayOfWeek.FRIDAY));

        // Shiftday friday, add 2 weeks
        codes.put("DBR", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        codes.put("DLR", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        codes.put("DBF", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        codes.put("DLF", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        codes.put("DBI", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("FSB", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("BKM", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DataIO, DMatV2
        codes.put("DMO", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("FSC", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("IDU", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("SNE", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("LEK", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("MMV", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("FIV", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        codes.put("ERA", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        codes.put("ERE", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        codes.put("NLL", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        codes.put("NLY", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        codes.put("ERL", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2

        // Shiftday friday, add 1 week. Will be modified when the record is being edited
        codes.put("BKR", new WeekCodeConfiguration().addWeeks(1).withShiftDay(DayOfWeek.FRIDAY));

        // Greenland
        codes.put("GBF", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));

        // Use default, most often used, value DIS197605. This may have to be corrected if the record
        // is indeed an old unowned KB record or the like
        codes.put("DIS", new WeekCodeConfiguration().withFixedWeekCode("197605"));

        // Use code for incomplete record. Finished record may become 197604 or 197607
        codes.put("OPR", new WeekCodeConfiguration().withFixedWeekCode("197601"));

        // Use the month number instead of the week number
        codes.put("PLA", new WeekCodeConfiguration().useMonthNumber().withShiftDay(DayOfWeek.FRIDAY));
        codes.put("PLN", new WeekCodeConfiguration().useMonthNumber().withShiftDay(DayOfWeek.FRIDAY));

        // Use fixed codes for these cataloguecodes
        codes.put("DBT", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("SDT", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("DIG", new WeekCodeConfiguration().withFixedWeekCode("198507"));
        codes.put("FFK", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("FSF", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        codes.put("HOB", new WeekCodeConfiguration().withFixedWeekCode("197300"));
    }

    // Make sure we all agree on when weeknumbers start
    static {
        Calendar.getInstance().setFirstDayOfWeek(Calendar.MONDAY);
        Calendar.getInstance().setMinimalDaysInFirstWeek(7);
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

    @SuppressWarnings("unused")
    public WeekResolver withTimeZone(String timezone) {
        this.zoneId = ZoneId.of(timezone);
        return this;
    }

    public WeekResolver withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public WeekResolverResult getWeekCode() throws UnsupportedOperationException {
        return getWeekCode(date);
    }

    /**
     * Calculate the weekcode for the given date depending on the cataloguecode
     *
     * @return a string with the weekcode
     * @throws UnsupportedOperationException if the cataloguecode is not supported
     */
    public WeekResolverResult getWeekCode(LocalDate customDate) throws UnsupportedOperationException {
        LOGGER.info("Calculating weekcode for catalogueCode={} and date={}", catalogueCode, customDate);

        // Get the current date
        LocalDate expectedDate = customDate;

        // Select configuration of weekcode calculation
        if( !codes.containsKey(catalogueCode.toUpperCase()) ){
            throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }
        WeekCodeConfiguration configuration = codes.get(catalogueCode.toUpperCase());

        // If the configuration has a fixed weekcode, return this
        if( configuration.getFixedWeekCode() != null ) {
            return new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate, calculateWeekDescription(configuration, customDate));
        }

        // Algorithm:
        //   step 1: Adjust the shiftday for the current week, since closing days may affect the shiftday
        //   step 2: If on or after shiftday, shift to first day in the next week
        //   step 3: take date and add [0,1,2,...] weeks
        //   step 4: if the final code ends up in the week after Easter, then move forward to next week
        //   step 5: if the date is a closing day then add 1 day untill we reach a working day

        // Step 1: Adjust shiftday, but only if we are not ignoring closing days
        DayOfWeek shiftDay = configuration.getIgnoreClosingDays() || configuration.getShiftDay() == null
                ? configuration.getShiftDay()
                : adjustShiftDay(expectedDate, configuration.getShiftDay(), configuration.getAllowEndOfYear());

        // Step 2: Is this on or after the shiftday ?
        if( shiftDay != null && expectedDate.getDayOfWeek().getValue() >= shiftDay.getValue() ) {
            expectedDate = expectedDate.plusWeeks(1);
            expectedDate = expectedDate.minusDays( expectedDate.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
            LOGGER.info("Date shifted to monday next week due to shiftday to {}", expectedDate);
        }

        // Step 3: add the selected number of weeks
        expectedDate = expectedDate.plusWeeks(configuration.getAddWeeks());
        LOGGER.info("date shifted {} week(s) {}", configuration.getAddWeeks(), expectedDate);

        // Step 4: if the final code ends up in the week after Easter, then move forward to next week
        if( !configuration.getIgnoreClosingDays() && isEasterWeek(expectedDate.minusWeeks(1)) ) {
            expectedDate = expectedDate.plusWeeks(1);
            LOGGER.info("Same weekday the week before is within the Easter week, pushing 1 week to {}", expectedDate);

        }

        // Step 5: Is this a closing day ?
        while( !configuration.getIgnoreClosingDays() && isClosingDay(expectedDate, configuration.getAllowEndOfYear()) ) {
            expectedDate = expectedDate.plusDays(1);
            LOGGER.info("date shifted 1 day due to closing day to {}", expectedDate);
        }

        // Build final result.
        LOGGER.info("Date {} pushed to final date {} with weeknumber {}", customDate, expectedDate, Integer.parseInt(expectedDate.format(DateTimeFormatter.ofPattern("w", locale))));

        // Build final result
        return new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate, calculateWeekDescription(configuration, customDate));
    }

    public WeekResolverResult getCurrentWeekCode() throws UnsupportedOperationException {
        return  getCurrentWeekCode(date);
    }

    /**
     * Calculate the current weekcode for the given date depending on the cataloguecode
     *
     * @return a string with the weekcode
     * @throws UnsupportedOperationException if the cataloguecode is not supported
     */
    public WeekResolverResult getCurrentWeekCode(LocalDate customDate) throws UnsupportedOperationException {
        LOGGER.info("Calculating current weekcode for catalogueCode={} and date={}", catalogueCode, customDate);

        // Get the current date
        LocalDate expectedDate = customDate;

        // Select configuration of weekcode calculation
        if( !codes.containsKey(catalogueCode.toUpperCase()) ){
            throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }
        WeekCodeConfiguration configuration = codes.get(catalogueCode.toUpperCase());

        // If the configuration has a fixed weekcode, return this
        if( configuration.getFixedWeekCode() != null ) {
            return new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate, calculateWeekDescription(configuration, customDate));
        }

        // Algorithm: Do note that we always allow end-of-year, since we just want the
        //            weekcode for a working day, not the forward weekcode that is set
        //            when records is created since the current weekcode is checked against
        //            the release week given by the records weekcode (returned by getWeekCode)
        //   step 1: Do we honor closing days ?, if not then return weekcode for today
        //   step 2: If today is a closing day, then return weekcode for the first not-closed day
        //   step 3: Otherwise check if we are on or before shiftday

        if (configuration.getIgnoreClosingDays()) {
            return new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate, calculateWeekDescription(configuration, customDate));
        }

        if (isClosingDay(expectedDate, true)) {
            // Move forward to next production day, but stop at sunday since we should return the following week, not any later week
            while (isClosingDay(expectedDate, true) && expectedDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                expectedDate = expectedDate.plusDays(1);
            }
        }

        DayOfWeek shiftDay = adjustShiftDay(expectedDate, configuration.getShiftDay(), true);
        if (shiftDay == null || expectedDate.getDayOfWeek().getValue() >= shiftDay.getValue()) {
            return new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate.plusWeeks(1), calculateWeekDescription(configuration, customDate));
        } else {
            return new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate, calculateWeekDescription(configuration, customDate));
        }
    }

    public YearPlanResult getYearPlan(Integer year) {
        YearPlanResult yearPlan = new YearPlanResult();

        // Add headers
        yearPlan.add(new ArrayList<>(WeekDescription.Headers));

        // Find first day of the year. If not a monday, then move backwards to find the last monday in the previous year
        LocalDate currentDate = LocalDate.parse(String.format("%04d-01-01", year), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        while (currentDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            currentDate = currentDate.minusDays(1);
        }

        // Iterate through all mondays and add the description of each week
        do {
            WeekResolverResult result = getCurrentWeekCode(currentDate);
            yearPlan.add(getResultAsRow(result));
            currentDate = currentDate.plusWeeks(1);
        } while (currentDate.getYear() == year);

        return yearPlan;
    }

    private DayOfWeek adjustShiftDay(LocalDate expectedDate, DayOfWeek shiftDay, boolean allowEndOfYear) {

        // Find the date of the shiftday in this week
        LocalDate dateOfShiftDay = expectedDate;
        dateOfShiftDay = dateOfShiftDay.plusDays(shiftDay.getValue() - expectedDate.getDayOfWeek().getValue());
        LOGGER.info("ExpectedDate is {} shiftday is then {}", expectedDate, dateOfShiftDay);

        // SPECIAL CASES:
        // 1: If the expected date falls inside the Easter week, then there is no shiftday
        LOGGER.info("Checking if {} is in the Easter week", expectedDate);
        if( isEasterWeek(expectedDate) ) {
            LOGGER.info("Sunday this week is Easter sunday. No shiftday for this week");
            return null;
        }
        // 2: If the expected date falls in the pentecost week and shiftday is friday, then move
        //    the shiftday back to thursday
        LOGGER.info("Checking if {} is in the pentecost week", expectedDate);
        if( isPentecostWeek(expectedDate) && shiftDay == DayOfWeek.FRIDAY ) {
            LOGGER.info("Sunday this week is pentecost and shiftday is friday. Move shiftday to thursdag");
            return DayOfWeek.THURSDAY;
        }

        //  If the expected date falls in the week before Easter and shiftday is friday, then move shiftday back 1 day
        LOGGER.info("Checking if next sunday {} is Easter sunday and shiftday {} is friday", expectedDate.plusWeeks(1), shiftDay);
        if( shiftDay == DayOfWeek.FRIDAY && isEasterWeek(expectedDate.plusWeeks(1)) ) {
            LOGGER.info("Shiftday is a friday and next week is the Easter week. Shiftday adjusted to THURSDAY");
            return DayOfWeek.THURSDAY;
        }

        // Adjust the shiftday back until it is not a closing day. This may potentially roll
        // back into the last week, but if we reach monday, then the shiftday is in effect no matter what
        // and we will end up adding a week as expected.
        while( isClosingDay(dateOfShiftDay, allowEndOfYear) && dateOfShiftDay.getDayOfWeek() != DayOfWeek.MONDAY ) {
            dateOfShiftDay = dateOfShiftDay.minusDays(1);
            LOGGER.info("Moving shiftday back 1 day to {}", dateOfShiftDay);
        }

        LOGGER.info("Final shiftday is set to {}", dateOfShiftDay.getDayOfWeek());
        return dateOfShiftDay.getDayOfWeek();
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
            DateTimeFormatter weekCodeFormatter = DateTimeFormatter.ofPattern("w", locale).withZone(zoneId);
            if( Integer.parseInt(expectedDate.format(weekCodeFormatter)) == 52 || Integer.parseInt(expectedDate.format(weekCodeFormatter)) == 53 ) {
                LOGGER.info("{} is within week 52 or 53", expectedDate);
                return true;
            }

            // Check for first week in a new year (not 100% consolidated rule, but have been so for several years now)
            if( Integer.parseInt(expectedDate.format(weekCodeFormatter)) == 1) {
                LOGGER.info("{} is first week of the year", expectedDate);
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

        // Check for Easter, pentecost and ascension day
        if( isEasterAndRelatedClosingDay(expectedDate) ) {
            LOGGER.info("{} is within Easter, pentecost or ascension day", expectedDate);
            return true;
        }

        // Not a closing day
        LOGGER.info("{} is not a closing day", expectedDate);
        return false;
    }

    /**
     * Check if the given date is a date within the Easter week
     * @param expectedDate The date to check
     * @return True if the date is within the Easter week
     */
    private boolean isEasterWeek(LocalDate expectedDate) {

        // Get the date of sunday in this week and the Easter sunday
        LocalDate dateOfSunday = getDateOfSunday(expectedDate);
        LocalDate easterSunday = getEasterSunday(dateOfSunday);

        // Check if the expected date is before Maundy Thursday
        if( dateOfSunday.isEqual(easterSunday)) {
            LOGGER.info("{} is in the Easter week", expectedDate);
            return true;
        }

        // Expected date is not somewhere in the Easter period.
        LOGGER.info("{} is not in the Easter week", expectedDate);
        return false;
    }

    /**
     * Check if the given date is a date within the pentecost week
     * @param expectedDate The date to check
     * @return True if the date is within the pentecost week
     */
    private boolean isPentecostWeek(LocalDate expectedDate) {

        // Get the date of sunday in this week and Easter sunday
        LocalDate dateOfSunday = getDateOfSunday(expectedDate);
        LocalDate easterSunday = getEasterSunday(dateOfSunday);

        // Pentecost. 7. sunday after Easter sunday
        LocalDate pentecost =  easterSunday.plusWeeks(7);
        while( pentecost.getDayOfWeek() != DayOfWeek.SUNDAY ) {
            pentecost = pentecost.plusDays(1);
        }
        LOGGER.info("Pentecost is {}", pentecost);
        if( dateOfSunday.isEqual(pentecost) ) {
            LOGGER.info("{} is ", dateOfSunday);
            return true;
        }

        // Expected date is not somewhere in the pentecost period.
        LOGGER.info("{} is not in the pentecost week", expectedDate);
        return false;
    }

    /**
     * Check if the given date is a date within the Easter or related closing days
     * (pentecost, ascension Day)
     * @param expectedDate The date to check
     * @return True if the date is within the Easter, pentecost or ascension days, otherwise false
     */
    private boolean isEasterAndRelatedClosingDay(LocalDate expectedDate) {

        // Locate Easter sunday for current year
        Optional<LocalDate> optionalSunday = EASTER_SUNDAYS.stream().filter(x -> x.getYear() == expectedDate.getYear()).findFirst();
        if(optionalSunday.isEmpty()) {
            LOGGER.warn("Request for date in the far-off past or future, date will not be checked for Easter");
            return false;
        }
        LocalDate easterSunday = optionalSunday.get();
        LOGGER.info("Easter sunday for {} is {}", expectedDate.getYear(), easterSunday);

        // Check if the expected date is before Maundy Thursday
        if( expectedDate.isAfter(easterSunday.minusDays(4)) && expectedDate.isBefore(easterSunday.plusDays(2)) ) {
            LOGGER.info("{} is within Easter", expectedDate);
            return true;
        }

        // Pentecost. 7. sunday after Easter sunday
        LocalDate pentecost =  easterSunday.plusWeeks(7);
        while( pentecost.getDayOfWeek() != DayOfWeek.SUNDAY ) {
            pentecost = pentecost.plusDays(1);
        }
        if( expectedDate.isEqual(pentecost.plusDays(1)) ) { // Check for withsun
            LOGGER.info("{} is withsun", expectedDate);
            return true;
        }

        // Ascension day. 6.th thursday after maundy thursday. Check also for pinched friday
        LocalDate ascensionDay = easterSunday.minusDays(3).plusWeeks(6);
        if( expectedDate.isEqual(ascensionDay) ) {
            LOGGER.info("{} is ascension day", expectedDate);
            return true;
        }
        if( expectedDate.isEqual(ascensionDay.plusDays(1)) ) {
            LOGGER.info("{} is pinched friday after ascension day", expectedDate);
            return true;
        }

        // Check for 'store bededag', tounge-in-cheek english name 'prayers day'
        // Todo: Remove or disable this after 'store bededag' 2023 since it has been cancelled from 2024
        LocalDate prayersDay = easterSunday.plusDays(26);
        if( expectedDate.isEqual(prayersDay) ) {
            LOGGER.info("{} is prayers day ('store bededag')", expectedDate);
            return true;
        }
        if( expectedDate.isEqual(prayersDay.plusDays(1)) && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
            LOGGER.info("{} is pinched friday after prayers day ('store bededag')", expectedDate);
            return true;
        }

        // Expected date is not somewhere in the Easter period or on any related closing days
        LOGGER.info("{} is not Easter, pentecost or ascension day", expectedDate);
        return false;
    }

    private LocalDate getEasterSunday(LocalDate dateOfSunday) {

        // Locate Easter sunday for current year
        Optional<LocalDate> optionalSunday = EASTER_SUNDAYS.stream().filter(x -> x.getYear() == dateOfSunday.getYear()).findFirst();
        if(optionalSunday.isEmpty()) {
            LOGGER.warn("Date {} is too far-off into the past", dateOfSunday.toString());
            throw new BadRequestException(String.format("Date %s is too far-off into the past", dateOfSunday.toString()));
        }
        LocalDate easterSunday = optionalSunday.get();
        LOGGER.info("Easter sunday for {} is {}", dateOfSunday.getYear(), easterSunday);
        return easterSunday;
    }

    private LocalDate getDateOfSunday(LocalDate expectedDate) {
        LocalDate dateOfSunday = expectedDate.plusDays(DayOfWeek.SUNDAY.getValue() - expectedDate.getDayOfWeek().getValue());
        LOGGER.info("Sunday in this week is {}", dateOfSunday);
        return dateOfSunday;
    }

    private WeekDescription calculateWeekDescription(WeekCodeConfiguration configuration, LocalDate date) {
        WeekDescription description = new WeekDescription();

        // Find monday in this week since all calculations of dates is done from that day onwards
        LocalDate monday = date.minusDays(date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());

        // Short version of weekcode (only digits).
        description.setWeekCodeShort(String.format("%04d", getYear(date)) +
                String.format("%02d", configuration.getUseMonthNumber() ? getMonth(date) : getWeekNumber(date)));

        // First day this weekcode is assigned
        LocalDate start = monday;
        while (isClosingDay(start, configuration.getAllowEndOfYear())) {
            start = start.plusDays(1);
        }
        description.setWeekCodeFirst(fromLocalDate(start));

        // Day the new weekcode is first assigned
        if (configuration.getShiftDay() != null ) {
            DayOfWeek shiftDay = adjustShiftDay(date, configuration.getShiftDay(), configuration.getAllowEndOfYear());
            if (shiftDay != null) {
                if (shiftDay == DayOfWeek.MONDAY) {
                    description.setNoProduction(true);
                }
                description.setShiftDay(fromLocalDate(monday.plusDays(shiftDay.getValue() - DayOfWeek.MONDAY.getValue())));
            } else {
                description.setNoProduction(true);
                description.setShiftDay(null);
            }
        } else {
            description.setShiftDay(fromLocalDate(monday.plusDays(6)));
        }

        // Last day this weekcode is assigned
        if (description.getShiftDay() != null) {
            description.setWeekCodeLast(fromLocalDate(fromDate(description.getShiftDay()).minusDays(1)));
        }

        // Book cart (monday)
        description.setBookCart(fromLocalDate(monday));

        // Proof (tuesday)
        description.setProof(fromLocalDate(monday.plusDays(1)));

        // BKM-red. (wednesday)
        description.setBkm(fromLocalDate(monday.plusDays(2)));

        // Proof can start at.. (monday 15.00)
        description.setProofFrom(fromLocalDate(monday));

        // End of proof (tuesday 17.00)
        description.setProofTo(fromLocalDate(monday.plusDays(1)));

        // Publish date (friday)
        description.setPublish(fromLocalDate(monday.plusDays(4)));

        return description;
    }

    private Date fromLocalDate(LocalDate date) {
        return Date.from(date.plusDays(6).atStartOfDay(zoneId).toInstant());
    }

    private LocalDate fromDate(Date date) {
        return LocalDate.ofInstant(date.toInstant(), zoneId);
    }

    public int getYear(LocalDate date) {

        // Format MUST be 'week year' (upper case 'YYYY'), NOT 'year' (lower case 'yyyy')
        int year = Integer.parseInt(date.format(DateTimeFormatter.ofPattern("YYYY", locale)));

        // Adjust for week 53
        if (getWeekNumber(date) == 53) {
            year--;
        }

        return year;
    }

    public int getMonth(LocalDate date) {
        return Integer.parseInt(date.format(DateTimeFormatter.ofPattern("MM", locale)));
    }

    public int getWeekNumber(LocalDate date) {
        int weekNumber = Integer.parseInt(date.format(DateTimeFormatter.ofPattern("w", locale).withZone(zoneId)));

        // Adjust for week 53
        if (weekNumber == 1 && date.getMonth() == Month.DECEMBER) {
            weekNumber = 53;
        }

        return weekNumber;
    }

    public List<String> getResultAsRow(WeekResolverResult result) {

        // Todo: Check if we need a prefix for this week
        String specialCasePrefix = "";

        return List.of(result.getDescription().getWeekCodeShort(),
                rowStringFromRowDate(specialCasePrefix, result.getDescription().getWeekCodeFirst()),
                rowStringFromRowDate(specialCasePrefix, result.getDescription().getWeekCodeLast()),
                rowStringFromRowDate(specialCasePrefix, result.getDescription().getShiftDay()),
                rowStringFromRowDate(specialCasePrefix, result.getDescription().getBookCart()),
                rowStringFromRowDate(result.getDescription().getProof()),
                rowStringFromRowDate(result.getDescription().getBkm()),
                rowStringFromRowDate(result.getDescription().getProofFrom()),
                rowStringFromRowDate(result.getDescription().getProofTo()),
                rowStringFromRowDate(result.getDescription().getPublish()));
    }

    private String rowStringFromRowDate(Date date) {
        return rowStringFromRowDate("", date);
    }

    private String rowStringFromRowDate(String prefix, Date date) {
        if (date == null) {
            return "\"\"";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = fromDate(date);
        return "\"" + prefix + localDate.format(formatter) + "\"";
    }
}
