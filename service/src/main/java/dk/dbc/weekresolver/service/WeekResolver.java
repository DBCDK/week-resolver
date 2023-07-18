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

    public static final HashMap<String, WeekCodeConfiguration> CODES = new HashMap<>();
    static {
        // No shiftday, no added weeks, allowing end-of-year and closingdays
        CODES.put("ACC", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays()); // DMatV2
        CODES.put("ACE", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays()); // DMatV2
        CODES.put("ACF", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        CODES.put("ACK", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        CODES.put("ACM", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        CODES.put("ACN", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        CODES.put("ACP", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());
        CODES.put("ACT", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays()); // DMatV2
        CODES.put("BLG", new WeekCodeConfiguration().allowEndOfYear().ignoreClosingDays());

        // Shiftday friday, add 1 week
        CODES.put("BKX", new WeekCodeConfiguration().addWeeks(1).withShiftDay(DayOfWeek.FRIDAY));

        // Shiftday friday, add 3 weeks, allow end-of-year
        CODES.put("DPF", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear()); // DataIO (automarc)
        CODES.put("FPF", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear());
        CODES.put("GPF", new WeekCodeConfiguration().addWeeks(3).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear()); // DataIO (periodicJobs)

        // Shiftday friday, add 1 week, allow end-of-year and ignore closing days
        CODES.put("EMO", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear().ignoreClosingDays());
        CODES.put("EMS", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear().ignoreClosingDays());
        CODES.put("EMM", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear().ignoreClosingDays());
        CODES.put("EMK", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear().ignoreClosingDays());

        // Shiftday friday, add 2 weeks, allow end-of-year and ignore closing days
        CODES.put("LIT", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY).allowEndOfYear().ignoreClosingDays());

        // No shiftday , add 1 week
        CODES.put("DAN", new WeekCodeConfiguration().addWeeks(1));
        CODES.put("DAR", new WeekCodeConfiguration().addWeeks(1));
        CODES.put("KBA", new WeekCodeConfiguration().addWeeks(1));
        CODES.put("SBA", new WeekCodeConfiguration().addWeeks(1));
        CODES.put("ABU", new WeekCodeConfiguration().addWeeks(1)); // Deprecated 201834, but apparently still used by dbckat ??

        // No shiftday, add 1 week, allowing end-of-year and closingdays
        CODES.put("ARK", new WeekCodeConfiguration().addWeeks(1).allowEndOfYear().ignoreClosingDays());

        // No shiftday, add 2 weeks, allowing end-of-year and closingdays
        CODES.put("VPT", new WeekCodeConfiguration().addWeeks(2).allowEndOfYear().ignoreClosingDays());

        // Shiftday friday, add 1 week
        CODES.put("UTI", new WeekCodeConfiguration().addWeeks(1).withShiftDay(DayOfWeek.FRIDAY));

        // Shiftday friday, add 2 weeks
        CODES.put("DBR", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        CODES.put("DLR", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        CODES.put("DBF", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        CODES.put("DLF", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        CODES.put("DBI", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        CODES.put("FSB", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        CODES.put("BKM", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DataIO, DMatV2
        CODES.put("DMO", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        CODES.put("FSC", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        CODES.put("IDU", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        CODES.put("SNE", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        CODES.put("LEK", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        CODES.put("MMV", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        CODES.put("FIV", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));
        CODES.put("ERA", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        CODES.put("ERE", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        CODES.put("NLL", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        CODES.put("NLY", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        CODES.put("ERL", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY)); // DMatV2
        CODES.put("FLX", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));

        // Shiftday friday, add 1 week. Will be modified when the record is being edited
        CODES.put("BKR", new WeekCodeConfiguration().addWeeks(1).withShiftDay(DayOfWeek.FRIDAY));

        // Greenland
        CODES.put("GBF", new WeekCodeConfiguration().addWeeks(2).withShiftDay(DayOfWeek.FRIDAY));

        // Use default, most often used, value DIS197605. This may have to be corrected if the record
        // is indeed an old unowned KB record or the like
        CODES.put("DIS", new WeekCodeConfiguration().withFixedWeekCode("197605"));

        // Use code for incomplete record. Finished record may become 197604 or 197607
        CODES.put("OPR", new WeekCodeConfiguration().withFixedWeekCode("197601"));

        // Use the month number instead of the week number
        CODES.put("PLA", new WeekCodeConfiguration().useMonthNumber().withShiftDay(DayOfWeek.FRIDAY));
        CODES.put("PLN", new WeekCodeConfiguration().useMonthNumber().withShiftDay(DayOfWeek.FRIDAY));

        // Use fixed codes for these cataloguecodes
        CODES.put("DBT", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        CODES.put("SDT", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        CODES.put("DIG", new WeekCodeConfiguration().withFixedWeekCode("198507"));
        CODES.put("FFK", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        CODES.put("FSF", new WeekCodeConfiguration().withFixedWeekCode("999999"));
        CODES.put("HOB", new WeekCodeConfiguration().withFixedWeekCode("197300"));
    }

    // Make sure we all agree on when week numbers start
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
        LOGGER.debug("Calculating weekcode for catalogueCode={} and date={}", catalogueCode, customDate);

        // Get the current date
        LocalDate expectedDate = customDate;

        // Select configuration of weekcode calculation
        if( !CODES.containsKey(catalogueCode.toUpperCase()) ){
            throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }
        WeekCodeConfiguration configuration = CODES.get(catalogueCode.toUpperCase());

        // If the configuration has a fixed weekcode, return this
        if( configuration.getFixedWeekCode() != null ) {
            return new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate,
                    calculateWeekDescription(configuration, customDate, configuration.getFixedWeekCode()));
        }

        LOGGER.debug("======================== BEGIN SHIFTDAY CALCULATION ==================================");
        if (!configuration.getIgnoreClosingDays()) {

            // Step 1: Adjust shiftday, but only if we are not ignoring closing days and/or shiftday
            if (configuration.getShiftDay() == null) {
                LOGGER.debug("No shiftday for this code, date unchanged at {}", expectedDate);
            } else {

                // Adjust shiftday
                DayOfWeek shiftDay = adjustShiftDay(expectedDate, configuration.getShiftDay(), configuration.getAllowEndOfYear());

                // Step 2: Is this on or after the shiftday ?
                if (shiftDay == null || expectedDate.getDayOfWeek().getValue() >= shiftDay.getValue()) {
                    expectedDate = getMonday(expectedDate.plusWeeks(1));
                    LOGGER.debug("Date shifted to monday next week due to shiftday {} to {}", shiftDay, expectedDate);

                    LOGGER.debug("Checking for closing days");
                    while (!configuration.getIgnoreClosingDays() && (isClosingDay(expectedDate, configuration.getAllowEndOfYear()) || isEasterWeek(expectedDate))) {
                        expectedDate = expectedDate.plusDays(1);
                        LOGGER.debug("Date shifted 1 more day due to closing day, easter week or first week of the year to {}", expectedDate);
                    }
                }
            }
        } else {
            if (configuration.getShiftDay() != null) {
                LOGGER.debug("Ignoring closing days but has a shiftday, date is {}", expectedDate);
                if (expectedDate.getDayOfWeek().getValue() >= configuration.getShiftDay().getValue()) {
                    expectedDate = getMonday(expectedDate.plusWeeks(1));
                    LOGGER.debug("Date shifted to monday next week due to shiftday {} to {}", configuration.getShiftDay(), expectedDate);
                } else {
                    LOGGER.debug("Date is before shiftday. Date remains at {}", expectedDate);
                }
            } else  {
                LOGGER.debug("No shiftday for this configuration. Date remains at {}", expectedDate);
            }
        }
        LOGGER.debug("======================== END SHIFTDAY CALCULATION ==================================");

        // Step 3: If the week after the one we are currently in, are totally closed (christmas, easter),
        //         then there is no way to handle BKM. and proof, so shift forward
        LOGGER.debug("======================== BEGIN WEEKCODE CALCULATION ==================================");
        while (isWithinClosingWeek(expectedDate.plusWeeks(1), configuration.getAllowEndOfYear()) || isEasterWeek(expectedDate.plusWeeks(1))) {
            expectedDate = getMonday(expectedDate.plusWeeks(1));
            LOGGER.debug("Date shifted to monday next week due to date within a closed week to {}", expectedDate);
        }

        // Step 4: add the selected number of weeks
        expectedDate = expectedDate.plusWeeks(configuration.getAddWeeks());
        LOGGER.debug("date shifted {} week(s) {}", configuration.getAddWeeks(), expectedDate);

        // Step 5: Check that bkm-red. and publish does not collide, if so, then push another week (example is end of 2024)
        //         We do this by checking that the week before the selected day has at least enough days
        //         so that proof and BKM-red can be finished by thursday - so we need 3 working days in the previous
        //         week to make sure this is fulfilled.
        //         The check is ignored if 'ignoreClosingDays' is set
        if (!configuration.getIgnoreClosingDays() && previousWeekIsTooShort(expectedDate, 4)) {
            expectedDate = getMonday(expectedDate.plusWeeks(1));
            LOGGER.debug("Date shifted to monday next week due to previous week having too few working days to {}", expectedDate);
        }

        // Step 6: Never land in week 01, unless allowed by configuration
        while (isWeek(expectedDate, 1) && !configuration.getAllowEndOfYear()) {
            expectedDate = expectedDate.plusDays(1);
            LOGGER.debug("Date shifted 1 day due to date in week 1 to {}", expectedDate);
        }
        LOGGER.debug("======================== END WEEKCODE CALCULATION ==================================");

        // Build final result
        LOGGER.debug("Date {} pushed to final date {} with weeknumber {}", customDate, expectedDate,
                Integer.parseInt(expectedDate.format(DateTimeFormatter.ofPattern("w", locale))));
        WeekResolverResult result = new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate);
        LOGGER.debug("======================== BEGIN DESCRIPTION CALCULATION ==================================");
        result.setDescription(calculateWeekDescription(configuration, customDate, result.getWeekCode()));
        LOGGER.debug("======================== END DESCRIPTION CALCULATION ==================================");
        return result;
    }

    private Boolean isWeek(LocalDate date, int week) {
        DateTimeFormatter weekCodeFormatter = DateTimeFormatter.ofPattern("w", locale).withZone(zoneId);
        if (Integer.parseInt(date.format(weekCodeFormatter)) == week ) {
            LOGGER.debug("{} is in week {}", date, week);
            return true;
        }
        LOGGER.debug("{} is NOT in week {}", date, week);
        return false;
    }

    private int numberOfWorkingDaysInWeekOf(LocalDate date) {
        LocalDate workingDay = getMonday(date);
        LOGGER.debug("Checking number of working days in week of {}", date);

        int numWorkingDays = 0;
        do {
            LOGGER.debug("Checking for working day {}", workingDay);
            if (!isClosingDay(workingDay, true)) { // also check working days in christmas weeks
                LOGGER.debug("{} could be a working day", workingDay);
                numWorkingDays++;
            }
            workingDay = workingDay.plusDays(1);
        } while(workingDay.getDayOfWeek().getValue() > DayOfWeek.MONDAY.getValue());

        LOGGER.debug("week of {} has {} working days", date, numWorkingDays);
        return numWorkingDays;
    }

    private Boolean previousWeekIsTooShort(LocalDate date, int required) {
        LocalDate previousDate = getMonday(date).minusDays(1);
        LOGGER.debug("Checking for short week from {}", previousDate);

        // Short weeks is only a problem around the year change, Easter is handled differently
        // since a whole week disappears.
        if (previousDate.getMonth() != Month.JANUARY) {
            LOGGER.debug("Not a date within january, so no check for short weeks");
            return false;
        }

        // Count the number of working days, then check if there is the required amount
        int numWorkingDays = numberOfWorkingDaysInWeekOf(previousDate);

        return numWorkingDays < required;
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
        LOGGER.debug("Calculating current weekcode for catalogueCode={} and date={}", catalogueCode, customDate);

        // Get the current date
        LocalDate expectedDate = customDate;

        // Select configuration of weekcode calculation
        if( !CODES.containsKey(catalogueCode.toUpperCase()) ){
            throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }
        WeekCodeConfiguration configuration = CODES.get(catalogueCode.toUpperCase());

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
            /*if (isClosingDay(expectedDate, true)) {
                // Move forward to next production day, but stop at sunday since we should return the following week, not any later week
                while (isClosingDay(expectedDate, true) && expectedDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    expectedDate = expectedDate.plusDays(1);
                }
            }*/

            // Check if we have passed the shiftday. There is no adjustments and no checks for closing
            // days, since we just want the code for the actual day.
            if (configuration.getShiftDay() != null && expectedDate.getDayOfWeek().getValue() >= configuration.getShiftDay().getValue()) {
                expectedDate = expectedDate.plusWeeks(1);
            }
        }

        // Build result
        WeekResolverResult result = new WeekResolverResult(configuration, zoneId, locale, catalogueCode, expectedDate);
        result.setDescription(calculateWeekDescription(configuration, customDate, result.getWeekCode()));
        return result;
    }

    public YearPlanResult getYearPlan(Integer year, Boolean showAbnormalDayNames, Boolean displayAllDays) {
        YearPlanResult yearPlan = new YearPlanResult().withYear(String.format("%04d", year));

        // Add headers
        yearPlan.add(new ArrayList<>(getHeadersAsRow(displayAllDays)));

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
        } while (currentDate.getYear() <= year || (currentDate.getYear() == year + 1 && Integer.parseInt(currentDate.format(weekCodeFormatter)) < 2));

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
            yearPlan.add(getResultAsRow(currentResult, showAbnormalDayNames, displayAllDays));

            previousResult = currentResult;
        }

        // Remove the
        return yearPlan;
    }

    private DayOfWeek adjustShiftDay(LocalDate expectedDate, DayOfWeek shiftDay, boolean allowEndOfYear) {
        LOGGER.debug("Adjusting shiftday for {} with shiftday {}", expectedDate, shiftDay);

        // Find the date of the shiftday in this week
        LocalDate dateOfShiftDay = expectedDate;
        dateOfShiftDay = dateOfShiftDay.plusDays(shiftDay.getValue() - expectedDate.getDayOfWeek().getValue());
        LOGGER.debug("ExpectedDate is {} shiftday is then {}", expectedDate, dateOfShiftDay);

        // SPECIAL CASES:
        // 1: If the expected date falls inside the Easter week, then there is no shiftday
        LOGGER.debug("Checking if {} is in the Easter week", expectedDate);
        if( isEasterWeek(expectedDate) ) {
            LOGGER.debug("Sunday this week is Easter sunday. No shiftday for this week");
            return null;
        }
        // 2: If the expected date falls in the pentecost week and shiftday is friday, then move
        //    the shiftday back to thursday
        LOGGER.debug("Checking if {} is in the pentecost week", expectedDate);
        if( isPentecostWeek(expectedDate) && shiftDay == DayOfWeek.FRIDAY ) {
            LOGGER.debug("Sunday this week is pentecost and shiftday is friday. Move shiftday to thursday");
            return DayOfWeek.THURSDAY;
        }
        // 3: If the expected date falls in the week before may 1st. or "Grundlovsdag", then move
        //    the shiftday back to thursday
        if (isWeekBeforeMayFirst(expectedDate) || isWeekBeforeConstitutionDay(expectedDate)) {
            LOGGER.debug("Sunday this week is the week before may 1st. or Grundlovsdag. Move shiftday to thursday");
            return DayOfWeek.THURSDAY;
        }

        //  If the expected date falls in the week before Easter and shiftday is friday, then move shiftday back 1 day
        LOGGER.debug("Checking if next sunday {} is Easter sunday and shiftday {} is friday", expectedDate.plusWeeks(1), shiftDay);
        if( shiftDay == DayOfWeek.FRIDAY && isEasterWeek(expectedDate.plusWeeks(1)) ) {
            LOGGER.debug("Shiftday is a friday and next week is the Easter week. Shiftday adjusted to THURSDAY");
            return DayOfWeek.THURSDAY;
        }

        // Adjust the shiftday back until it is not a closing day. This may potentially roll
        // back into the last week, but if we reach monday, then the shiftday is in effect no matter what,
        // and we will end up adding a week as expected.
        while( isClosingDay(dateOfShiftDay, allowEndOfYear) && dateOfShiftDay.getDayOfWeek() != DayOfWeek.MONDAY ) {
            dateOfShiftDay = dateOfShiftDay.minusDays(1);
            LOGGER.debug("Moving shiftday back 1 day to {}", dateOfShiftDay);
        }

        //  If the next week is week 52 (or 53), Christmas weeks, then move the shiftday back 1 day
        LOGGER.debug("Checking if next day {} is start of the Christmas days", dateOfShiftDay.plusDays(1));
        DateTimeFormatter weekCodeFormatter = DateTimeFormatter.ofPattern("w", locale).withZone(zoneId);
        int weekOfShiftDay = Integer.parseInt(dateOfShiftDay.format(weekCodeFormatter));
        if (weekOfShiftDay >= 51 && dateOfShiftDay.getDayOfWeek() != DayOfWeek.MONDAY) {
            dateOfShiftDay = dateOfShiftDay.minusDays(1);
            LOGGER.debug("Shiftday adjusted to {} due to next week being Christmas week", dateOfShiftDay);
        }

        LOGGER.debug("Final shiftday is set to {}", dateOfShiftDay.getDayOfWeek());
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
            LOGGER.debug("{} is saturday or sunday", expectedDate);
            return true;
        }

        // 1. maj and pinched friday
        if( expectedDate.getMonth() == Month.MAY && expectedDate.getDayOfMonth() == 1 ) {
            LOGGER.debug("{} is 1. may", expectedDate);
            return true;
        }
        if( expectedDate.getMonth() == Month.MAY && expectedDate.getDayOfMonth() == 2 && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
            LOGGER.debug("{} is pinched friday after 1. may", expectedDate);
            return true;
        }

        // Grundlovsdag and pinched friday
        if( expectedDate.getMonth() == Month.JUNE  && expectedDate.getDayOfMonth() == 5 ) {
            LOGGER.debug("{} is constitution day (grundlovsdag)", expectedDate);
            return true;
        }
        if( expectedDate.getMonth() == Month.JUNE && expectedDate.getDayOfMonth() == 6 && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
            LOGGER.debug("{} is pinched friday af constitution day (grundlovsdag)", expectedDate);
            return true;
        }

        // Christmas and pinched friday
        if( expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 24 ||
                expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 25 ||
                expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 26) {
            LOGGER.debug("{} is a christmas day", expectedDate);
            return true;
        }
        if( expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 27 && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
            LOGGER.debug("{} is pinched friday after christmas", expectedDate);
            return true;
        }

        // Check for week 52 and 53, which is never used!
        if( allowEndOfYearWeeks ) {
            // New years eve
            if (expectedDate.getMonth() == Month.DECEMBER && expectedDate.getDayOfMonth() == 31) {
                LOGGER.debug("{} is new years eve", expectedDate);
                return true;
            }
        } else {
            // Check for week 52 and 53
            DateTimeFormatter weekCodeFormatter = DateTimeFormatter.ofPattern("w", locale).withZone(zoneId);

            if (List.of(52, 53).contains(Integer.parseInt(expectedDate.format(weekCodeFormatter)))) {
                LOGGER.debug("{} is within week 52 or 53", expectedDate);

                // After much deliberation, we landed on an empirical rule that states that..:
                //   "if there is 4 or more coherent working days in week 52, then it is ok to place
                //    shiftday and the book cart in week 52, otherwise not"
                if (isWeek(expectedDate, 52)) {
                    int numberOfWorkingDays = numberOfWorkingDaysInWeekOf(expectedDate);
                    LOGGER.debug("{} is within week 52 and has {} working days", expectedDate, numberOfWorkingDays);
                    if (numberOfWorkingDays >= 4) {
                        LOGGER.debug("Allowing week 52 due to empirical rule: There is 4 or more coherent working days");
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        // 1. January and pinched friday
        if( expectedDate.getMonth() == Month.JANUARY && expectedDate.getDayOfMonth() == 1 ) {
            LOGGER.debug("{} is 1. jan (hangover day)", expectedDate);
            return true;
        }
        if( expectedDate.getMonth() == Month.JANUARY && expectedDate.getDayOfMonth() == 2 && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY ) {
            LOGGER.debug("{} is pinched friday after 1. jan (hangover day for older people)", expectedDate);
            return true;
        }

        // Check for Easter, pentecost and ascension day
        if( isEasterAndRelatedClosingDay(expectedDate) ) {
            LOGGER.debug("{} is within Easter, pentecost or ascension day", expectedDate);
            return true;
        }

        // Not a closing day
        LOGGER.debug("{} is not a closing day", expectedDate);
        return false;
    }

    private Boolean isWithinClosingWeek(LocalDate date, boolean allowEndOfYearWeeks) {
        LOGGER.debug("Checking if {} is within a closed week", date);
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

    private LocalDate getFriday(LocalDate date) {
        return date.plusDays( DayOfWeek.FRIDAY.getValue() - date.getDayOfWeek().getValue());
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
            LOGGER.debug("{} is in the Easter week", expectedDate);
            return true;
        }

        // Expected date is not somewhere in the Easter period.
        LOGGER.debug("{} is not in the Easter week", expectedDate);
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
        LOGGER.debug("Pentecost is {}", pentecost);
        if( dateOfSunday.isEqual(pentecost) ) {
            LOGGER.debug("{} is ", dateOfSunday);
            return true;
        }

        // Expected date is not somewhere in the pentecost period.
        LOGGER.debug("{} is not in the pentecost week", expectedDate);
        return false;
    }

    private boolean isWeekBeforeMayFirst(LocalDate expectedDate) {

        // Get the date of sunday in this week and Easter sunday
        LocalDate dateOfSunday = getDateOfSunday(expectedDate);

        if (dateOfSunday.getMonth() == Month.APRIL && dateOfSunday.getDayOfMonth() == 30) {
            LOGGER.debug("{} is the day before may 1st.", dateOfSunday);
            return true;
        } else {
            LOGGER.debug("{} is not the day before may 1st.", dateOfSunday);
            return false;
        }
    }

    // Ahem.. "Constitution Day" is not the most correct translation of "Grundlovsday", but anywho.. :)
    private boolean isWeekBeforeConstitutionDay(LocalDate expectedDate) {

        // Get the date of sunday in this week and Easter sunday
        LocalDate dateOfSunday = getDateOfSunday(expectedDate);

        if (dateOfSunday.getMonth() == Month.JUNE && dateOfSunday.getDayOfMonth() == 4) {
            LOGGER.debug("{} is the day before Grundlovsdag", dateOfSunday);
            return true;
        } else {
            LOGGER.debug("{} is not the day before Grundlovsdag", dateOfSunday);
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
        LOGGER.debug("Easter sunday for {} is {}", expectedDate.getYear(), easterSunday);

        // Check if the expected date is before Maundy Thursday
        if( expectedDate.isAfter(easterSunday.minusDays(4)) && expectedDate.isBefore(easterSunday.plusDays(2)) ) {
            LOGGER.debug("{} is within Easter", expectedDate);
            return true;
        }

        // Pentecost. 7. sunday after Easter sunday
        LocalDate pentecost =  easterSunday.plusWeeks(7);
        while( pentecost.getDayOfWeek() != DayOfWeek.SUNDAY ) {
            pentecost = pentecost.plusDays(1);
        }
        if( expectedDate.isEqual(pentecost.plusDays(1)) ) { // Check for withsun
            LOGGER.debug("{} is withsun", expectedDate);
            return true;
        }

        // Ascension day. 6.th thursday after maundy thursday. Check also for pinched friday
        LocalDate ascensionDay = easterSunday.minusDays(3).plusWeeks(6);
        if( expectedDate.isEqual(ascensionDay) ) {
            LOGGER.debug("{} is ascension day", expectedDate);
            return true;
        }
        if( expectedDate.isEqual(ascensionDay.plusDays(1)) ) {
            LOGGER.debug("{} is pinched friday after ascension day", expectedDate);
            return true;
        }

        // Check for 'store bededag', tounge-in-cheek english name 'prayers day'
        if (expectedDate.getYear() < 2024) {
            LocalDate prayersDay = easterSunday.plusDays(26);
            if (expectedDate.isEqual(prayersDay)) {
                LOGGER.debug("{} is prayers day ('store bededag')", expectedDate);
                return true;
            }
            if (expectedDate.isEqual(prayersDay.plusDays(1)) && expectedDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
                LOGGER.debug("{} is pinched friday after prayers day ('store bededag')", expectedDate);
                return true;
            }
        }

        // Expected date is not somewhere in the Easter period or on any related closing days
        LOGGER.debug("{} is not Easter, pentecost or ascension day", expectedDate);
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
        LOGGER.debug("Easter sunday for {} is {}", dateOfSunday.getYear(), easterSunday);
        return easterSunday;
    }

    private LocalDate getDateOfSunday(LocalDate expectedDate) {
        LocalDate dateOfSunday = expectedDate.plusDays(DayOfWeek.SUNDAY.getValue() - expectedDate.getDayOfWeek().getValue());
        LOGGER.debug("Sunday in this week is {}", dateOfSunday);
        return dateOfSunday;
    }

    private WeekDescription calculateWeekDescription(WeekCodeConfiguration configuration, LocalDate date, String weekcode) {
        WeekDescription description = new WeekDescription();

        // Find monday in this week since all calculations of dates is done from that day
        LocalDate monday = date.minusDays(date.getDayOfWeek().getValue() - 1);

        // Set week number of actual week
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
                LOGGER.debug("WEEKCODE_FIRST = {}", description.getWeekCodeFirst());
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
        LOGGER.debug("WEEKCODE_FIRST = {}", description.getWeekCodeFirst());

        // No further descriptions if this week has no shiftday
        if (description.getShiftDay() == null) {
            return description;
        }

        // Last day this weekcode is assigned
        description.setWeekCodeLast(fromLocalDate(fromDate(description.getShiftDay()).minusDays(1)));
        LOGGER.debug("WEEKCODE_LAST = {}", description.getWeekCodeLast());

        // Book cart the next working day after shiftday. Here we ignore Christmas weeks since
        // the book cart can be handled on working days in the Christmas weeks
        LocalDate bookCart = fromDate(description.getShiftDay());
        do {
            bookCart = bookCart.plusDays(1);
        } while (isClosingDay(bookCart, true));
        description.setBookCart(fromLocalDate(bookCart));
        LOGGER.debug("BOOKCART = {}", description.getBookCart());

        // Proof can start at 17.00 the day the book cart has been handled
        description.setProofFrom(description.getBookCart());
        LOGGER.debug("PROOF_FROM = {}", description.getProofFrom());

        // Proof. The first working day after proof start, not in the Christmas week and before New Year's Eve,
        // Proof must not be within the Easter week
        LocalDate proof = fromDate(description.getProofFrom());
        do {
            proof = proof.plusDays(1);
        } while (isClosingDay(proof, configuration.getAllowEndOfYear()) || isBetweenChristmasAndNewYearsEve(proof) || isEasterWeek(proof));
        description.setProof(fromLocalDate(proof));
        LOGGER.debug("PROOF = {}", description.getProof());

        // Proof must be completed by tuesday in the next week at 17.00. (No adjustments). Same as proof
        description.setProofTo(description.getProof());
        LOGGER.debug("PROOF_TO = {}", description.getProofTo());

        // BKM-red. Wednesday in the next week., the day after proof ended
        // Make sure that BKM-red does not end up on a closing day
        LocalDate bkm = fromDate(description.getProofTo());
        do {
            bkm = bkm.plusDays(1);
        } while (isClosingDay(bkm, configuration.getAllowEndOfYear()) || isEasterWeek(bkm));
        description.setBkm(fromLocalDate(bkm));
        LOGGER.debug("BKM = {}", description.getBkm());

        // Publish date. Last working day in the week of the proof.
        LocalDate publish = getFriday(fromDate(description.getProofTo()));
        while (isClosingDay(publish, configuration.getAllowEndOfYear()) && publish.isAfter(fromDate(description.getProofTo()))) {
            publish = publish.minusDays(1);
        }
        description.setPublish(fromLocalDate(publish));
        LOGGER.debug("PUBLISH = {}", description.getPublish());

        return description;
    }

    public boolean isBetweenChristmasAndNewYearsEve(LocalDate date) {
        return date.getMonth() == Month.DECEMBER && date.getDayOfMonth() >= 24;
    }

    public Date fromLocalDate(LocalDate date) {
        return Date.from(date.atStartOfDay(zoneId).toInstant());
    }

    public LocalDate fromDate(Date date) {
        return LocalDate.ofInstant(date.toInstant(), zoneId);
    }

    private List<YearPlanResult.YearPlanRowColumn> getHeadersAsRow(Boolean displayAllDays) {
        // Make sure that results are returned in this order by getResultAsRow()

        return List.of(
                new YearPlanResult.YearPlanRowColumn("Katalogkode", false, true),
                new YearPlanResult.YearPlanRowColumn("DBCKat ugekode start", false, true),
                new YearPlanResult.YearPlanRowColumn("DBCKat ugekode slut", false, true),
                new YearPlanResult.YearPlanRowColumn("DBCKat ugeafslutning", false, true),
                new YearPlanResult.YearPlanRowColumn("Bogvogn", false, true),
                new YearPlanResult.YearPlanRowColumn("Ugekorrekturen køres", false, displayAllDays),
                new YearPlanResult.YearPlanRowColumn("Ugekorrektur", false, true),
                new YearPlanResult.YearPlanRowColumn("Slutredaktion (ugekorrektur)", false, displayAllDays),
                new YearPlanResult.YearPlanRowColumn("BKM-red.", false, true),
                new YearPlanResult.YearPlanRowColumn("Udgivelsesdato", false, true),
                new YearPlanResult.YearPlanRowColumn("Ugenummber", false, true)
        );
    }

    private List<YearPlanResult.YearPlanRowColumn> getResultAsRow(WeekResolverResult result, Boolean showAbnormalDayNames, Boolean displayAllDays) {
        // Make sure that headers are returned in same order as the rows are added below by getHeadersAsRow()

        if (result.getDescription().getNoProduction()) {
            return List.of(
                    new YearPlanResult.YearPlanRowColumn(result.getDescription().getWeekCodeShort()),
                    new YearPlanResult.YearPlanRowColumn(),
                    new YearPlanResult.YearPlanRowColumn(),
                    new YearPlanResult.YearPlanRowColumn(),
                    new YearPlanResult.YearPlanRowColumn(),
                    new YearPlanResult.YearPlanRowColumn().withVisible(displayAllDays),
                    new YearPlanResult.YearPlanRowColumn(),
                    new YearPlanResult.YearPlanRowColumn().withVisible(displayAllDays),
                    new YearPlanResult.YearPlanRowColumn(),
                    new YearPlanResult.YearPlanRowColumn(),
                    new YearPlanResult.YearPlanRowColumn(result.getDescription().getWeekNumber())
            );
        }

        return List.of(
                new YearPlanResult.YearPlanRowColumn(result.getDescription().getWeekCodeShort()),
                rowContentFromDate(isSpecialDay(result.getDescription().getWeekCodeFirst(), DayOfWeek.FRIDAY),
                        result.getDescription().getWeekCodeFirst(), true, showAbnormalDayNames, true),
                rowContentFromDate(isSpecialDay(result.getDescription().getWeekCodeLast(), DayOfWeek.THURSDAY),
                        result.getDescription().getWeekCodeLast(), true, showAbnormalDayNames, true),
                rowContentFromDate(isSpecialDay(result.getDescription().getShiftDay(), DayOfWeek.FRIDAY),
                        result.getDescription().getShiftDay(), true, showAbnormalDayNames, true),
                rowContentFromDate(isSpecialDay(result.getDescription().getBookCart(), DayOfWeek.MONDAY),
                        result.getDescription().getBookCart(), true, showAbnormalDayNames, true),
                rowContentFromDate(result.getDescription().getProofFrom(), true, displayAllDays),
                rowContentFromDate(result.getDescription().getProof(), true, true),
                rowContentFromDate(result.getDescription().getProofTo(), true, displayAllDays),
                rowContentFromDate(isSpecialDay(result.getDescription().getBkm(), DayOfWeek.WEDNESDAY),
                        result.getDescription().getBkm(), true, showAbnormalDayNames, true),
                rowContentFromDate(result.getDescription().getPublish(), true, true),
                new YearPlanResult.YearPlanRowColumn(result.getDescription().getWeekNumber())
        );
    }

    private Boolean isSpecialDay(Date date, DayOfWeek expectedDayOfWeek) {
        if (date == null) {
            return false;
        }
        LocalDate actualDayOfWeek = fromDate(date);
        return actualDayOfWeek.getDayOfWeek() != expectedDayOfWeek;
    }

    public YearPlanResult.YearPlanRowColumn rowContentFromDate(Date date) {
        return rowContentFromDate(date, false, true);
    }

    private YearPlanResult.YearPlanRowColumn rowContentFromDate(Date date, boolean quoted, boolean displayAllDays) {
        return rowContentFromDate(false, date, quoted, false, displayAllDays);
    }

    private YearPlanResult.YearPlanRowColumn rowContentFromDate(Boolean isAbnormalDay, Date date, boolean quoted, Boolean showAbnormalDayNames, boolean displayAllDays) {
        if (date == null) {
            return new YearPlanResult.YearPlanRowColumn(quoted ? "\"\"" : "", false, displayAllDays);
        }

        LocalDate actualDayOfWeek = fromDate(date);
        String prefix = isAbnormalDay && showAbnormalDayNames
                ? actualDayOfWeek.getDayOfWeek().getDisplayName(TextStyle.FULL, locale).toUpperCase() + "   "
                : "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", locale);
        LocalDate localDate = fromDate(date);
        return new YearPlanResult.YearPlanRowColumn(
                (quoted ? "\"" : "") + prefix + localDate.format(formatter) + (quoted ? "\"" : ""),
                isAbnormalDay, displayAllDays);
    }

    public LocalDate fromString(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
