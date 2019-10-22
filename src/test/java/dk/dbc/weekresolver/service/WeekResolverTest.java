package dk.dbc.weekresolver.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.format.DateTimeParseException;

public class WeekResolverTest {
    final static String zone = "Europe/Copenhagen";
    final static SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd");

    @Test
    public void TestInvalidCatalogueCode() {
        WeekResolver b = new WeekResolver(zone)
                .withDate("2019-11-29")
                .withCatalogueCode("qxz");
        assertThrows(UnsupportedOperationException.class,() -> b.build());
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
    public void TestCatalogueCodeBPF() throws ParseException {
        WeekResolver b = new WeekResolver(zone)
                .withCatalogueCode("dpf");

        assertDoesNotThrow(() -> b.withDate("2019-11-29").build());

        WeekResolverResult result = b.withDate("2019-11-29").build();
        assertThat(result.getWeekNumber(), is(51));
        assertThat(result.getYear(), is(2019));
        assertThat(result.getCatalogueCode(), is("dpf"));
        assertThat(result.getWeekCode(), is("dpf201951"));

        result = b.withDate("2019-12-29").build();
        assertThat(result.getWeekNumber(), is(3));
        assertThat(result.getYear(), is(2020));
        assertThat(result.getCatalogueCode(), is("dpf"));
        assertThat(result.getWeekCode(), is("dpf202003"));
        LocalDate localDate = LocalDate.of(2020, 1, 13);
        Date  d = Date.from(localDate.atStartOfDay(ZoneId.of(zone)).toInstant());
        assertThat(result.getDate(), is(d));
    }

    @Test
    public void TestDPFAtEaster2019() {

        // This test uses the DPF cataloguecode to test the generic logic handling easter (and other closingdays).
        // DPF should add 2 weeks and uses friday as shiftday
        WeekResolver b = new WeekResolver(zone)
                .withCatalogueCode("DPF");

        // take date
        // add [0,1,2] weeks
        // if closed => add 1 week
        // if day-of-week = shiftday => add 1 week

        // 3. apr. = wednesday => + 2 weeks = 17. apr., week 16, the day before maundy thursday.
        // Not closed and not shiftday = 16
        assertThat(b.withDate("2019-04-03").build().getWeekCode(), is("DPF201916"));

        // 4. apr. = thursday => + 2 weeks = 18. apr., week 16, maundy thursday
        // Closed, but not shiftday so add 1 week = week 17
        assertThat(b.withDate("2019-04-04").build().getWeekCode(), is("DPF201917"));

        // 5.apr. = friday => + 2 weeks = 19. apr., week 16, good friday
        // Closed and shiftday so add 2 weeks = week 18
        assertThat(b.withDate("2019-04-05").build().getWeekCode(), is("DPF201918"));

        // 8. apr. = monday => + 2 weeks = 22. apr., week 17, easter monday
        // Closed, not shiftday so add 1 week = 18
        assertThat(b.withDate("2019-04-08").build().getWeekCode(), is("DPF201918"));

        // 15. apr. = monday => + 2 weeks = 29. apr., week 18
        // Not closed and not shiftday = 18
        assertThat(b.withDate("2019-04-15").build().getWeekCode(), is("DPF201918"));

        // 17. apr. = wednesday => + 2 weeks = 1. may., week 18
        // Closed, not shiftday so add 1 week = 19
        assertThat(b.withDate("2019-04-17").build().getWeekCode(), is("DPF201919"));

        // 23. apr. = thursday => + 2 weeks = 7. may., week 19
        // Not closed and not shiftday = 19
        assertThat(b.withDate("2019-04-23").build().getWeekCode(), is("DPF201919"));

        // 26. apr. = friday => + 2 weeks = 10. may., week 19
        // Not closed but shiftday = 20
        assertThat(b.withDate("2019-04-26").build().getWeekCode(), is("DPF201920"));
    }
}
