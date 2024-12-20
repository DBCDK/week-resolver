package dk.dbc.weekresolver.service;

import dk.dbc.weekresolver.model.WeekResolverResult;
import dk.dbc.weekresolver.model.YearPlanResult;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeekResolverTest {
    final static String zone = "Europe/Copenhagen";

    @Test
    void TestInvalidCatalogueCode() {
        WeekResolver wr = new WeekResolver(zone)
                .withDate("2019-11-29")
                .withCatalogueCode("qxz");
        assertThrows(UnsupportedOperationException.class, wr::getWeekCode);
    }

    @Test
    void TestInvalidDates() {
        assertThrows(DateTimeParseException.class, () -> new WeekResolver(zone).withDate("2019-13-29"));
        assertThrows(DateTimeParseException.class, () -> new WeekResolver(zone).withDate("11-29"));
    }

    @Test
    void TestValidCatalogueCodeLowerCase() {
        WeekResolver wr = new WeekResolver(zone)
                .withDate("2019-11-29");
        assertDoesNotThrow(() -> wr.withCatalogueCode("dpf").getWeekCode());
        assertDoesNotThrow(() -> wr.withCatalogueCode("DPF").getWeekCode());
    }

    @Test
    void TestCatalogueCodeDPF() {
        WeekResolver wr = new WeekResolver(zone)
                .withCatalogueCode("dpf");

        assertDoesNotThrow(() -> wr.withDate("2019-11-22").getWeekCode());

        WeekResolverResult result = wr.withDate("2019-11-22").getWeekCode();
        assertThat(result.getWeekNumber(), is(51));
        assertThat(result.getYear(), is(2019));
        assertThat(result.getCatalogueCode(), is("DPF"));
        assertThat(result.getWeekCode(), is("DPF201951"));

        result = wr.withDate("2019-12-22").getWeekCode();
        assertThat(result.getWeekNumber(), is(3));
        assertThat(result.getYear(), is(2020));
        assertThat(result.getCatalogueCode(), is("DPF"));
        assertThat(result.getWeekCode(), is("DPF202003"));
        LocalDate localDate = LocalDate.of(2020, 1, 13);
        Date d = Date.from(localDate.atStartOfDay(ZoneId.of(zone)).toInstant());
        assertThat(result.getDate(), is(d));
    }

    @Test
    void TestDPFAtEaster2019() {

        // This test uses the DPF cataloguecode to test the generic logic handling easter (and other closingdays).
        // DPF should add 3 weeks and uses friday as shiftday
        WeekResolver wr = new WeekResolver(zone)
                .withCatalogueCode("DPF");

        // take date
        // if day-of-week >= shiftday then add days untill beginning of next week
        // add [0,1,2] weeks
        // if closed => add days untill not a closing day

        // 27. mar. = wednesday => + 3 weeks = 17. apr., week 16, the day before maundy thursday.
        // Not closed and not shiftday = 16, and not within the week after easter
        assertThat(wr.withDate("2019-03-27").getWeekCode().getWeekCode(), is("DPF201916"));

        // 28. mar. = thursday => loong before easter
        assertThat(wr.withDate("2019-03-28").getWeekCode().getWeekCode(), is("DPF201916"));

        // 5.apr. = friday, shiftday => loong before easter but shiftday
        assertThat(wr.withDate("2019-03-29").getWeekCode().getWeekCode(), is("DPF201917"));

        // 1. apr. = monday => 2 weeks before easter
        assertThat(wr.withDate("2019-04-01").getWeekCode().getWeekCode(), is("DPF201917"));

        // 03. apr. = wednesday => 2 weeks before easter
        assertThat(wr.withDate("2019-04-03").getWeekCode().getWeekCode(), is("DPF201917"));

        // 8. apr. = monday => next week is easter week so no proof in that week
        assertThat(wr.withDate("2019-04-08").getWeekCode().getWeekCode(), is("DPF201919"));

        // 16. apr. = thursday => + 3 weeks + easter = 14. may., week 20
        // Not closed and not shiftday = 19
        assertThat(wr.withDate("2019-04-16").getWeekCode().getWeekCode(), is("DPF201920"));

        // 26. apr. = friday, shiftday => + 1 + 3 weeks = 24. may., week 21
        // Not closed but shiftday = 20
        assertThat(wr.withDate("2019-04-26").getWeekCode().getWeekCode(), is("DPF201921"));
    }

    @Test
    void TestCatalogueCodeFPF() {

        // This code has the same configuration as DPF, so just check that the code is accepted
        WeekResolver wr = new WeekResolver(zone)
                .withCatalogueCode("FPF");

        assertDoesNotThrow(() -> wr.withDate("2019-11-29").getWeekCode());
        assertThat(wr.withDate("2019-11-29").getWeekCode().getCatalogueCode(), is("FPF"));
    }

    @Test
    void TestCatalogueCodeGPF() {

        // This code has the same configuration as DPF, so just check that the code is accepted
        WeekResolver wr = new WeekResolver(zone)
                .withCatalogueCode("GPF");

        assertDoesNotThrow(() -> wr.withDate("2019-11-29").getWeekCode());
        assertThat(wr.withDate("2019-11-29").getWeekCode().getCatalogueCode(), is("GPF"));
    }

    /* Below here is compressed tests for the remaining weekcodes */

    @Test
    void TestCatalogueCodeEMO() {

        // EMO:
        //   - current week number + 2
        //   - Shiftday is friday
        //   - No handling of closing days
        //   - Allow end of year weeks
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("EMO");
        assertDoesNotThrow(() -> wr.withDate("2019-11-29").getWeekCode());
        assertThat(wr.withDate("2019-11-23").getWeekCode().getWeekCode(), is("EMO201950")); // saturday, week 47 = 50 (after shiftday)
        assertThat(wr.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMO201950")); // sunday, week 47 = 50 (after shiftday)
        assertThat(wr.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMO201950")); // monday, week 48 = 50
        assertThat(wr.withDate("2019-12-26").getWeekCode().getWeekCode(), is("EMO202002")); // wednesday, week 52 = 02
        assertThat(wr.withDate("2020-04-09").getWeekCode().getWeekCode(), is("EMO202017")); // thursday, week 15 = 17
        assertThat(wr.withDate("2019-05-01").getWeekCode().getWeekCode(), is("EMO201920")); // wednesday, may 1.st (closing day ignored), week 18 = 20
    }

    @Test
    void TestCatalogueCodeEMS() {

        // EMS:
        //   - current week number + 2
        //   - Shiftday is friday
        //   - No handling of closing days
        //   - Allow end of year weeks
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("EMS");
        assertDoesNotThrow(() -> wr.withDate("2019-11-29").getWeekCode());
        assertThat(wr.withDate("2019-11-23").getWeekCode().getWeekCode(), is("EMS201950")); // saturday, week 47 = 50 (after shiftday)
        assertThat(wr.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMS201950")); // sunday, week 47 = 50 (after shiftday)
        assertThat(wr.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMS201950")); // monday, week 48 = 50
        assertThat(wr.withDate("2019-12-26").getWeekCode().getWeekCode(), is("EMS202002")); // wednesday, week 52 = 02
        assertThat(wr.withDate("2020-04-09").getWeekCode().getWeekCode(), is("EMS202017")); // thursday, week 15 = 17
        assertThat(wr.withDate("2025-05-01").getWeekCode().getWeekCode(), is("EMS202520")); // thursday, may 1.st (closing day ignored), week 18 = 20
    }

    @Test
    void TestCatalogueCodeEMK() {

        // EMK:
        //   - current week number + 2
        //   - Shiftday is friday
        //   - No handling of closing days
        //   - Allow end of year weeks
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("EMK");
        assertDoesNotThrow(() -> wr.withDate("2019-11-29").getWeekCode());
        assertThat(wr.withDate("2019-11-23").getWeekCode().getWeekCode(), is("EMK201950")); // saturday, week 47 = 50 (after shiftday)
        assertThat(wr.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMK201950")); // sunday, week 47 = 50 (after shiftday)
        assertThat(wr.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMK201950")); // monday, week 48 = 50
        assertThat(wr.withDate("2019-12-26").getWeekCode().getWeekCode(), is("EMK202002")); // wednesday, week 52 = 02
        assertThat(wr.withDate("2020-04-09").getWeekCode().getWeekCode(), is("EMK202017")); // thursday, week 15 = 17
        assertThat(wr.withDate("2025-05-01").getWeekCode().getWeekCode(), is("EMK202520")); // thursday, may 1.st (closing day ignored), week 18 = 20
    }

    @Test
    void TestCatalogueCodeEMM() {

        // EMM:
        //   - current week number + 2
        //   - Shiftday is friday
        //   - No handling of closing days
        //   - Allow end of year weeks
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("EMM");
        assertDoesNotThrow(() -> wr.withDate("2019-11-29").getWeekCode());
        assertThat(wr.withDate("2019-11-23").getWeekCode().getWeekCode(), is("EMM201950")); // saturday, week 47 = 50 (after shiftday)
        assertThat(wr.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMM201950")); // sunday, week 47 = 50 (after shiftday)
        assertThat(wr.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMM201950")); // monday, week 48 = 50
        assertThat(wr.withDate("2019-12-26").getWeekCode().getWeekCode(), is("EMM202002")); // wednesday, week 52 = 02
        assertThat(wr.withDate("2020-04-09").getWeekCode().getWeekCode(), is("EMM202017")); // thursday, week 15 = 17
        assertThat(wr.withDate("2025-05-01").getWeekCode().getWeekCode(), is("EMM202520")); // thursday, may 1.st (closing day ignored), week 18 = 20
    }

    @Test
    void TestCatalogueCodeACC() {

        // ACC (and ACE, ACF, ACT, ACM, ARK, BLG):
        //   - Current week number
        //   - No shiftday
        //   - No handling of closing days
        //   - Allow end of year weeks
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("ACC");
        assertDoesNotThrow(() -> wr.withDate("2019-12-26").getWeekCode());
        assertThat(wr.withDate("2019-12-26").getWeekCode().getWeekCode(), is("ACC201952"));
    }

    @Test
    void TestFixedCodes() {

        // Codes DBT, SDT should return a fixed code since they are used for retro updates
        // and since we dont have the record data, we cannot set the correct weekcode
        WeekResolver wr = new WeekResolver(zone);
        assertDoesNotThrow(() -> wr.withCatalogueCode("DBT").withDate("2019-12-26").getWeekCode());
        assertThat(wr.withCatalogueCode("DBT").withDate("2019-12-26").getWeekCode().getWeekCode(), is("DBT999999"));

        assertDoesNotThrow(() -> wr.withCatalogueCode("SDT").withDate("2019-12-26").getWeekCode());
        assertThat(wr.withCatalogueCode("SDT").withDate("2019-12-26").getWeekCode().getWeekCode(), is("SDT999999"));
    }

    @Test
    void TestLocaleParameter() {
        WeekResolver wr1 = new WeekResolver().withCatalogueCode("EMS");
        WeekResolver wr2 = new WeekResolver(zone, new Locale("da", "DK")).withCatalogueCode("EMS");
        WeekResolver wr3 = new WeekResolver().withCatalogueCode("EMS").withLocale(new Locale("da", "DK"));
        WeekResolver wr4 = new WeekResolver().withCatalogueCode("EMS").withLocale(new Locale("en", "US")); // Uses sunday as week begin

        assertThat(wr1.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMS201950")); // sunday, week 47 = 50
        assertThat(wr1.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMS201950")); // monday, week 48 = 50

        assertThat(wr2.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMS201950")); // sunday, week 47 = 50
        assertThat(wr2.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMS201950")); // monday, week 48 = 50

        assertThat(wr3.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMS201950")); // sunday, week 47 = 50
        assertThat(wr4.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMS201950")); // monday, week 48 = 50

        assertThat(wr4.withDate("2019-11-23").getWeekCode().getWeekCode(), is("EMS201950")); // saturday, week 47 = 50
        assertThat(wr4.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMS201950")); // sunday, week 47 (week 48 in us) = 50
        assertThat(wr4.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMS201950")); // monday, week 48 = 50
    }

    @Test
    void TestBkm() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        assertThat(wr.withDate("2020-05-05").getWeekCode().getWeekCode(), is("BKM202021"));
    }

    @Test
    void TestYearEnd() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        assertThat(wr.withDate("2019-12-03").getWeekCode().getWeekCode(), is("BKM201951"));
        assertThat(wr.withDate("2019-12-10").getWeekCode().getWeekCode(), is("BKM201952"));
    }

    @Test
    void TestAllCodes() {
        WeekResolver wr = new WeekResolver(zone);

        // +0 weeks
        assertThat(wr.withCatalogueCode("ACC").withDate("2020-04-22").getWeekCode().getWeekCode(), is("ACC202017"));
        assertThat(wr.withCatalogueCode("ACE").withDate("2020-04-22").getWeekCode().getWeekCode(), is("ACE202017"));
        assertThat(wr.withCatalogueCode("ACF").withDate("2020-04-22").getWeekCode().getWeekCode(), is("ACF202017"));
        assertThat(wr.withCatalogueCode("ACK").withDate("2021-09-01").getWeekCode().getWeekCode(), is("ACK202135"));
        assertThat(wr.withCatalogueCode("ACM").withDate("2020-04-22").getWeekCode().getWeekCode(), is("ACM202017"));
        assertThat(wr.withCatalogueCode("ACN").withDate("2021-08-23").getWeekCode().getWeekCode(), is("ACN202134"));
        assertThat(wr.withCatalogueCode("ACP").withDate("2021-08-31").getWeekCode().getWeekCode(), is("ACP202135"));
        assertThat(wr.withCatalogueCode("ACT").withDate("2020-04-22").getWeekCode().getWeekCode(), is("ACT202017"));
        assertThat(wr.withCatalogueCode("ARK").withDate("2020-04-22").getWeekCode().getWeekCode(), is("ARK202018"));
        assertThat(wr.withCatalogueCode("BLG").withDate("2020-04-22").getWeekCode().getWeekCode(), is("BLG202017"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-06-15").getWeekCode().getWeekCode(), is("VPT202326"));

        // +1 week
        // Note: When checked, existing records from this date has EM[S|O]202019, that is current week + 1
        // This is different from the specification of the future use of EMS/EMO
        assertThat(wr.withCatalogueCode("EMO").withDate("2020-04-22").getWeekCode().getWeekCode(), is("EMO202019"));
        assertThat(wr.withCatalogueCode("EMS").withDate("2020-04-22").getWeekCode().getWeekCode(), is("EMS202019"));
        assertThat(wr.withCatalogueCode("DAN").withDate("2020-04-22").getWeekCode().getWeekCode(), is("DAN202018"));
        assertThat(wr.withCatalogueCode("DAR").withDate("2020-04-22").getWeekCode().getWeekCode(), is("DAR202018"));
        assertThat(wr.withCatalogueCode("ABU").withDate("2023-06-01").getWeekCode().getWeekCode(), is("ABU202323"));
        assertThat(wr.withCatalogueCode("ABU").withDate("2023-06-02").getWeekCode().getWeekCode(), is("ABU202323"));
        assertThat(wr.withCatalogueCode("ABU").withDate("2023-06-03").getWeekCode().getWeekCode(), is("ABU202323"));
        assertThat(wr.withCatalogueCode("ABU").withDate("2023-06-04").getWeekCode().getWeekCode(), is("ABU202323"));
        assertThat(wr.withCatalogueCode("ABU").withDate("2023-06-05").getWeekCode().getWeekCode(), is("ABU202324"));

        // +2 weeks
        assertThat(wr.withCatalogueCode("LIT").withDate("2023-05-15").getWeekCode().getWeekCode(), is("LIT202322"));

        // +2 weeks
        assertThat(wr.withCatalogueCode("DLR").withDate("2020-04-22").getWeekCode().getWeekCode(), is("DLR202019"));
        assertThat(wr.withCatalogueCode("DLF").withDate("2020-04-29").getWeekCode().getWeekCode(), is("DLF202020"));
        assertThat(wr.withCatalogueCode("DBR").withDate("2019-12-26").getWeekCode().getWeekCode(), is("DBR202003"));
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-04-27").getWeekCode().getWeekCode(), is("DBF202020"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-04-27").getWeekCode().getWeekCode(), is("BKM202020"));
        assertThat(wr.withCatalogueCode("GBF").withDate("2019-11-26").getWeekCode().getWeekCode(), is("GBF201950"));
        assertThat(wr.withCatalogueCode("FLX").withDate("2023-05-31").getWeekCode().getWeekCode(), is("FLX202324"));

        // +3 weeks
        assertThat(wr.withCatalogueCode("DPF").withDate("2020-04-22").getWeekCode().getWeekCode(), is("DPF202020"));
        assertThat(wr.withCatalogueCode("FPF").withDate("2020-04-22").getWeekCode().getWeekCode(), is("FPF202020"));
        assertThat(wr.withCatalogueCode("GPF").withDate("2020-04-22").getWeekCode().getWeekCode(), is("GPF202020"));

        // Checked in RR for the given creation date since these are not commonly used
        // Note: The rules for assigning some of these codes has changes, they now differ from the
        //       actual value in RR. (DLF, DMO, ERL, FSC, IDU, SNE)
        assertThat(wr.withCatalogueCode("DBI").withDate("2020-04-22").getWeekCode().getWeekCode(), is("DBI202019"));
        assertThat(wr.withCatalogueCode("DMO").withDate("2020-04-16").getWeekCode().getWeekCode(), is("DMO202018"));
        assertThat(wr.withCatalogueCode("BKR").withDate("2020-01-29").getWeekCode().getWeekCode(), is("BKR202006"));
        assertThat(wr.withCatalogueCode("BKX").withDate("2020-04-22").getWeekCode().getWeekCode(), is("BKX202018"));
        assertThat(wr.withCatalogueCode("DIG").withDate("2020-04-22").getWeekCode().getWeekCode(), is("DIG198507"));
        assertThat(wr.withCatalogueCode("DIS").withDate("2020-04-22").getWeekCode().getWeekCode(), is("DIS197605"));
        assertThat(wr.withCatalogueCode("FFK").withDate("2020-04-22").getWeekCode().getWeekCode(), is("FFK999999"));
        assertThat(wr.withCatalogueCode("FSC").withDate("2020-03-10").getWeekCode().getWeekCode(), is("FSC202013"));
        assertThat(wr.withCatalogueCode("FSB").withDate("2020-03-10").getWeekCode().getWeekCode(), is("FSB202013"));
        assertThat(wr.withCatalogueCode("FSF").withDate("2020-04-22").getWeekCode().getWeekCode(), is("FSF999999"));
        assertThat(wr.withCatalogueCode("HOB").withDate("2020-04-22").getWeekCode().getWeekCode(), is("HOB197300"));
        assertThat(wr.withCatalogueCode("IDU").withDate("2020-04-22").getWeekCode().getWeekCode(), is("IDU202019"));
        assertThat(wr.withCatalogueCode("OPR").withDate("2020-04-22").getWeekCode().getWeekCode(), is("OPR197601"));
        assertThat(wr.withCatalogueCode("UTI").withDate("2020-03-18").getWeekCode().getWeekCode(), is("UTI202013"));
        assertThat(wr.withCatalogueCode("SNE").withDate("2020-04-22").getWeekCode().getWeekCode(), is("SNE202019"));
        assertThat(wr.withCatalogueCode("LEK").withDate("2020-04-22").getWeekCode().getWeekCode(), is("LEK202019"));
        assertThat(wr.withCatalogueCode("MMV").withDate("2020-04-22").getWeekCode().getWeekCode(), is("MMV202019"));
        assertThat(wr.withCatalogueCode("FIV").withDate("2020-04-22").getWeekCode().getWeekCode(), is("FIV202019"));

        assertThat(wr.withCatalogueCode("ERA").withDate("2020-04-08").getWeekCode().getWeekCode(), is("ERA202018"));
        assertThat(wr.withCatalogueCode("ERE").withDate("2020-04-15").getWeekCode().getWeekCode(), is("ERE202018"));
        assertThat(wr.withCatalogueCode("ERL").withDate("2020-04-22").getWeekCode().getWeekCode(), is("ERL202019"));
        assertThat(wr.withCatalogueCode("NLL").withDate("2020-04-24").getWeekCode().getWeekCode(), is("NLL202020"));
        assertThat(wr.withCatalogueCode("NLY").withDate("2020-04-27").getWeekCode().getWeekCode(), is("NLY202020"));

        // Month number
        assertThat(wr.withCatalogueCode("PLA").withDate("2020-04-22").getWeekCode().getWeekCode(), is("PLA202004"));
        assertThat(wr.withCatalogueCode("PLN").withDate("2020-04-30").getWeekCode().getWeekCode(), is("PLN202005"));
    }

    @Test
    void TestSpecialCases() {
        WeekResolver wr = new WeekResolver(zone);

        // "Fredag 27.03.20 afsluttes DBC+BKM202015. Fredag morgen skal koden være 202017"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-03-27").getWeekCode().getWeekCode(), is("DBF202017"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-03-27").getWeekCode().getWeekCode(), is("BKM202017"));

        // DBC+BKM202016 udgår"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-03-25").getWeekCode().getWeekCode(), is("DBF202015"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-03-25").getWeekCode().getWeekCode(), is("BKM202015"));

        // Torsdag 02.04.20 afsluttes DBC+BKM202017. Torsdag morgen skal koden være 202018"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-04-09").getWeekCode().getWeekCode(), is("DBF202018"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-04-09").getWeekCode().getWeekCode(), is("BKM202018"));

        // "Der er ingen afslutning i uge 15 (påskeugen)"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-04-02").getWeekCode().getWeekCode(), is("DBF202018"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-04-02").getWeekCode().getWeekCode(), is("BKM202018"));

        // Fredag 17.04.20 afsluttes DBF+BKM2020202018. Fredag morgen skal koden være 202019"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-04-17").getWeekCode().getWeekCode(), is("DBF202019"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-04-17").getWeekCode().getWeekCode(), is("BKM202019"));

        // "1. maj. Torsdag 30.04.20 afsluttes DBC+BMK202021. Torsdag morgen skal koden være 202021"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-04-30").getWeekCode().getWeekCode(), is("DBF202021"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-04-30").getWeekCode().getWeekCode(), is("BKM202021"));

        // "Bededag. Torsdag 07.05.20 afsluttes DBC+BKM202022. Torsdag morgen d. 7/5 skal koden være 202022"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-05-07").getWeekCode().getWeekCode(), is("DBF202022"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-05-07").getWeekCode().getWeekCode(), is("BKM202022"));

        // "Kristi himmelfart. Onsdag 20.05.20 afsluttes DBF+BKM202023. Onsdag morgen skal koden være 202024"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-05-20").getWeekCode().getWeekCode(), is("DBF202024"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-05-20").getWeekCode().getWeekCode(), is("BKM202024"));

        // "Pinse. Torsdag 28.05.20 afsluttes DBF+BKM202024. Torsdag morgen skal koden være 202025"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-05-28").getWeekCode().getWeekCode(), is("DBF202025"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-05-28").getWeekCode().getWeekCode(), is("BKM202025"));

        // "Grundlovsdag. Torsdag d. 04.06.20 afsluttes DBF+BKM202026. Torsdag morgen skal koden være 202026"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-06-04").getWeekCode().getWeekCode(), is("DBF202026"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-06-04").getWeekCode().getWeekCode(), is("BKM202026"));
    }

    @Test
    void TestFirstWeekOfYear() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        assertThat(wr.withDate("2022-12-01").getWeekCode().getWeekCode(), is("BKM202250"));
        assertThat(wr.withDate("2022-12-05").getWeekCode().getWeekCode(), is("BKM202251"));
        assertThat(wr.withDate("2022-12-15").getWeekCode().getWeekCode(), is("BKM202252"));
        assertThat(wr.withDate("2022-12-19").getWeekCode().getWeekCode(), is("BKM202302"));
    }

    @Test
    void TestCurrentWeekCode() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");

        // (Pseudo)Random days in december
        assertThat(wr.withDate("2022-12-01").getCurrentWeekCode().getWeekCode(), is("BKM202248"));
        assertThat(wr.withDate("2022-12-05").getCurrentWeekCode().getWeekCode(), is("BKM202249"));
        assertThat(wr.withDate("2022-12-09").getCurrentWeekCode().getWeekCode(), is("BKM202250"));
        assertThat(wr.withDate("2022-12-15").getCurrentWeekCode().getWeekCode(), is("BKM202250"));
        assertThat(wr.withDate("2022-12-20").getCurrentWeekCode().getWeekCode(), is("BKM202251"));
        assertThat(wr.withDate("2022-12-21").getCurrentWeekCode().getWeekCode(), is("BKM202251"));
        assertThat(wr.withDate("2022-12-22").getCurrentWeekCode().getWeekCode(), is("BKM202251"));
        assertThat(wr.withDate("2022-12-23").getCurrentWeekCode().getWeekCode(), is("BKM202252"));

        // Christmas
        assertThat(wr.withDate("2022-12-24").getCurrentWeekCode().getWeekCode(), is("BKM202252"));
        assertThat(wr.withDate("2022-12-30").getCurrentWeekCode().getWeekCode(), is("BKM202301"));
        assertThat(wr.withDate("2022-12-31").getCurrentWeekCode().getWeekCode(), is("BKM202301"));
        assertThat(wr.withDate("2023-01-05").getCurrentWeekCode().getWeekCode(), is("BKM202301"));
        assertThat(wr.withDate("2023-01-06").getCurrentWeekCode().getWeekCode(), is("BKM202302"));

        // "Kristi Himmelfart"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-05-19").getCurrentWeekCode().getWeekCode(), is("DBF202021"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-05-19").getCurrentWeekCode().getWeekCode(), is("BKM202021"));
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-05-20").getCurrentWeekCode().getWeekCode(), is("DBF202021"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-05-20").getCurrentWeekCode().getWeekCode(), is("BKM202021"));
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-05-21").getCurrentWeekCode().getWeekCode(), is("DBF202021"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-05-21").getCurrentWeekCode().getWeekCode(), is("BKM202021"));
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-05-22").getCurrentWeekCode().getWeekCode(), is("DBF202022"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-05-22").getCurrentWeekCode().getWeekCode(), is("BKM202022"));
    }

    @Test
    void testCurrentWeekCodeAllWeeksOf2023() {
        WeekResolver resolverWithShiftday = new WeekResolver(zone).withCatalogueCode("BKM");
        WeekResolver resolverWithoutShiftday = new WeekResolver(zone).withCatalogueCode("ACC");

        Calendar.getInstance().setFirstDayOfWeek(Calendar.MONDAY);
        Calendar.getInstance().setMinimalDaysInFirstWeek(7);

        final ZoneId zoneId = ZoneId.of(zone);
        final Locale locale = new Locale("da", "DK");
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", locale).withZone(zoneId);
        final DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("w", locale).withZone(zoneId);

        LocalDate date = LocalDate.parse("2023-01-01", dateFormatter);

        while (date.getYear() == 2023) {
            int week = Integer.parseInt(date.format(weekFormatter));

            WeekResolverResult resultWithShiftday = resolverWithShiftday.getCurrentWeekCode(date);
            WeekResolverResult resultWithoutShiftday = resolverWithoutShiftday.getCurrentWeekCode(date);

            // For visual verification, if needed
            /*System.out.println(String.format("%02d/%02d-%d  =  %02d  ==>  %s (%02d) / %s (%02d)",
                    date.getDayOfMonth(), date.getMonth().getValue(), date.getYear(), week,
                    resultWithShiftday.getWeekCode(), resultWithShiftday.getWeekNumber(),
                    resultWithoutShiftday.getWeekCode(), resultWithoutShiftday.getWeekNumber()));*/

            // For codes with shiftday (friday), the week rolls over on friday.
            // Handle edge cases with week 52 on saturday and sunday
            if (date.getDayOfWeek().getValue() < DayOfWeek.FRIDAY.getValue()) {
                assertThat("mon-fri weekcode equals week number with shiftday", resultWithShiftday.getWeekNumber(), is(week));
            } else {
                Integer expectedWeek = week == 52 ? 1 : week + 1;
                assertThat("say-sun weekcode equals week number + 1 with shiftday", resultWithShiftday.getWeekNumber(), is(expectedWeek));
            }

            // For codes without shiftday (and no closing days) the weekcode follows the week number
            assertThat("weekcode equals week number without shiftday", resultWithoutShiftday.getWeekNumber(), is(week));

            date = date.plusDays(1);
        }
    }

    @Test
    void TestChristmas2022() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");

        assertThat(wr.withDate("2022-12-16").getWeekCode().getWeekCode(), is("BKM202302"));
        assertThat(wr.withDate("2022-12-19").getWeekCode().getWeekCode(), is("BKM202302"));
        assertThat(wr.withDate("2022-12-22").getWeekCode().getWeekCode(), is("BKM202302"));
        assertThat(wr.withDate("2022-12-26").getWeekCode().getWeekCode(), is("BKM202302"));
        assertThat(wr.withDate("2022-12-27").getWeekCode().getWeekCode(), is("BKM202302"));
        assertThat(wr.withDate("2022-12-28").getWeekCode().getWeekCode(), is("BKM202302"));
        assertThat(wr.withDate("2022-12-29").getWeekCode().getWeekCode(), is("BKM202303"));
        assertThat(wr.withDate("2022-12-30").getWeekCode().getWeekCode(), is("BKM202303"));
    }

    @Test
    void testGetYearPlan2022() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        YearPlanResult yearPlan = wr.getYearPlan(2022, true, true);

        // Check size
        assertThat(yearPlan.size(), is(53));

        // Check alignment
        assertThat(yearPlan.getRows().get(1).getColumns().get(0).getContent(), is("202202"));
        assertThat(yearPlan.getRows().get(52).getColumns().get(0).getContent(), is("202303"));

        // Check that the book cart is not placed the 23. december
        assertThat(yearPlan.getRows().get(51).getColumns().get(0).getContent(), is("202302"));         // Week code
        assertThat(yearPlan.getRows().get(51).getColumns().get(1).getContent(), is("\"2022-12-16\"")); // Weekcode first
        assertThat(yearPlan.getRows().get(51).getColumns().get(2).getContent().contains("2022-12-28"), is(true)); // Weekcode last
        assertThat(yearPlan.getRows().get(51).getColumns().get(2).getContent().contains("ONSDAG"), is(true)); // Weekcode last
        assertThat(yearPlan.getRows().get(51).getColumns().get(3).getContent().contains("2022-12-29"), is(true)); // Shiftday
        assertThat(yearPlan.getRows().get(51).getColumns().get(3).getContent().contains("TORSDAG"), is(true)); // Weekcode last
        assertThat(yearPlan.getRows().get(51).getColumns().get(4).getContent().contains("2022-12-30"), is(true)); // Book cart
        assertThat(yearPlan.getRows().get(51).getColumns().get(4).getContent().contains("FREDAG"), is(true)); // Weekcode last
        assertThat(yearPlan.getRows().get(51).getColumns().get(5).getContent(), is("\"2022-12-30\"")); // Proof starts
        assertThat(yearPlan.getRows().get(51).getColumns().get(6).getContent(), is("\"2023-01-02\"")); // Proof
        assertThat(yearPlan.getRows().get(51).getColumns().get(7).getContent(), is("\"2023-01-03\"")); // Proof ends
        assertThat(yearPlan.getRows().get(51).getColumns().get(8).getContent().contains("2023-01-04"), is(true)); // BKM-red.
        assertThat(yearPlan.getRows().get(51).getColumns().get(9).getContent(), is("\"2023-01-06\"")); // Publish
        assertThat(yearPlan.getRows().get(51).getColumns().get(10).getContent(), is("51 + 52"));       // Week number
    }

    @Test
    void testGetYearPlan2023() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        YearPlanResult yearPlan = wr.getYearPlan(2023, true, true);

        // Check size
        assertThat(yearPlan.size(), is(53));

        // Check alignment
        assertThat(yearPlan.getRows().get(1).getColumns().get(0).getContent(), is("202302"));
        assertThat(yearPlan.getRows().get(52).getColumns().get(0).getContent(), is("202403"));

        // Check beginning of the year
        assertThat(yearPlan.getRows().get(2).getColumns().get(1).getContent().contains("2022-12-29"), is(true)); // week 2, first assignment
        assertThat(yearPlan.getRows().get(2).getColumns().get(1).getContent().contains("TORSDAG"), is(true));        // week 2, first assignment

        // Check easter
        assertThat(yearPlan.getRows().get(14).getColumns().get(2).getContent().contains("ONSDAG"), is(true));     // week 13, last assignment
        assertThat(yearPlan.getRows().get(14).getColumns().get(2).getContent().contains("2023-03-29"), is(true)); // week 13, last assignment
        assertThat(yearPlan.getRows().get(14).getColumns().get(3).getContent().contains("TORSDAG"), is(true));    // week 13, shiftday
        assertThat(yearPlan.getRows().get(14).getColumns().get(3).getContent().contains("2023-03-30"), is(true)); // week 13, shiftday
        assertThat(yearPlan.getRows().get(15).getColumns().get(1).getContent().contains("TORSDAG"), is(true));    // week 15, first assignment
        assertThat(yearPlan.getRows().get(15).getColumns().get(1).getContent().contains("2023-03-30"), is(true)); // week 15, first assignment
        assertThat(yearPlan.getRows().get(15).getColumns().get(2).getContent(), is("\"2023-04-13\""));            // week 15, last assignment
        assertThat(yearPlan.getRows().get(15).getColumns().get(3).getContent(), is("\"2023-04-14\""));            // week 15, shiftday assignment

        // Week before may 1st.
        assertThat(yearPlan.getRows().get(17).getColumns().get(1).getContent(), is("\"2023-04-21\""));            // week 17, first assignment
        assertThat(yearPlan.getRows().get(17).getColumns().get(2).getContent().contains("ONSDAG"), is(true));     // week 17, last assignment
        assertThat(yearPlan.getRows().get(17).getColumns().get(2).getContent().contains("2023-04-26"), is(true)); // week 17, last assignment
        assertThat(yearPlan.getRows().get(17).getColumns().get(3).getContent().contains("TORSDAG"), is(true));    // week 17, shiftday
        assertThat(yearPlan.getRows().get(17).getColumns().get(3).getContent().contains("2023-04-27"), is(true)); // week 17, shiftday

        // Ascension day
        assertThat(yearPlan.getRows().get(20).getColumns().get(2).getContent().contains("TIRSDAG"), is(true));    // week 20, last assignment
        assertThat(yearPlan.getRows().get(20).getColumns().get(2).getContent().contains("2023-05-16"), is(true)); // week 20, last assignment
        assertThat(yearPlan.getRows().get(20).getColumns().get(3).getContent().contains("ONSDAG"), is(true));     // week 20, shiftday
        assertThat(yearPlan.getRows().get(20).getColumns().get(3).getContent().contains("2023-05-17"), is(true)); // week 20, shiftday

        // Pentecost
        assertThat(yearPlan.getRows().get(21).getColumns().get(1).getContent().contains("ONSDAG"), is(true));     // week 21, first assignment
        assertThat(yearPlan.getRows().get(21).getColumns().get(1).getContent().contains("2023-05-17"), is(true)); // week 21, first assignment
        assertThat(yearPlan.getRows().get(21).getColumns().get(2).getContent().contains("ONSDAG"), is(true));     // week 21, last assignment
        assertThat(yearPlan.getRows().get(21).getColumns().get(2).getContent().contains("2023-05-24"), is(true)); // week 21, last assignment
        assertThat(yearPlan.getRows().get(21).getColumns().get(3).getContent().contains("TORSDAG"), is(true));    // week 21, shiftday
        assertThat(yearPlan.getRows().get(21).getColumns().get(3).getContent().contains("2023-05-25"), is(true)); // week 21, shiftday

        // Week before "Grundlovsdag"
        assertThat(yearPlan.getRows().get(22).getColumns().get(1).getContent().contains("TORSDAG"), is(true));    // week 22, first assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(1).getContent().contains("2023-05-25"), is(true)); // week 22, first assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(2).getContent().contains("ONSDAG"), is(true));     // week 22, last assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(2).getContent().contains("2023-05-31"), is(true)); // week 22, last assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(3).getContent().contains("TORSDAG"), is(true));    // week 22, shiftday
        assertThat(yearPlan.getRows().get(22).getColumns().get(3).getContent().contains("2023-06-01"), is(true)); // week 22, shiftday

        // Christmas and year change
        assertThat(yearPlan.getRows().get(50).getColumns().get(0).getContent(), is("202352"));          // Week code
        assertThat(yearPlan.getRows().get(50).getColumns().get(1).getContent(), is("\"2023-12-08\""));  // Weekcode first
        assertThat(yearPlan.getRows().get(50).getColumns().get(2).getContent(), is("\"2023-12-14\""));  // Weekcode last
        assertThat(yearPlan.getRows().get(50).getColumns().get(3).getContent(), is("\"2023-12-15\""));  // Shiftday
        assertThat(yearPlan.getRows().get(50).getColumns().get(4).getContent(), is("\"2023-12-18\""));  // Book cart
        assertThat(yearPlan.getRows().get(50).getColumns().get(5).getContent(), is("\"2023-12-18\""));  // Proof starts
        assertThat(yearPlan.getRows().get(50).getColumns().get(6).getContent(), is("\"2023-12-19\""));  // Proof
        assertThat(yearPlan.getRows().get(50).getColumns().get(7).getContent(), is("\"2023-12-19\""));  // Proof ends
        assertThat(yearPlan.getRows().get(50).getColumns().get(8).getContent(), is("\"2023-12-20\""));  // BKM-red.
        assertThat(yearPlan.getRows().get(50).getColumns().get(9).getContent(), is("\"2023-12-22\""));  // Publish
        assertThat(yearPlan.getRows().get(50).getColumns().get(10).getContent(), is("50"));             // Week number
        // ...
        assertThat(yearPlan.getRows().get(51).getColumns().get(0).getContent(), is("202402"));                    // Week code
        assertThat(yearPlan.getRows().get(51).getColumns().get(1).getContent().contains("2023-12-15"), is(true)); // Weekcode first
        assertThat(yearPlan.getRows().get(51).getColumns().get(2).getContent().contains("ONSDAG"), is(true));     // Weekcode last, name of day
        assertThat(yearPlan.getRows().get(51).getColumns().get(2).getContent().contains("2023-12-20"), is(true)); // Weekcode last
        assertThat(yearPlan.getRows().get(51).getColumns().get(3).getContent().contains("TORSDAG"), is(true));    // Shiftday, name of day
        assertThat(yearPlan.getRows().get(51).getColumns().get(3).getContent().contains("2023-12-21"), is(true)); // Shiftday
        assertThat(yearPlan.getRows().get(51).getColumns().get(4).getContent().contains("FREDAG"), is(true));     // Book cart, name of day
        assertThat(yearPlan.getRows().get(51).getColumns().get(4).getContent().contains("2023-12-22"), is(true)); // Book cart
        assertThat(yearPlan.getRows().get(51).getColumns().get(5).getContent(), is("\"2023-12-22\""));            // Proof starts
        assertThat(yearPlan.getRows().get(51).getColumns().get(6).getContent(), is("\"2024-01-02\""));            // Proof
        assertThat(yearPlan.getRows().get(51).getColumns().get(7).getContent(), is("\"2024-01-02\""));            // Proof ends
        assertThat(yearPlan.getRows().get(51).getColumns().get(8).getContent(), is("\"2024-01-03\""));            // BKM-red.
        assertThat(yearPlan.getRows().get(51).getColumns().get(9).getContent(), is("\"2024-01-05\""));            // Publish
        assertThat(yearPlan.getRows().get(51).getColumns().get(10).getContent(), is("51"));                       // Week number
        // ...
        assertThat(yearPlan.getRows().get(52).getColumns().get(0).getContent(), is("202403"));                    // Week code
        assertThat(yearPlan.getRows().get(52).getColumns().get(1).getContent().contains("TORSDAG"), is(true));    // Weekcode first, name of day
        assertThat(yearPlan.getRows().get(52).getColumns().get(1).getContent().contains("2023-12-21"), is(true)); // Weekcode first
        assertThat(yearPlan.getRows().get(52).getColumns().get(2).getContent(), is("\"2024-01-04\""));            // Weekcode last
        assertThat(yearPlan.getRows().get(52).getColumns().get(3).getContent(), is("\"2024-01-05\""));            // Shiftday
        assertThat(yearPlan.getRows().get(52).getColumns().get(4).getContent(), is("\"2024-01-08\""));            // Book cart
        assertThat(yearPlan.getRows().get(52).getColumns().get(5).getContent(), is("\"2024-01-08\""));            // Proof starts
        assertThat(yearPlan.getRows().get(52).getColumns().get(6).getContent(), is("\"2024-01-09\""));            // Proof
        assertThat(yearPlan.getRows().get(52).getColumns().get(7).getContent(), is("\"2024-01-09\""));            // Proof ends
        assertThat(yearPlan.getRows().get(52).getColumns().get(8).getContent(), is("\"2024-01-10\""));            // BKM-red.
        assertThat(yearPlan.getRows().get(52).getColumns().get(9).getContent(), is("\"2024-01-12\""));            // Publish
        assertThat(yearPlan.getRows().get(52).getColumns().get(10).getContent(), is("52 + 1"));                   // Week number

        // Weeks without production
        assertThat(yearPlan.getRows().get(2).getColumns().get(1).getContent().contains("2022-12-29"), is(true));  // week 52 previous year
        assertThat(yearPlan.getRows().get(2).getColumns().get(0).getContent(), is("202303"));          // week 1 previous year
        assertThat(yearPlan.getRows().get(3).getColumns().get(1).getContent(), is("\"2023-01-06\""));  // week 1
        assertThat(yearPlan.getRows().get(3).getColumns().get(0).getContent(), is("202304"));          // week 1
        assertThat(yearPlan.getRows().get(14).getColumns().get(0).getContent(), is("202316"));         // week 13
        assertThat(yearPlan.getRows().get(51).getColumns().get(1).getContent(), is("\"2023-12-15\"")); // week 52
        assertThat(yearPlan.getRows().get(51).getColumns().get(0).getContent(), is("202402"));         // week 52
        yearPlan.getRows().forEach(r -> {
            if (!Set.of("202303", "202304", "202316", "202403").contains(r.getColumns().get(0).getContent())) {
                assertThat(r.getColumns().get(1).getContent().isEmpty(), is(false));
            }
        });
    }

    @Test
    void testGetYearPlanWithWeek2024() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        YearPlanResult yearPlan = wr.getYearPlan(2024, true, true);

        // Check size
        assertThat(yearPlan.size(), is(51));

        // Check alignment
        assertThat(yearPlan.getRows().get(1).getColumns().get(0).getContent(), is("202403"));
        assertThat(yearPlan.getRows().get(50).getColumns().get(0).getContent(), is("202503"));

        // Check proof and publish in the Easter week
        assertThat(yearPlan.getRows().get(12).getColumns().get(0).getContent(), is("202415"));                    // Week code
        assertThat(yearPlan.getRows().get(12).getColumns().get(1).getContent().contains("2024-03-15"), is(true)); // Weekcode first
        assertThat(yearPlan.getRows().get(12).getColumns().get(2).getContent().contains("ONSDAG"), is(true));     // Weekcode last, name of day
        assertThat(yearPlan.getRows().get(12).getColumns().get(2).getContent().contains("2024-03-20"), is(true)); // Weekcode last
        assertThat(yearPlan.getRows().get(12).getColumns().get(3).getContent().contains("TORSDAG"), is(true));    // Shiftday, name of day
        assertThat(yearPlan.getRows().get(12).getColumns().get(3).getContent().contains("2024-03-21"), is(true)); // Shiftday
        assertThat(yearPlan.getRows().get(12).getColumns().get(4).getContent().contains("FREDAG"), is(true));     // Book cart, name of day
        assertThat(yearPlan.getRows().get(12).getColumns().get(4).getContent().contains("2024-03-22"), is(true)); // Book cart
        assertThat(yearPlan.getRows().get(12).getColumns().get(5).getContent(), is("\"2024-03-22\""));            // Proof starts
        assertThat(yearPlan.getRows().get(12).getColumns().get(6).getContent(), is("\"2024-04-02\""));            // Proof
        assertThat(yearPlan.getRows().get(12).getColumns().get(7).getContent(), is("\"2024-04-02\""));            // Proof ends
        assertThat(yearPlan.getRows().get(12).getColumns().get(8).getContent(), is("\"2024-04-03\""));            // BKM-red.
        assertThat(yearPlan.getRows().get(12).getColumns().get(9).getContent(), is("\"2024-04-05\""));            // Publish
        assertThat(yearPlan.getRows().get(12).getColumns().get(10).getContent(), is("12"));                       // Week number

        // Check proof and publish just before May 1st.
        assertThat(yearPlan.getRows().get(16).getColumns().get(0).getContent(), is("202419"));                    // Week code
        assertThat(yearPlan.getRows().get(16).getColumns().get(1).getContent(), is("\"2024-04-19\""));            // Weekcode first
        assertThat(yearPlan.getRows().get(16).getColumns().get(2).getContent(), is("\"2024-04-25\""));            // Weekcode last
        assertThat(yearPlan.getRows().get(16).getColumns().get(3).getContent(), is("\"2024-04-26\""));            // Shiftday
        assertThat(yearPlan.getRows().get(16).getColumns().get(4).getContent(), is("\"2024-04-29\""));            // Book cart
        assertThat(yearPlan.getRows().get(16).getColumns().get(5).getContent(), is("\"2024-04-29\""));            // Proof starts
        assertThat(yearPlan.getRows().get(16).getColumns().get(6).getContent(), is("\"2024-04-30\""));            // Proof
        assertThat(yearPlan.getRows().get(16).getColumns().get(7).getContent(), is("\"2024-04-30\""));            // Proof ends
        assertThat(yearPlan.getRows().get(16).getColumns().get(8).getContent().contains("TORSDAG"), is(true));    // BKM-red, name of day
        assertThat(yearPlan.getRows().get(16).getColumns().get(8).getContent().contains("2024-05-02"), is(true)); // BKM-red.
        assertThat(yearPlan.getRows().get(16).getColumns().get(9).getContent(), is("\"2024-05-03\""));            // Publish
        assertThat(yearPlan.getRows().get(16).getColumns().get(10).getContent(), is("17"));                       // Week number

        // Check proof and publish around ascension Day
        assertThat(yearPlan.getRows().get(17).getColumns().get(0).getContent(), is("202420"));                    // Week code
        assertThat(yearPlan.getRows().get(17).getColumns().get(1).getContent(), is("\"2024-04-26\""));            // Weekcode first
        assertThat(yearPlan.getRows().get(17).getColumns().get(2).getContent(), is("\"2024-05-02\""));            // Weekcode last
        assertThat(yearPlan.getRows().get(17).getColumns().get(3).getContent(), is("\"2024-05-03\""));            // Shiftday
        assertThat(yearPlan.getRows().get(17).getColumns().get(4).getContent(), is("\"2024-05-06\""));            // Book cart
        assertThat(yearPlan.getRows().get(17).getColumns().get(5).getContent(), is("\"2024-05-06\""));            // Proof starts
        assertThat(yearPlan.getRows().get(17).getColumns().get(6).getContent(), is("\"2024-05-07\""));            // Proof
        assertThat(yearPlan.getRows().get(17).getColumns().get(7).getContent(), is("\"2024-05-07\""));            // Proof ends
        assertThat(yearPlan.getRows().get(17).getColumns().get(8).getContent(), is("\"2024-05-08\""));            // BKM-red.
        assertThat(yearPlan.getRows().get(17).getColumns().get(9).getContent(), is("\"2024-05-10\""));            // Publish
        assertThat(yearPlan.getRows().get(17).getColumns().get(10).getContent(), is("18"));                       // Week number

        // Check proof and publish just before Constitution day
        assertThat(yearPlan.getRows().get(21).getColumns().get(0).getContent(), is("202424"));                    // Week code
        assertThat(yearPlan.getRows().get(21).getColumns().get(1).getContent(), is("\"2024-05-24\""));            // Weekcode first
        assertThat(yearPlan.getRows().get(21).getColumns().get(2).getContent(), is("\"2024-05-30\""));            // Weekcode last
        assertThat(yearPlan.getRows().get(21).getColumns().get(3).getContent(), is("\"2024-05-31\""));            // Shiftday
        assertThat(yearPlan.getRows().get(21).getColumns().get(4).getContent(), is("\"2024-06-03\""));            // Book cart
        assertThat(yearPlan.getRows().get(21).getColumns().get(5).getContent(), is("\"2024-06-03\""));            // Proof starts
        assertThat(yearPlan.getRows().get(21).getColumns().get(6).getContent(), is("\"2024-06-04\""));            // Proof
        assertThat(yearPlan.getRows().get(21).getColumns().get(7).getContent(), is("\"2024-06-04\""));            // Proof ends
        assertThat(yearPlan.getRows().get(21).getColumns().get(8).getContent().contains("TORSDAG"), is(true));    // BKM-red, name of day
        assertThat(yearPlan.getRows().get(21).getColumns().get(8).getContent().contains("2024-06-06"), is(true)); // BKM-red.
        assertThat(yearPlan.getRows().get(21).getColumns().get(9).getContent(), is("\"2024-06-07\""));            // Publish
        assertThat(yearPlan.getRows().get(21).getColumns().get(10).getContent(), is("22"));                       // Week number

        // Check proof and publish just around the year change
        assertThat(yearPlan.getRows().get(49).getColumns().get(0).getContent(), is("202452"));                    // Week code
        assertThat(yearPlan.getRows().get(49).getColumns().get(1).getContent(), is("\"2024-12-06\""));            // Weekcode first
        assertThat(yearPlan.getRows().get(49).getColumns().get(2).getContent(), is("\"2024-12-12\""));            // Weekcode last
        assertThat(yearPlan.getRows().get(49).getColumns().get(3).getContent(), is("\"2024-12-13\""));            // Shiftday
        assertThat(yearPlan.getRows().get(49).getColumns().get(4).getContent(), is("\"2024-12-16\""));            // Book cart
        assertThat(yearPlan.getRows().get(49).getColumns().get(5).getContent(), is("\"2024-12-16\""));            // Proof starts
        assertThat(yearPlan.getRows().get(49).getColumns().get(6).getContent(), is("\"2024-12-17\""));            // Proof
        assertThat(yearPlan.getRows().get(49).getColumns().get(7).getContent(), is("\"2024-12-17\""));            // Proof ends
        assertThat(yearPlan.getRows().get(49).getColumns().get(8).getContent(), is("\"2024-12-18\""));            // BKM-red.
        assertThat(yearPlan.getRows().get(49).getColumns().get(9).getContent(), is("\"2024-12-20\""));            // Publish
        assertThat(yearPlan.getRows().get(49).getColumns().get(10).getContent(), is("50"));                       // Week number
        // --
        assertThat(yearPlan.getRows().get(50).getColumns().get(0).getContent(), is("202503"));                    // Week code
        assertThat(yearPlan.getRows().get(50).getColumns().get(1).getContent(), is("\"2024-12-13\""));            // Weekcode first
        assertThat(yearPlan.getRows().get(50).getColumns().get(2).getContent(), is("\"2025-01-02\""));            // Weekcode last
        assertThat(yearPlan.getRows().get(50).getColumns().get(3).getContent(), is("\"2025-01-03\""));            // Shiftday
        assertThat(yearPlan.getRows().get(50).getColumns().get(4).getContent(), is("\"2025-01-06\""));            // Book cart
        assertThat(yearPlan.getRows().get(50).getColumns().get(5).getContent(), is("\"2025-01-06\""));            // Proof starts
        assertThat(yearPlan.getRows().get(50).getColumns().get(6).getContent(), is("\"2025-01-07\""));            // Proof
        assertThat(yearPlan.getRows().get(50).getColumns().get(7).getContent(), is("\"2025-01-07\""));            // Proof ends
        assertThat(yearPlan.getRows().get(50).getColumns().get(8).getContent(), is("\"2025-01-08\""));            // BKM-red.
        assertThat(yearPlan.getRows().get(50).getColumns().get(9).getContent(), is("\"2025-01-10\""));            // Publish
        assertThat(yearPlan.getRows().get(50).getColumns().get(10).getContent(), is("51 + 52 + 1"));              // Week number
    }

    @Test
    void testGetYearPlanWithWeek2025() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        YearPlanResult yearPlan = wr.getYearPlan(2025, true, true);

        // Check size
        assertThat(yearPlan.size(), is(52));

        // Check alignment
        assertThat(yearPlan.getRows().get(1).getColumns().get(0).getContent(), is("202503"));
        assertThat(yearPlan.getRows().get(51).getColumns().get(0).getContent(), is("202604"));
    }

    @Test
    void test2022To2023() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        LocalDate monday = LocalDate.parse("2022-12-05", DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<WeekResolverResult> results = new ArrayList<>();

        results.add(wr.getWeekCode(monday));                         // 05/12-2022 = BKM202251
        results.add(wr.getWeekCode(monday.plusWeeks(1))); // 12/12-2022 = BKM202252
        results.add(wr.getWeekCode(monday.plusWeeks(2))); // 19/12-2022 = BKM202302
        results.add(wr.getWeekCode(monday.plusWeeks(3))); // 26/12-2022 = BKM202302
        results.add(wr.getWeekCode(monday.plusWeeks(4))); // 02/01-2023 = BKM202303
        results.add(wr.getWeekCode(monday.plusWeeks(5))); // 09/01-2023 = BKM202304
        results.add(wr.getWeekCode(monday.plusWeeks(6))); // 16/01-2023 = BKM202305
        assertThat(results.size(), is(7));

        assertThat(results.get(0).getWeekCode(), is("BKM202251"));
        assertThat(wr.rowContentFromDate(results.get(0).getDescription().getWeekCodeFirst()).getContent(), is("2022-12-02"));
        assertThat(wr.rowContentFromDate(results.get(0).getDescription().getWeekCodeLast()).getContent(), is("2022-12-08"));
        assertThat(wr.rowContentFromDate(results.get(0).getDescription().getShiftDay()).getContent(), is("2022-12-09"));

        assertThat(results.get(1).getWeekCode(), is("BKM202252"));
        assertThat(wr.rowContentFromDate(results.get(1).getDescription().getWeekCodeFirst()).getContent(), is("2022-12-09"));
        assertThat(wr.rowContentFromDate(results.get(1).getDescription().getWeekCodeLast()).getContent(), is("2022-12-15"));
        assertThat(wr.rowContentFromDate(results.get(1).getDescription().getShiftDay()).getContent(), is("2022-12-16"));

        assertThat(results.get(2).getWeekCode(), is("BKM202302"));
        assertThat(wr.rowContentFromDate(results.get(2).getDescription().getWeekCodeFirst()).getContent(), is("2022-12-16"));
        assertThat(wr.rowContentFromDate(results.get(2).getDescription().getWeekCodeLast()).getContent(), is("2022-12-21"));
        assertThat(wr.rowContentFromDate(results.get(2).getDescription().getShiftDay()).getContent(), is("2022-12-22"));

        assertThat(results.get(2).getWeekCode(), is("BKM202302"));
        assertThat(wr.rowContentFromDate(results.get(2).getDescription().getWeekCodeFirst()).getContent(), is("2022-12-16"));
        assertThat(wr.rowContentFromDate(results.get(2).getDescription().getWeekCodeLast()).getContent(), is("2022-12-21"));
        assertThat(wr.rowContentFromDate(results.get(2).getDescription().getShiftDay()).getContent(), is("2022-12-22"));

        assertThat(results.get(3).getWeekCode(), is("BKM202302"));
        assertThat(wr.rowContentFromDate(results.get(3).getDescription().getWeekCodeFirst()).getContent(), is("2022-12-22"));
        assertThat(wr.rowContentFromDate(results.get(3).getDescription().getWeekCodeLast()).getContent(), is("2022-12-28"));
        assertThat(wr.rowContentFromDate(results.get(3).getDescription().getShiftDay()).getContent(), is("2022-12-29"));

        assertThat(results.get(5).getWeekCode(), is("BKM202304"));
        assertThat(wr.rowContentFromDate(results.get(5).getDescription().getWeekCodeFirst()).getContent(), is("2023-01-06"));
        assertThat(wr.rowContentFromDate(results.get(5).getDescription().getWeekCodeLast()).getContent(), is("2023-01-12"));
        assertThat(wr.rowContentFromDate(results.get(5).getDescription().getShiftDay()).getContent(), is("2023-01-13"));

        assertThat(results.get(6).getWeekCode(), is("BKM202305"));
        assertThat(wr.rowContentFromDate(results.get(6).getDescription().getWeekCodeFirst()).getContent(), is("2023-01-13"));
        assertThat(wr.rowContentFromDate(results.get(6).getDescription().getWeekCodeLast()).getContent(), is("2023-01-19"));
        assertThat(wr.rowContentFromDate(results.get(6).getDescription().getShiftDay()).getContent(), is("2023-01-20"));
    }

    @Test
    void TestStoreBededag() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");

        // 2023
        assertThat(wr.withDate("2023-05-03").getWeekCode().getWeekCode(), is("BKM202320"));
        assertThat(wr.withDate("2023-05-04").getWeekCode().getWeekCode(), is("BKM202321"));
        assertThat(wr.withDate("2023-05-05").getWeekCode().getWeekCode(), is("BKM202321"));

        // Although Store Bededay has been cancelled by the almighty deity the prime minister,
        // the days formerly known as 'Store Bededag' often appears so that the production date
        // two weeks down the road (BKM = two weeks shift) coincides with other non-production days
        // or the regular shiftday.

        // 2024 - Friday = shiftday
        assertThat(wr.withDate("2024-04-24").getWeekCode().getWeekCode(), is("BKM202419"));
        assertThat(wr.withDate("2024-04-25").getWeekCode().getWeekCode(), is("BKM202419"));
        assertThat(wr.withDate("2024-04-26").getWeekCode().getWeekCode(), is("BKM202420"));

        // 2025 - no Store Bededag, but two weeks ahead is ascension day
        assertThat(wr.withDate("2025-05-14").getWeekCode().getWeekCode(), is("BKM202522"));
        assertThat(wr.withDate("2025-05-15").getWeekCode().getWeekCode(), is("BKM202522"));
        assertThat(wr.withDate("2025-05-16").getWeekCode().getWeekCode(), is("BKM202523"));

        // 2026 - no Store Bededag, but two weeks ahead is may 1.st.
        assertThat(wr.withDate("2026-04-29").getWeekCode().getWeekCode(), is("BKM202620"));
        assertThat(wr.withDate("2026-04-30").getWeekCode().getWeekCode(), is("BKM202621"));
        assertThat(wr.withDate("2026-05-01").getWeekCode().getWeekCode(), is("BKM202621"));

        // Catalogue code DPF, which has 3 weeks shift, will show the cancelled Store Bededag
        // for some years, but not all
        wr.withCatalogueCode("DPF");

        // 2023
        assertThat(wr.withDate("2023-05-03").getWeekCode().getWeekCode(), is("DPF202321"));
        assertThat(wr.withDate("2023-05-04").getWeekCode().getWeekCode(), is("DPF202322"));
        assertThat(wr.withDate("2023-05-05").getWeekCode().getWeekCode(), is("DPF202322"));

        // 2024
        assertThat(wr.withDate("2024-04-24").getWeekCode().getWeekCode(), is("DPF202420"));
        assertThat(wr.withDate("2024-04-25").getWeekCode().getWeekCode(), is("DPF202420"));
        assertThat(wr.withDate("2024-04-26").getWeekCode().getWeekCode(), is("DPF202421"));

        // 2025 - Day before 'Grundlovsday' and related pinched friday
        assertThat(wr.withDate("2025-05-14").getWeekCode().getWeekCode(), is("DPF202523"));
        assertThat(wr.withDate("2025-05-15").getWeekCode().getWeekCode(), is("DPF202523"));
        assertThat(wr.withDate("2025-05-16").getWeekCode().getWeekCode(), is("DPF202524"));

        // 2026 - thursday is shiftday due to upcomming ascension
        assertThat(wr.withDate("2026-04-29").getWeekCode().getWeekCode(), is("DPF202621"));
        assertThat(wr.withDate("2026-04-30").getWeekCode().getWeekCode(), is("DPF202622"));
        assertThat(wr.withDate("2026-05-01").getWeekCode().getWeekCode(), is("DPF202622"));
    }

    @Test
    void TestEaster2023() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");

        // Week before the Easter week
        assertThat(wr.withDate("2023-03-24").getWeekCode().getWeekCode(), is("BKM202316"));
        assertThat(wr.withDate("2023-03-25").getWeekCode().getWeekCode(), is("BKM202316"));
        assertThat(wr.withDate("2023-03-26").getWeekCode().getWeekCode(), is("BKM202316"));
        assertThat(wr.withDate("2023-03-27").getWeekCode().getWeekCode(), is("BKM202316"));
        assertThat(wr.withDate("2023-03-28").getWeekCode().getWeekCode(), is("BKM202316"));
        assertThat(wr.withDate("2023-03-29").getWeekCode().getWeekCode(), is("BKM202316"));
        assertThat(wr.withDate("2023-03-29").getWeekCode().getWeekCode(), is("BKM202316"));

        assertThat(wr.withDate("2023-03-30").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-03-31").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-01").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-02").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-03").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-04").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-05").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-06").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-07").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-08").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-09").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-10").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-11").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-12").getWeekCode().getWeekCode(), is("BKM202317"));
        assertThat(wr.withDate("2023-04-13").getWeekCode().getWeekCode(), is("BKM202317"));

        assertThat(wr.withDate("2023-04-14").getWeekCode().getWeekCode(), is("BKM202318"));
    }

    @Test
    void TestYearChange2023To2024() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");

        assertThat(wr.withDate("2023-12-01").getWeekCode().getWeekCode(), is("BKM202351"));
        assertThat(wr.withDate("2023-12-08").getWeekCode().getWeekCode(), is("BKM202352"));
        assertThat(wr.withDate("2023-12-15").getWeekCode().getWeekCode(), is("BKM202402"));
        assertThat(wr.withDate("2023-12-20").getWeekCode().getWeekCode(), is("BKM202402"));
        assertThat(wr.withDate("2023-12-21").getWeekCode().getWeekCode(), is("BKM202403"));
        assertThat(wr.withDate("2023-12-22").getWeekCode().getWeekCode(), is("BKM202403"));
        assertThat(wr.withDate("2024-01-04").getWeekCode().getWeekCode(), is("BKM202403"));
        assertThat(wr.withDate("2024-01-05").getWeekCode().getWeekCode(), is("BKM202404"));
        assertThat(wr.withDate("2024-01-06").getWeekCode().getWeekCode(), is("BKM202404"));
    }

    @Test
    void testYearPlanAndWeekCodeCrossCheck() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");

        // Check year plan and code for the previous year, up to 2 years into the future.
        // This (almost certainly) ensures that an automatic build breaks at least a year
        // before someone is going to use a year plan or codes from a given year.
        int thisYear = LocalDate.now().getYear();
        IntStream years = IntStream.range(thisYear - 1, thisYear + 3);

        years.forEach(year -> {
            YearPlanResult yearPlan = wr.getYearPlan(year, true, true);

            // Check size
            assertThat(yearPlan.size(), anyOf(is(51), is(52), is(53)));

            // Check alignment
            assertThat(yearPlan.getRows().get(1).getColumns().get(0).getContent(), anyOf(is(String.format("%d02", year)), is(String.format("%d03", year)), is(String.format("%d04", year))));
            assertThat(yearPlan.getRows().get(yearPlan.size() - 1).getColumns().get(0).getContent(), anyOf(is(String.format("%d03", year + 1)), is(String.format("%d04", year + 1))));

            // Check all days and their weekcodes
            yearPlan.getRows().stream().skip(1).forEach(row -> {

                // Get year plan code, first and last date for each week
                String code = row.getColumns().get(0).getContent();
                String first = row.getColumns().get(1).getContent();
                String last = row.getColumns().get(2).getContent();

                assertThat(code == null || code.isEmpty(), is(false));
                assertThat(first == null || first.isEmpty(), is(false));
                if (last == null || last.isEmpty()) {
                    return;
                }

                // Clean first and last (they may have added day names due to abnormal weeks)
                first = first.replaceAll("\"", "");
                first = first.substring(first.length() - 10);
                last = last.replaceAll("\"", "");
                last = last.substring(last.length() - 10);

                // Convert to dates
                LocalDate firstDate = wr.fromString(first);
                LocalDate lastDate = wr.fromString(last);

                // Check all dates between first and last, and make sure they get the code indicated by the year plan
                LocalDate date = firstDate;
                while (date.isBefore(lastDate) || date.isEqual(lastDate)) {
                    WeekResolverResult result = wr.getWeekCode(date);
                    assertThat(result.getWeekCode(), is(result.getCatalogueCode() + code));
                    date = date.plusDays(1);
                }
            });
        });
    }

    @Test
    void Test2025SkipWeek02() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");

        assertThat(wr.withDate("2024-12-12").getWeekCode().getWeekCode(), is("BKM202452"));
        assertThat(wr.withDate("2024-12-13").getWeekCode().getWeekCode(), is("BKM202503"));

        assertThat(wr.withDate("2024-12-16").getWeekCode().getWeekCode(), is("BKM202503"));
        assertThat(wr.withDate("2024-12-23").getWeekCode().getWeekCode(), is("BKM202503"));
        assertThat(wr.withDate("2024-12-30").getWeekCode().getWeekCode(), is("BKM202503"));

        assertThat(wr.withDate("2025-01-02").getWeekCode().getWeekCode(), is("BKM202503"));
        assertThat(wr.withDate("2025-01-03").getWeekCode().getWeekCode(), is("BKM202504"));
        assertThat(wr.withDate("2025-01-06").getWeekCode().getWeekCode(), is("BKM202504"));
    }

    @Test
    void TestCatalogueCodeDAN_DAR_SBA_KBA() {
        WeekResolver wr = new WeekResolver(zone);

        Set.of("DAN", "DAR", "SBA", "KBA").forEach(code -> {
            assertThat(wr.withCatalogueCode(code).withDate("2023-04-23").getWeekCode().getWeekCode(), is(code + "202317"));
            assertThat(wr.withCatalogueCode(code).withDate("2023-04-24").getWeekCode().getWeekCode(), is(code + "202318"));
            assertThat(wr.withCatalogueCode(code).withDate("2023-04-25").getWeekCode().getWeekCode(), is(code + "202318"));
            assertThat(wr.withCatalogueCode(code).withDate("2023-04-26").getWeekCode().getWeekCode(), is(code + "202318"));
            assertThat(wr.withCatalogueCode(code).withDate("2023-04-27").getWeekCode().getWeekCode(), is(code + "202318"));
            assertThat(wr.withCatalogueCode(code).withDate("2023-04-28").getWeekCode().getWeekCode(), is(code + "202318"));
            assertThat(wr.withCatalogueCode(code).withDate("2023-04-29").getWeekCode().getWeekCode(), is(code + "202318"));
            assertThat(wr.withCatalogueCode(code).withDate("2023-04-30").getWeekCode().getWeekCode(), is(code + "202318"));
            assertThat(wr.withCatalogueCode(code).withDate("2023-05-01").getWeekCode().getWeekCode(), is(code + "202319"));
        });
    }

    @Test
    void TestCatalogueCodeARK() {
        WeekResolver wr = new WeekResolver(zone);

        assertThat(wr.withCatalogueCode("ARK").withDate("2023-04-23").getWeekCode().getWeekCode(), is("ARK202317"));
        assertThat(wr.withCatalogueCode("ARK").withDate("2023-04-24").getWeekCode().getWeekCode(), is("ARK202318"));
        assertThat(wr.withCatalogueCode("ARK").withDate("2023-04-25").getWeekCode().getWeekCode(), is("ARK202318"));
        assertThat(wr.withCatalogueCode("ARK").withDate("2023-04-26").getWeekCode().getWeekCode(), is("ARK202318"));
        assertThat(wr.withCatalogueCode("ARK").withDate("2023-04-27").getWeekCode().getWeekCode(), is("ARK202318"));
        assertThat(wr.withCatalogueCode("ARK").withDate("2023-04-28").getWeekCode().getWeekCode(), is("ARK202318"));
        assertThat(wr.withCatalogueCode("ARK").withDate("2023-04-29").getWeekCode().getWeekCode(), is("ARK202318"));
        assertThat(wr.withCatalogueCode("ARK").withDate("2023-04-30").getWeekCode().getWeekCode(), is("ARK202318"));
        assertThat(wr.withCatalogueCode("ARK").withDate("2023-05-01").getWeekCode().getWeekCode(), is("ARK202319"));
    }

    @Test
    void TestCatalogueCodeVPT() {
        WeekResolver wr = new WeekResolver(zone);

        assertThat(wr.withCatalogueCode("VPT").withDate("2023-06-12").getWeekCode().getWeekCode(), is("VPT202326"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-06-13").getWeekCode().getWeekCode(), is("VPT202326"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-06-14").getWeekCode().getWeekCode(), is("VPT202326"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-06-15").getWeekCode().getWeekCode(), is("VPT202326"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-06-16").getWeekCode().getWeekCode(), is("VPT202326"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-06-17").getWeekCode().getWeekCode(), is("VPT202326"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-06-18").getWeekCode().getWeekCode(), is("VPT202326"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-06-19").getWeekCode().getWeekCode(), is("VPT202327"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-06-20").getWeekCode().getWeekCode(), is("VPT202327"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-12-11").getWeekCode().getWeekCode(), is("VPT202352"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-12-18").getWeekCode().getWeekCode(), is("VPT202401"));
        assertThat(wr.withCatalogueCode("VPT").withDate("2023-12-25").getWeekCode().getWeekCode(), is("VPT202402"));
    }

    @Test
    void Test2025MayFirstIsThursday() {
        // From: https://dbcjira.atlassian.net/browse/MS-4889
        // This change reverses a previous decision that a thursday following may 1.
        // should move the shiftday
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");

        assertThat("2025-04-23", wr.withDate("2025-04-23").getWeekCode().getWeekCode(), is("BKM202519"));
        assertThat("2025-04-24", wr.withDate("2025-04-24").getWeekCode().getWeekCode(), is("BKM202519"));
        assertThat("2025-04-25",wr.withDate("2025-04-25").getWeekCode().getWeekCode(), is("BKM202520"));
        assertThat("2025-04-26",wr.withDate("2025-04-26").getWeekCode().getWeekCode(), is("BKM202520"));
        assertThat("2025-04-27",wr.withDate("2025-04-27").getWeekCode().getWeekCode(), is("BKM202520"));
        assertThat("2025-04-28",wr.withDate("2025-04-28").getWeekCode().getWeekCode(), is("BKM202520"));
        assertThat("2025-04-29",wr.withDate("2025-04-29").getWeekCode().getWeekCode(), is("BKM202520"));
        assertThat("2025-04-30",wr.withDate("2025-04-30").getWeekCode().getWeekCode(), is("BKM202520"));

        assertThat("2025-05-01",wr.withDate("2025-05-01").getWeekCode().getWeekCode(), is("BKM202520"));
        assertThat("2025-05-02",wr.withDate("2025-05-02").getWeekCode().getWeekCode(), is("BKM202521"));

        assertThat("2025-05-08",wr.withDate("2025-05-08").getWeekCode().getWeekCode(), is("BKM202521"));
        assertThat("2025-05-09",wr.withDate("2025-05-09").getWeekCode().getWeekCode(), is("BKM202522"));
    }

    @Test
    void Test2025EarlyShiftdayDueToAscensionDay() {
        // From: https://dbcjira.atlassian.net/browse/MS-4889
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");

        assertThat("2025-05-22",wr.withDate("2025-05-22").getWeekCode().getWeekCode(), is("BKM202523"));

        assertThat("2025-05-23",wr.withDate("2025-05-23").getWeekCode().getWeekCode(), is("BKM202524"));
        assertThat("2025-05-24",wr.withDate("2025-05-24").getWeekCode().getWeekCode(), is("BKM202524"));
        assertThat("2025-05-25",wr.withDate("2025-05-25").getWeekCode().getWeekCode(), is("BKM202524"));
        assertThat("2025-05-26",wr.withDate("2025-05-26").getWeekCode().getWeekCode(), is("BKM202524"));
        assertThat("2025-05-27",wr.withDate("2025-05-27").getWeekCode().getWeekCode(), is("BKM202524"));

        assertThat("2025-05-28",wr.withDate("2025-05-28").getWeekCode().getWeekCode(), is("BKM202525"));
        assertThat("2025-05-29",wr.withDate("2025-05-29").getWeekCode().getWeekCode(), is("BKM202525"));
        assertThat("2025-05-30",wr.withDate("2025-05-30").getWeekCode().getWeekCode(), is("BKM202525"));
        assertThat("2025-05-31",wr.withDate("2025-05-31").getWeekCode().getWeekCode(), is("BKM202525"));
        assertThat("2025-06-01",wr.withDate("2025-06-01").getWeekCode().getWeekCode(), is("BKM202525"));
        assertThat("2025-06-02",wr.withDate("2025-06-02").getWeekCode().getWeekCode(), is("BKM202525"));
        assertThat("2025-06-03",wr.withDate("2025-06-03").getWeekCode().getWeekCode(), is("BKM202525"));

        assertThat("2025-06-04",wr.withDate("2025-06-04").getWeekCode().getWeekCode(), is("BKM202526"));
        assertThat("2025-06-05",wr.withDate("2025-06-05").getWeekCode().getWeekCode(), is("BKM202526"));
    }

    @Test
    void testYearPlan2025EarlyShiftdayDueToAscensionDay() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        YearPlanResult yearPlan = wr.getYearPlan(2025, true, true);

        // Check size
        assertThat(yearPlan.size(), is(52));

        // Pentecost and "Grundlovsdag" conflicts with a pinched friday on june 6., where the book cart should be positioned
        assertThat(yearPlan.getRows().get(22).getColumns().get(1).getContent().contains("ONSDAG"), is(true));     // week 23, first assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(1).getContent().contains("2025-05-28"), is(true)); // week 23, first assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(2).getContent().contains("TIRSDAG"), is(true));     // week 23, last assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(2).getContent().contains("2025-06-03"), is(true)); // week 23, last assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(3).getContent().contains("ONSDAG"), is(true));    // week 23, shiftday
        assertThat(yearPlan.getRows().get(22).getColumns().get(3).getContent().contains("2025-06-04"), is(true)); // week 23, shiftday
        assertThat(yearPlan.getRows().get(22).getColumns().get(4).getContent().contains("FREDAG"), is(true));    // week 23, book cart
        assertThat(yearPlan.getRows().get(22).getColumns().get(4).getContent().contains("2025-06-06"), is(true)); // week 23, book cart
    }

    @Test
    void Test2025Week51WithEarlyChristmasWeek() {
        // From: https://dbcjira.atlassian.net/browse/MS-4889
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");

        assertThat("2025-12-11",wr.withDate("2025-12-11").getWeekCode().getWeekCode(), is("BKM202552"));

        assertThat("2025-12-12",wr.withDate("2025-12-12").getWeekCode().getWeekCode(), is("BKM202603"));
        assertThat("2025-12-13",wr.withDate("2025-12-13").getWeekCode().getWeekCode(), is("BKM202603"));
        assertThat("2025-12-14",wr.withDate("2025-12-14").getWeekCode().getWeekCode(), is("BKM202603"));
        assertThat("2025-12-15",wr.withDate("2025-12-15").getWeekCode().getWeekCode(), is("BKM202603"));
        assertThat("2025-12-16",wr.withDate("2025-12-16").getWeekCode().getWeekCode(), is("BKM202603"));
        assertThat("2025-12-17",wr.withDate("2025-12-17").getWeekCode().getWeekCode(), is("BKM202603"));
        assertThat("2025-12-18",wr.withDate("2025-12-18").getWeekCode().getWeekCode(), is("BKM202603"));

        assertThat("2025-12-19",wr.withDate("2025-12-19").getWeekCode().getWeekCode(), is("BKM202604")); // Notice: regular shiftday due to early christmas week 51
        assertThat("2025-12-20",wr.withDate("2025-12-20").getWeekCode().getWeekCode(), is("BKM202604"));
        assertThat("2025-12-23",wr.withDate("2025-12-23").getWeekCode().getWeekCode(), is("BKM202604"));
        assertThat("2025-12-27",wr.withDate("2025-12-27").getWeekCode().getWeekCode(), is("BKM202604"));
        assertThat("2025-12-30",wr.withDate("2025-12-30").getWeekCode().getWeekCode(), is("BKM202604"));
        assertThat("2026-01-02",wr.withDate("2026-01-02").getWeekCode().getWeekCode(), is("BKM202604"));
        assertThat("2026-01-08",wr.withDate("2026-01-08").getWeekCode().getWeekCode(), is("BKM202604"));

        assertThat("2026-01-09",wr.withDate("2026-01-09").getWeekCode().getWeekCode(), is("BKM202605"));
    }

    @Test
    void testYearPlan2025WithEarlyChristmasWeek() {
        // From: https://dbcjira.atlassian.net/browse/MS-4889
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        YearPlanResult yearPlan = wr.getYearPlan(2025, true, true);

        // Check size
        assertThat(yearPlan.size(), is(52));

        // Check that "BKM Redaktion" is correct in 202603
        assertThat(yearPlan.getRows().get(50).getColumns().get(0).getContent().contains("202603"), is(true));     // Weekcode
        assertThat(yearPlan.getRows().get(50).getColumns().get(8).getContent().contains("2026-01-07"), is(true)); // week 03, BKM redaktion
        assertThat(yearPlan.getRows().get(50).getColumns().get(9).getContent().contains("2026-01-09"), is(true)); // week 03, publishing date

        // Check that the first weeks of 2026 has collapsed correctly
        assertThat(yearPlan.getRows().get(51).getColumns().get(0).getContent().contains("202604"), is(true));     // Weekcode
        assertThat(yearPlan.getRows().get(51).getColumns().get(1).getContent().contains("2025-12-19"), is(true)); // week 04, first assignment
        assertThat(yearPlan.getRows().get(51).getColumns().get(2).getContent().contains("2026-01-08"), is(true)); // week 04, last assignment
        assertThat(yearPlan.getRows().get(51).getColumns().get(3).getContent().contains("2026-01-09"), is(true)); // week 04, shiftday
        assertThat(yearPlan.getRows().get(51).getColumns().get(4).getContent().contains("2026-01-12"), is(true)); // week 04, book cart
        assertThat(yearPlan.getRows().get(51).getColumns().get(10).getContent().contains("52 + 1 + 2"), is(true)); // contained week numbers
    }
}
