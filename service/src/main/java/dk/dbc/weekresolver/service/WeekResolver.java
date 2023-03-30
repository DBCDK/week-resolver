package dk.dbc.weekresolver.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.time.format.TextStyle;
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
            return new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate,
                    calculateWeekDescription(configuration, customDate, configuration.getFixedWeekCode()));
        }

        LOGGER.info("======================== BEGIN SHIFTDAY CALCULATION ==================================");
        if (!configuration.getIgnoreClosingDays()) {

            // Step 1: Adjust shiftday, but only if we are not ignoring closing days
            DayOfWeek shiftDay = configuration.getShiftDay() == null
                    ? configuration.getShiftDay()
                    : adjustShiftDay(expectedDate, configuration.getShiftDay(), configuration.getAllowEndOfYear());

            // Step 2: Is this on or after the shiftday ?
            if (shiftDay == null || expectedDate.getDayOfWeek().getValue() >= shiftDay.getValue()) {
                expectedDate = getMonday(expectedDate.plusWeeks(1));
                LOGGER.info("Date shifted to monday next week due to shiftday to {}", expectedDate);

                LOGGER.info("Checking for closing days");
                while (!configuration.getIgnoreClosingDays() && (isClosingDay(expectedDate, configuration.getAllowEndOfYear()) || isEasterWeek(expectedDate))) {
                    expectedDate = expectedDate.plusDays(1);
                    LOGGER.info("Date shifted 1 more day due to closing day, easter week or first week of the year to {}", expectedDate);
                }
            }
        } else {
            LOGGER.info("Ignoring shiftday for this configuration. Date remains at {}", expectedDate);
        }
        LOGGER.info("======================== END SHIFTDAY CALCULATION ==================================");

        // Step 3: If the week after the one we are currently in, are totally closed (christmas, easter),
        //         then there is no way to handle BKM. and proof, so shift forward
        LOGGER.info("======================== BEGIN WEEKCODE CALCULATION ==================================");
        while (isWithinClosingWeek(expectedDate.plusWeeks(1), configuration.getAllowEndOfYear()) || isEasterWeek(expectedDate.plusWeeks(1))) {
            expectedDate = getMonday(expectedDate.plusWeeks(1));
            LOGGER.info("Date shifted to monday next week due to date within a closed week to {}", expectedDate);
        }

        // Step 4: add the selected number of weeks
        expectedDate = expectedDate.plusWeeks(configuration.getAddWeeks());
        LOGGER.info("date shifted {} week(s) {}", configuration.getAddWeeks(), expectedDate);
        LOGGER.info("======================== END WEEKCODE CALCULATION ==================================");

        // Build final result
        LOGGER.info("Date {} pushed to final date {} with weeknumber {}", customDate, expectedDate,
                Integer.parseInt(expectedDate.format(DateTimeFormatter.ofPattern("w", locale))));
        WeekResolverResult result = new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate);
        LOGGER.info("======================== BEGIN DESCRIPTION CALCULATION ==================================");
        result.setDescription(calculateWeekDescription(configuration, customDate, result.getWeekCode()));
        LOGGER.info("======================== END DESCRIPTION CALCULATION ==================================");
        return result;
    }

    private Boolean isFirstWeekOfYear(LocalDate date) {
        DateTimeFormatter weekCodeFormatter = DateTimeFormatter.ofPattern("w", locale).withZone(zoneId);

        if (Integer.parseInt(date.format(weekCodeFormatter)) == 1) {
            LOGGER.info("{} is within week 01, which has no production release", date);
            return true;
        }

        return false;
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
            return new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate,
                    calculateWeekDescription(configuration, customDate, configuration.getFixedWeekCode()));
        }

        // Algorithm: Do note that we always allow end-of-year, since we just want the
        //            weekcode for a working day, not the forward weekcode that is set
        //            when records is created since the current weekcode is checked against
        //            the release week given by the records weekcode (returned by getWeekCode)
        //   step 1: Do we honor closing days ?, if not then return weekcode for today
        //   step 2: If today is a closing day, then return weekcode for the first not-closed day
        //   step 3: Otherwise check if we are on or before shiftday

        if (!configuration.getIgnoreClosingDays()) {
            if (isClosingDay(expectedDate, true)) {
                // Move forward to next production day, but stop at sunday since we should return the following week, not any later week
                while (isClosingDay(expectedDate, true) && expectedDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    expectedDate = expectedDate.plusDays(1);
                }
            }

            // Check if we have passed the shiftday
            DayOfWeek shiftDay = adjustShiftDay(expectedDate, configuration.getShiftDay(), true);
            if (shiftDay == null || expectedDate.getDayOfWeek().getValue() >= shiftDay.getValue()) {
                expectedDate = expectedDate.plusWeeks(1);
            }
        }

        // Build result
        WeekResolverResult result = new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate);
        result.setDescription(calculateWeekDescription(configuration, customDate, result.getWeekCode()));
        return result;
    }

    public YearPlanResult getYearPlan(Integer year, Boolean showAbnormalDayNames) {
        YearPlanResult yearPlan = new YearPlanResult().withYear(String.format("%04d", year));

        // Add headers
        yearPlan.add(new ArrayList<>(getHeadersAsRow()));

        // Find first day of the year. If not a monday, then move backwards to find the second-last monday in the previous year
        LocalDate currentDate = LocalDate.parse(String.format("%04d-01-01", year), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        while (currentDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            currentDate = currentDate.minusDays(1);
        }
        currentDate = currentDate.minusWeeks(1);

        // Iterate through all mondays and get the description of each week
        DateTimeFormatter weekCodeFormatter = DateTimeFormatter.ofPattern("w", locale).withZone(zoneId);
        List<WeekResolverResult> results = new ArrayList<>();
        do {
            WeekResolverResult result = getWeekCode(currentDate);
            results.add(result);
            currentDate = currentDate.plusWeeks(1);
        } while (currentDate.getYear() <= year || (currentDate.getYear() == (year + 1) && Integer.parseInt(currentDate.format(weekCodeFormatter)) < 2));

        // Add rows with week descriptions. Check if we can merge some rows (typical the first/last weeks)
        WeekResolverResult previousResult = results.get(0);
        for (int i = 1; i < results.size(); i++) {
            WeekResolverResult currentResult = results.get(i);

            if (previousResult.getWeekCode().equals(currentResult.getWeekCode()) && currentResult.getDescription().getWeekCodeFirst() != null) {
                currentResult.getDescription().setWeekCodeFirst(previousResult.getDescription().getWeekCodeFirst());
                currentResult.getDescription().setWeekNumber(previousResult.getDescription().getWeekNumber() + " + " + currentResult.getDescription().getWeekNumber());
                if (yearPlan.size() > 1) {
                    yearPlan.getRows().remove(yearPlan.size() - 1);
                }
            }
            yearPlan.add(getResultAsRow(currentResult, showAbnormalDayNames));

            previousResult = currentResult;
        };

        // Remove the
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
            LOGGER.info("Sunday this week is pentecost and shiftday is friday. Move shiftday to thursday");
            return DayOfWeek.THURSDAY;
        }
        // 3: If the expected date falls in the week before may 1st. or "Grundlovsdag", then move
        //    the shiftday back to thursday
        if (isWeekBeforeMayFirst(expectedDate) || isWeekBeforeConstitutionDay(expectedDate)) {
            LOGGER.info("Sunday this week is the week before may 1st. or Grundlovsdag. Move shiftday to thursday");
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

        // Check for week 52 and 53, which is never used!
        if( allowEndOfYearWeeks ) {
            // New years eve
            if (expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 31) {
                LOGGER.info("{} is new years eve", expectedDate);
                return true;
            }
        } else {
            // Check for week 52 and 53
            DateTimeFormatter weekCodeFormatter = DateTimeFormatter.ofPattern("w", locale).withZone(zoneId);

            if (List.of(52, 53).contains(Integer.parseInt(expectedDate.format(weekCodeFormatter)))) {
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

        // Check for Easter, pentecost and ascension day
        if( isEasterAndRelatedClosingDay(expectedDate) ) {
            LOGGER.info("{} is within Easter, pentecost or ascension day", expectedDate);
            return true;
        }

        // Not a closing day
        LOGGER.info("{} is not a closing day", expectedDate);
        return false;
    }

    private Boolean isWithinClosingWeek(LocalDate date, boolean allowEndOfYearWeeks) {
        LocalDate currentDate = getMonday(date);
        while (currentDate.getDayOfWeek().getValue() < DayOfWeek.SATURDAY.getValue()) {
            if (!isClosingDay(currentDate, allowEndOfYearWeeks)) {
                return false;
            }
            currentDate = currentDate.plusDays(1);
        }
        return true;
    }

    private LocalDate getMonday(LocalDate date) {
        return date.minusDays( date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
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

    private boolean isWeekBeforeMayFirst(LocalDate expectedDate) {

        // Get the date of sunday in this week and Easter sunday
        LocalDate dateOfSunday = getDateOfSunday(expectedDate);

        if (dateOfSunday.getMonth() == Month.APRIL && dateOfSunday.getDayOfMonth() == 30) {
            LOGGER.info("{} is the day before may 1st.", dateOfSunday);
            return true;
        } else {
            LOGGER.info("{} is not the day before may 1st.", dateOfSunday);
            return false;
        }
    }

    // Ahem.. "Constitution Day" is not the most correct translation of "Grundlovsday", but anywho.. :)
    private boolean isWeekBeforeConstitutionDay(LocalDate expectedDate) {

        // Get the date of sunday in this week and Easter sunday
        LocalDate dateOfSunday = getDateOfSunday(expectedDate);

        if (dateOfSunday.getMonth() == Month.JUNE && dateOfSunday.getDayOfMonth() == 4) {
            LOGGER.info("{} is the day before Grundlovsdag", dateOfSunday);
            return true;
        } else {
            LOGGER.info("{} is not the day before Grundlovsdag", dateOfSunday);
            return false;
        }
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
        if (expectedDate.getYear() < 2024) {
            LocalDate prayersDay = easterSunday.plusDays(26);
            if (expectedDate.isEqual(prayersDay)) {
                LOGGER.info("{} is prayers day ('store bededag')", expectedDate);
                return true;
            }
            if (expectedDate.isEqual(prayersDay.plusDays(1)) && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
                LOGGER.info("{} is pinched friday after prayers day ('store bededag')", expectedDate);
                return true;
            }
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

    private WeekDescription calculateWeekDescription(WeekCodeConfiguration configuration, LocalDate date, String weekcode) {
        WeekDescription description = new WeekDescription();

        // Find monday in this week since all calculations of dates is done from that day
        LocalDate monday = date.minusDays(date.getDayOfWeek().getValue() - 1);

        // Set weeknumber of actual week
        description.setWeekNumber(monday.format(DateTimeFormatter.ofPattern("w", locale).withZone(zoneId)));

        // BKM code
        description.setWeekCodeShort(weekcode.substring(3));

        // Shiftday. The shiftday indicates if we have a week without production
        if (configuration.getShiftDay() != null ) {
            DayOfWeek shiftDay = adjustShiftDay(date, configuration.getShiftDay(), configuration.getAllowEndOfYear());
            if (shiftDay != null) {
                if (shiftDay == DayOfWeek.MONDAY) {
                    // When the shiftday is monday, it means that the shiftday could not be shifted
                    // any more backwards, which indicates that we have a week without production
                    description.setNoProduction(true);
                } else {
                    description.setShiftDay(fromLocalDate(monday.plusDays(shiftDay.getValue() - 1)));
                }
            } else {
                description.setNoProduction(true);
            }
        } else {
            description.setNoProduction(true);
        }

        // If week of no production, then leave other fields than the starting date blank
        // (starting date must be set to be able to merge with the next row when presenting the data)
        if (description.getNoProduction()) {
            if (configuration.getShiftDay() != null) {
                LocalDate previousShiftDate = monday;

                // Move back week after week until we reach a week when we have a shiftday
                DayOfWeek previousShiftDay;
                do {
                    previousShiftDate = previousShiftDate.minusWeeks(1);
                    previousShiftDay = adjustShiftDay(previousShiftDate, configuration.getShiftDay(), configuration.getAllowEndOfYear());
                } while (previousShiftDay == null || previousShiftDay == DayOfWeek.MONDAY);

                // Then adjust the date to the day before shiftday
                previousShiftDate = previousShiftDate.plusDays(previousShiftDay.getValue() - 1);
                description.setWeekCodeFirst(fromLocalDate(previousShiftDate));
            }

            return description;
        }

        // The first assignment of this weekcode, is the shiftday in the week before
        LocalDate previousMonday = monday.minusWeeks(1);
        DayOfWeek previousShiftDay = adjustShiftDay(previousMonday, configuration.getShiftDay(), configuration.getAllowEndOfYear());
        while (previousShiftDay == null || previousShiftDay == DayOfWeek.MONDAY) {
            previousMonday = previousMonday.minusWeeks(1);
            previousShiftDay = adjustShiftDay(previousMonday, configuration.getShiftDay(), configuration.getAllowEndOfYear());
        }
        description.setWeekCodeFirst(fromLocalDate(previousMonday.plusDays(previousShiftDay.getValue() - 1)));

        // Last day this weekcode is assigned
        if (description.getShiftDay() != null) {
            description.setWeekCodeLast(fromLocalDate(fromDate(description.getShiftDay()).minusDays(1)));
        }

        // Book cart the next working day after shiftday
        LocalDate shiftDay = fromDate(description.getShiftDay());
        do {
            shiftDay = shiftDay.plusDays(1);
        } while (isClosingDay(shiftDay, configuration.getAllowEndOfYear()));
        description.setBookCart(fromLocalDate(shiftDay));

        // Proof. Tuesday in the next week. (No adjustments)
        description.setProof(fromLocalDate(monday.plusDays(8)));

        // BKM-red. Wednesday in the next week. (No adjustments)
        description.setBkm(fromLocalDate(monday.plusDays(9)));

        // Proof can start at the day before tuesday in the next week, at 15.00. (No adjustments)
        description.setProofFrom(fromLocalDate(monday.plusDays(7)));

        // Proof must be completed by tuesday in the next week at 17.00. (No adjustments)
        description.setProofTo(fromLocalDate(monday.plusDays(8)));

        // Publish date. Friday in the next week. (No adjustments)
        description.setPublish(fromLocalDate(monday.plusDays(11)));

        return description;
    }

    public Date fromLocalDate(LocalDate date) {
        return Date.from(date.atStartOfDay(zoneId).toInstant());
    }

    public LocalDate fromDate(Date date) {
        return LocalDate.ofInstant(date.toInstant(), zoneId);
    }

    private List<String> getHeadersAsRow() {
        // Make sure that results are returned in this order by getResultAsRow()

        return List.of(
                "Katalogkode",
                "DBCKat ugekode start",
                "DBCKat ugekode slut",
                "DBCKat ugeafslutning",
                "Bogvogn",
                "Ugekorrekturen køres",
                "Ugekorrektur",
                "Slutredaktion (ugekorrektur)",
                "BKM-red.",
                "Udgivelsesdato",
                "Ugenummber"
        );
    }

    private List<String> getResultAsRow(WeekResolverResult result, Boolean showAbnormalDayNames) {
        // Make sure that headers are returned in this order by getHeadersAsRow()

        if (result.getDescription().getNoProduction()) {
            return List.of(result.getDescription().getWeekCodeShort(), "", "", "", "", "", "", "", "", "",
                    result.getDescription().getWeekNumber());
        }

        return List.of(result.getDescription().getWeekCodeShort(),
                stringFromDate(isSpecialDay(result.getDescription().getWeekCodeFirst(), DayOfWeek.FRIDAY, showAbnormalDayNames),
                        result.getDescription().getWeekCodeFirst(), true),
                stringFromDate(isSpecialDay(result.getDescription().getWeekCodeLast(), DayOfWeek.THURSDAY, showAbnormalDayNames),
                        result.getDescription().getWeekCodeLast(), true),
                stringFromDate(isSpecialDay(result.getDescription().getShiftDay(), DayOfWeek.FRIDAY, showAbnormalDayNames),
                        result.getDescription().getShiftDay(), true),
                stringFromDate(isSpecialDay(result.getDescription().getBookCart(), DayOfWeek.MONDAY, showAbnormalDayNames),
                        result.getDescription().getBookCart(), true),
                stringFromDate(result.getDescription().getProofFrom(), true),
                stringFromDate(result.getDescription().getProof(), true),
                stringFromDate(result.getDescription().getProofTo(), true),
                stringFromDate(result.getDescription().getBkm(), true),
                stringFromDate(result.getDescription().getPublish(), true),
                result.getDescription().getWeekNumber());
    }

    private String isSpecialDay(Date date, DayOfWeek expectedDayOfWeek, Boolean showAbnormalDayNames) {
        if (date == null) {
            return "--";
        }
        LocalDate actualDayOfWeek = fromDate(date);
        if (actualDayOfWeek.getDayOfWeek() != expectedDayOfWeek) {
            if (showAbnormalDayNames) {
                return actualDayOfWeek.getDayOfWeek().getDisplayName(TextStyle.FULL, locale).toUpperCase() + "   ";
            } else {
                return "* ";
            }
        } else {
            return "";
        }
    }

    public String stringFromDate(Date date) {
        return stringFromDate(date, false);
    }

    private String stringFromDate(Date date, boolean quoted) {
        return stringFromDate("", date, quoted);
    }

    private String stringFromDate(String prefix, Date date, boolean quoted) {
        if (date == null) {
            return "\"\"";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", locale);
        LocalDate localDate = fromDate(date);
        return (quoted ? "\"" : "") + prefix + localDate.format(formatter) + (quoted ? "\"" : "");
    }

    public LocalDate fromString(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
