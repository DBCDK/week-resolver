package dk.dbc.weekresolver.service;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.format.DateTimeParseException;
import java.util.Locale;

public class WeekResolverTest {
    final static String zone = "Europe/Copenhagen";

    @Test
    public void TestInvalidCatalogueCode() {
        WeekResolver b = new WeekResolver(zone)
                .withDate("2019-11-29")
                .withCatalogueCode("qxz");
        assertThrows(UnsupportedOperationException.class, () -> b.build());
    }

    @Test
    public void TestInvalidDates() {
        assertThrows(DateTimeParseException.class, () -> new WeekResolver(zone).withDate("2019-13-29"));
        assertThrows(DateTimeParseException.class, () -> new WeekResolver(zone).withDate("11-29"));
    }

    @Test
    public void TestValidCatalogueCodeLowerCase() {
        WeekResolver b = new WeekResolver(zone)
                .withDate("2019-11-29");
        assertDoesNotThrow(() -> b.withCatalogueCode("dpf").build());
        assertDoesNotThrow(() -> b.withCatalogueCode("DPF").build());
    }

    @Test
    public void TestCatalogueCodeDPF() throws ParseException {
        WeekResolver b = new WeekResolver(zone)
                .withCatalogueCode("dpf");

        assertDoesNotThrow(() -> b.withDate("2019-11-29").build());

        WeekResolverResult result = b.withDate("2019-11-29").build();
        assertThat(result.getWeekNumber(), is(51));
        assertThat(result.getYear(), is(2019));
        assertThat(result.getCatalogueCode(), is("DPF"));
        assertThat(result.getWeekCode(), is("DPF201951"));

        result = b.withDate("2019-12-29").build();
        assertThat(result.getWeekNumber(), is(3));
        assertThat(result.getYear(), is(2020));
        assertThat(result.getCatalogueCode(), is("DPF"));
        assertThat(result.getWeekCode(), is("DPF202003"));
        LocalDate localDate = LocalDate.of(2020, 1, 13);
        Date d = Date.from(localDate.atStartOfDay(ZoneId.of(zone)).toInstant());
        assertThat(result.getDate(), is(d));
    }

    @Test
    public void TestDPFAtEaster2019() {

        // This test uses the DPF cataloguecode to test the generic logic handling easter (and other closingdays).
        // DPF should add 2 weeks and uses friday as shiftday
        WeekResolver b = new WeekResolver(zone)
                .withCatalogueCode("DPF");

        // take date
        // if day-of-week >= shiftday then add days untill beginning of next week
        // add [0,1,2] weeks
        // if closed => add days untill not a closing day

        // 3. apr. = wednesday => + 2 weeks = 17. apr., week 16, the day before maundy thursday.
        // Not closed and not shiftday = 16
        assertThat(b.withDate("2019-04-03").build().getWeekCode(), is("DPF201916"));

        // 4. apr. = thursday => + 2 weeks = 18. apr., week 16, maundy thursday
        // Closed, but not shiftday so add 2 weeks and ajust for closing days = tuesday week 17
        assertThat(b.withDate("2019-04-04").build().getWeekCode(), is("DPF201917"));

        // 5.apr. = friday, shiftday => + 1 week + 2 weeks = 26. apr., week 17
        // Shiftday so add 1 + 2 weeks = week 17
        assertThat(b.withDate("2019-04-05").build().getWeekCode(), is("DPF201917"));

        // 8. apr. = monday => + 2 weeks = 22. apr., week 17, easter monday
        // Closed, not shiftday so add 2 weeks and adjust for closing days = tuesday week 17
        assertThat(b.withDate("2019-04-08").build().getWeekCode(), is("DPF201917"));

        // 15. apr. = monday => + 2 weeks = 29. apr., week 18
        // Not closed and not shiftday = 18
        assertThat(b.withDate("2019-04-15").build().getWeekCode(), is("DPF201918"));

        // 17. apr. = wednesday => + 2 weeks = 1. may., week 18
        // Closed, not shiftday so add 2 weeks and adjust for closing days = 18
        assertThat(b.withDate("2019-04-17").build().getWeekCode(), is("DPF201918"));

        // 23. apr. = thursday => + 2 weeks = 7. may., week 19
        // Not closed and not shiftday = 19
        assertThat(b.withDate("2019-04-23").build().getWeekCode(), is("DPF201919"));

        // 26. apr. = friday, shiftday => + 1 + 2 weeks = 17. may., week 20
        // Not closed but shiftday = 20
        assertThat(b.withDate("2019-04-26").build().getWeekCode(), is("DPF201920"));
    }

    @Test
    public void TestCatalogueCodeFPF() throws ParseException {

        // This code has the same configuration as DPF, so just check that the code is accepted
        WeekResolver b = new WeekResolver(zone)
                .withCatalogueCode("FPF");

        assertDoesNotThrow(() -> b.withDate("2019-11-29").build());
        assertThat(b.withDate("2019-11-29").build().getCatalogueCode(), is("FPF"));
    }

    @Test
    public void TestCatalogueCodeGPF() throws ParseException {

        // This code has the same configuration as DPF, so just check that the code is accepted
        WeekResolver b = new WeekResolver(zone)
                .withCatalogueCode("GPF");

        assertDoesNotThrow(() -> b.withDate("2019-11-29").build());
        assertThat(b.withDate("2019-11-29").build().getCatalogueCode(), is("GPF"));
    }

    /* Below here is compressed tests for the remaining weekcodes */

    @Test
    public void TestCatalogueCodeEMO() throws ParseException {

        // EMO:
        //   - current week number + 1
        //   - Shiftday is sunday
        //   - No handling of closing days
        //   - Allow end of year weeks
        WeekResolver b = new WeekResolver(zone).withCatalogueCode("EMO");
        assertDoesNotThrow(() -> b.withDate("2019-11-29").build());
        assertThat(b.withDate("2019-11-23").build().getWeekCode(), is("EMO201948")); // saturday, week 47 = 48
        assertThat(b.withDate("2019-11-24").build().getWeekCode(), is("EMO201948")); // sunday, week 47 = 48
        assertThat(b.withDate("2019-11-25").build().getWeekCode(), is("EMO201949")); // monday, week 48 = 49
        assertThat(b.withDate("2019-12-26").build().getWeekCode(), is("EMO202001")); // wednesday, week 52 = 01
        assertThat(b.withDate("2020-04-09").build().getWeekCode(), is("EMO202016")); // thursday, week 15 = 16
        assertThat(b.withDate("2020-05-01").build().getWeekCode(), is("EMO202019")); // friday may 1.st (closing day ignored), week 18 = 19
    }

    @Test
    public void TestCatalogueCodeEMS() throws ParseException {

        // EMS:
        //   - current week number + 1
        //   - Shiftday is sunday
        //   - No handling of closing days
        //   - Allow end of year weeks
        WeekResolver b = new WeekResolver(zone).withCatalogueCode("EMS");
        assertDoesNotThrow(() -> b.withDate("2019-11-29").build());
        assertThat(b.withDate("2019-11-23").build().getWeekCode(), is("EMS201948")); // saturday, week 47 = 48
        assertThat(b.withDate("2019-11-24").build().getWeekCode(), is("EMS201948")); // sunday, week 47 = 48
        assertThat(b.withDate("2019-11-25").build().getWeekCode(), is("EMS201949")); // monday, week 48 = 49
        assertThat(b.withDate("2019-12-26").build().getWeekCode(), is("EMS202001")); // wednesday, week 52 = 01
        assertThat(b.withDate("2020-04-09").build().getWeekCode(), is("EMS202016")); // thursday, week 15 = 16
        assertThat(b.withDate("2020-05-01").build().getWeekCode(), is("EMS202019")); // friday may 1.st (closing day ignored), week 18 = 19
    }

    @Test
    public void TestCatalogueCodeACC() throws ParseException {

        // ACC (and ACE, ACF, ACT, ACM, ARK, BLG):
        //   - Current week number
        //   - No shiftday
        //   - No handling of closing days
        //   - Allow end of year weeks
        WeekResolver b = new WeekResolver(zone).withCatalogueCode("ACC");
        assertDoesNotThrow(() -> b.withDate("2019-12-26").build());
        assertThat(b.withDate("2019-12-26").build().getWeekCode(), is("ACC201952"));
    }

    @Test
    public void TestFixedCodes() throws ParseException {

        // Codes DBR, DBT, SDT should return a fixed code since they are used for retro updates
        // and since we dont have the record data, we cannot set the correct weekcode
        WeekResolver b = new WeekResolver();
        assertDoesNotThrow(() -> b.withCatalogueCode("DBR").withDate("2019-12-26").build());
        assertThat(b.withCatalogueCode("DBR").withDate("2019-12-26").build().getWeekCode(), is("DBR999999"));

        assertDoesNotThrow(() -> b.withCatalogueCode("DBT").withDate("2019-12-26").build());
        assertThat(b.withCatalogueCode("DBT").withDate("2019-12-26").build().getWeekCode(), is("DBT999999"));

        assertDoesNotThrow(() -> b.withCatalogueCode("SDT").withDate("2019-12-26").build());
        assertThat(b.withCatalogueCode("SDT").withDate("2019-12-26").build().getWeekCode(), is("SDT999999"));
    }

    @Test
    public void TestLocaleParameter() {
        WeekResolver b1 = new WeekResolver().withCatalogueCode("EMS");
        WeekResolver b2 = new WeekResolver(zone, new Locale("da", "DK")).withCatalogueCode("EMS");
        WeekResolver b3 = new WeekResolver().withCatalogueCode("EMS").withLocale(new Locale("da", "DK"));
        WeekResolver b4 = new WeekResolver().withCatalogueCode("EMS").withLocale(new Locale("en", "US")); // Uses sunday as week begin

        assertThat(b1.withDate("2019-11-24").build().getWeekCode(), is("EMS201948")); // sunday, week 47 = 48
        assertThat(b1.withDate("2019-11-25").build().getWeekCode(), is("EMS201949")); // monday, week 48 = 49

        assertThat(b2.withDate("2019-11-24").build().getWeekCode(), is("EMS201948")); // sunday, week 47 = 48
        assertThat(b2.withDate("2019-11-25").build().getWeekCode(), is("EMS201949")); // monday, week 48 = 49

        assertThat(b3.withDate("2019-11-24").build().getWeekCode(), is("EMS201948")); // sunday, week 47 = 48
        assertThat(b4.withDate("2019-11-25").build().getWeekCode(), is("EMS201949")); // monday, week 48 = 49

        assertThat(b4.withDate("2019-11-23").build().getWeekCode(), is("EMS201948")); // saturday, week 47 = 48
        assertThat(b4.withDate("2019-11-24").build().getWeekCode(), is("EMS201949")); // sunday, week 47 (week 48 in us) = 49
        assertThat(b4.withDate("2019-11-25").build().getWeekCode(), is("EMS201949")); // monday, week 48 = 49
    }
}