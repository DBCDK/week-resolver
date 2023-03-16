package dk.dbc.weekresolver.service;

import dk.dbc.weekresolver.model.WeekResolverResult;
import dk.dbc.weekresolver.model.YearPlanResult;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeekResolverTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverTest.class);

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

        // 28. mar. = thursday => + 3 weeks = 18. apr., week 16, maundy thursday
        // Closed, but not shiftday so add 3 weeks and ajust for closing days = tuesday week 17
        assertThat(wr.withDate("2019-03-28").getWeekCode().getWeekCode(), is("DPF201917"));

        // 5.apr. = friday, shiftday => + 1 week + 3 weeks = 26. apr., week 17
        // Shiftday so add 1 + 3 weeks = week 17. Last week was easter, no closing in this
        // week, so push forward to 18
        assertThat(wr.withDate("2019-03-29").getWeekCode().getWeekCode(), is("DPF201918"));

        // 8. apr. = monday => + 3 weeks = 22. apr., week 17, easter monday
        // Closed, not shiftday so add 3 weeks and adjust for closing days = tuesday week 17
        // Last week was easter, so push forward to 18
        assertThat(wr.withDate("2019-04-01").getWeekCode().getWeekCode(), is("DPF201918"));

        // 8. apr. = monday => + 3 weeks = 29. apr., week 18
        // Not closed and not shiftday = 18
        assertThat(wr.withDate("2019-04-08").getWeekCode().getWeekCode(), is("DPF201918"));

        // 03. apr. = wednesday => + 3 weeks = 1. may., week 18
        // Closed, not shiftday so add 2 weeks and adjust for closing days and easter last week = 18
        assertThat(wr.withDate("2019-04-03").getWeekCode().getWeekCode(), is("DPF201918"));

        // 16. apr. = thursday => + 3 weeks = 7. may., week 19
        // Not closed and not shiftday = 19
        assertThat(wr.withDate("2019-04-16").getWeekCode().getWeekCode(), is("DPF201919"));

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
        //   - current week number + 1
        //   - Shiftday is sunday
        //   - No handling of closing days
        //   - Allow end of year weeks
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("EMO");
        assertDoesNotThrow(() -> wr.withDate("2019-11-29").getWeekCode());
        assertThat(wr.withDate("2019-11-23").getWeekCode().getWeekCode(), is("EMO201948")); // saturday, week 47 = 48
        assertThat(wr.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMO201948")); // sunday, week 47 = 48
        assertThat(wr.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMO201949")); // monday, week 48 = 49
        assertThat(wr.withDate("2019-12-26").getWeekCode().getWeekCode(), is("EMO202001")); // wednesday, week 52 = 01
        assertThat(wr.withDate("2020-04-09").getWeekCode().getWeekCode(), is("EMO202016")); // thursday, week 15 = 16
        assertThat(wr.withDate("2020-05-01").getWeekCode().getWeekCode(), is("EMO202019")); // friday may 1.st (closing day ignored), week 18 = 19
    }

    @Test
    void TestCatalogueCodeEMS() {

        // EMS:
        //   - current week number + 1
        //   - Shiftday is sunday
        //   - No handling of closing days
        //   - Allow end of year weeks
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("EMS");
        assertDoesNotThrow(() -> wr.withDate("2019-11-29").getWeekCode());
        assertThat(wr.withDate("2019-11-23").getWeekCode().getWeekCode(), is("EMS201948")); // saturday, week 47 = 48
        assertThat(wr.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMS201948")); // sunday, week 47 = 48
        assertThat(wr.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMS201949")); // monday, week 48 = 49
        assertThat(wr.withDate("2019-12-26").getWeekCode().getWeekCode(), is("EMS202001")); // wednesday, week 52 = 01
        assertThat(wr.withDate("2020-04-09").getWeekCode().getWeekCode(), is("EMS202016")); // thursday, week 15 = 16
        assertThat(wr.withDate("2020-05-01").getWeekCode().getWeekCode(), is("EMS202019")); // friday may 1.st (closing day ignored), week 18 = 19
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

        assertThat(wr1.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMS201948")); // sunday, week 47 = 48
        assertThat(wr1.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMS201949")); // monday, week 48 = 49

        assertThat(wr2.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMS201948")); // sunday, week 47 = 48
        assertThat(wr2.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMS201949")); // monday, week 48 = 49

        assertThat(wr3.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMS201948")); // sunday, week 47 = 48
        assertThat(wr4.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMS201949")); // monday, week 48 = 49

        assertThat(wr4.withDate("2019-11-23").getWeekCode().getWeekCode(), is("EMS201948")); // saturday, week 47 = 48
        assertThat(wr4.withDate("2019-11-24").getWeekCode().getWeekCode(), is("EMS201949")); // sunday, week 47 (week 48 in us) = 49
        assertThat(wr4.withDate("2019-11-25").getWeekCode().getWeekCode(), is("EMS201949")); // monday, week 48 = 49
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
        assertThat(wr.withDate("2019-12-10").getWeekCode().getWeekCode(), is("BKM202002"));
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
        assertThat(wr.withCatalogueCode("ARK").withDate("2020-04-22").getWeekCode().getWeekCode(), is("ARK202017"));
        assertThat(wr.withCatalogueCode("BLG").withDate("2020-04-22").getWeekCode().getWeekCode(), is("BLG202017"));

        // +1 week
        // Note: When checked, existing records from this date has EM[S|O]202019, that is current week + 1
        // This is different from the specification of the future use of EMS/EMO
        assertThat(wr.withCatalogueCode("EMO").withDate("2020-04-22").getWeekCode().getWeekCode(), is("EMO202018"));
        assertThat(wr.withCatalogueCode("EMS").withDate("2020-04-22").getWeekCode().getWeekCode(), is("EMS202018"));
        assertThat(wr.withCatalogueCode("DAN").withDate("2020-04-22").getWeekCode().getWeekCode(), is("DAN202018"));
        assertThat(wr.withCatalogueCode("DAR").withDate("2020-04-22").getWeekCode().getWeekCode(), is("DAR202018"));

        // +2 weeks
        assertThat(wr.withCatalogueCode("DLR").withDate("2020-04-22").getWeekCode().getWeekCode(), is("DLR202019"));
        assertThat(wr.withCatalogueCode("DLF").withDate("2020-04-29").getWeekCode().getWeekCode(), is("DLF202020"));
        assertThat(wr.withCatalogueCode("DBR").withDate("2019-12-26").getWeekCode().getWeekCode(), is("DBR202003"));
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-04-27").getWeekCode().getWeekCode(), is("DBF202020"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-04-27").getWeekCode().getWeekCode(), is("BKM202020"));
        assertThat(wr.withCatalogueCode("GBF").withDate("2019-11-26").getWeekCode().getWeekCode(), is("GBF201950"));

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

        assertThat(wr.withCatalogueCode("ERA").withDate("2020-04-08").getWeekCode().getWeekCode(), is("ERA202017"));
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

        // Torsdag 02.04.20 afsluttes DBC+BKM202017. Torsdag morgen skal koden være 202017"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-04-09").getWeekCode().getWeekCode(), is("DBF202017"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-04-09").getWeekCode().getWeekCode(), is("BKM202017"));

        // "Der er ingen afslutning i uge 15 (påskeugen)"
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-04-02").getWeekCode().getWeekCode(), is("DBF202017"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-04-02").getWeekCode().getWeekCode(), is("BKM202017"));

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

        // Random days in december
        assertThat(wr.withDate("2022-12-01").getCurrentWeekCode().getWeekCode(), is("BKM202248"));
        assertThat(wr.withDate("2022-12-05").getCurrentWeekCode().getWeekCode(), is("BKM202249"));
        assertThat(wr.withDate("2022-12-09").getCurrentWeekCode().getWeekCode(), is("BKM202250"));
        assertThat(wr.withDate("2022-12-15").getCurrentWeekCode().getWeekCode(), is("BKM202250"));
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
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-05-20").getCurrentWeekCode().getWeekCode(), is("DBF202022"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-05-20").getCurrentWeekCode().getWeekCode(), is("BKM202022"));
        assertThat(wr.withCatalogueCode("DBF").withDate("2020-05-21").getCurrentWeekCode().getWeekCode(), is("DBF202022"));
        assertThat(wr.withCatalogueCode("BKM").withDate("2020-05-21").getCurrentWeekCode().getWeekCode(), is("BKM202022"));
    }

    @Test
    void testGetYearPlan2022() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        YearPlanResult yearPlan = wr.getYearPlan(2022);

        // Check size
        assertThat(yearPlan.size(), is(53));

        // Check alignment
        assertThat(yearPlan.getRows().get(1).getColumns().get(0), is("202202"));
        assertThat(yearPlan.getRows().get(52).getColumns().get(0), is("202302"));
    }

    @Test
    void testGetYearPlan2023() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        YearPlanResult yearPlan = wr.getYearPlan(2023);

        // Check size
        assertThat(yearPlan.size(), is(53));

        // Check alignment
        assertThat(yearPlan.getRows().get(1).getColumns().get(0), is("202302"));
        assertThat(yearPlan.getRows().get(52).getColumns().get(0), is("202402"));

        // Check beginning of the year
        assertThat(yearPlan.getRows().get(3).getColumns().get(1), is("\"2023-01-06\"")); // week 2, first assignment

        // Check easter
        assertThat(yearPlan.getRows().get(14).getColumns().get(2).contains("ONSDAG"), is(true));     // week 13, last assignment
        assertThat(yearPlan.getRows().get(14).getColumns().get(2).contains("2023-03-29"), is(true)); // week 13, last assignment
        assertThat(yearPlan.getRows().get(14).getColumns().get(3).contains("TORSDAG"), is(true));    // week 13, shiftday
        assertThat(yearPlan.getRows().get(14).getColumns().get(3).contains("2023-03-30"), is(true)); // week 13, shiftday
        assertThat(yearPlan.getRows().get(15).getColumns().get(2).isEmpty(), is(true));              // week 14, last assignment
        assertThat(yearPlan.getRows().get(15).getColumns().get(3).isEmpty(), is(true));              // week 14, shiftday
        assertThat(yearPlan.getRows().get(16).getColumns().get(1).contains("TORSDAG"), is(true));    // week 15, first assignment
        assertThat(yearPlan.getRows().get(16).getColumns().get(1).contains("2023-03-30"), is(true)); // week 15, first assignment
        assertThat(yearPlan.getRows().get(16).getColumns().get(2), is("\"2023-04-13\""));            // week 15, last assignment
        assertThat(yearPlan.getRows().get(16).getColumns().get(3), is("\"2023-04-14\""));            // week 15, shiftday assignment

        // Week before may 1st.
        assertThat(yearPlan.getRows().get(18).getColumns().get(1), is("\"2023-04-21\""));            // week 17, first assignment
        assertThat(yearPlan.getRows().get(18).getColumns().get(2).contains("ONSDAG"), is(true));     // week 17, last assignment
        assertThat(yearPlan.getRows().get(18).getColumns().get(2).contains("2023-04-26"), is(true)); // week 17, last assignment
        assertThat(yearPlan.getRows().get(18).getColumns().get(3).contains("TORSDAG"), is(true));    // week 17, shiftday
        assertThat(yearPlan.getRows().get(18).getColumns().get(3).contains("2023-04-27"), is(true)); // week 17, shiftday

        // Ascension day
        assertThat(yearPlan.getRows().get(21).getColumns().get(2).contains("TIRSDAG"), is(true));    // week 20, last assignment
        assertThat(yearPlan.getRows().get(21).getColumns().get(2).contains("2023-05-16"), is(true)); // week 20, last assignment
        assertThat(yearPlan.getRows().get(21).getColumns().get(3).contains("ONSDAG"), is(true));     // week 20, shiftday
        assertThat(yearPlan.getRows().get(21).getColumns().get(3).contains("2023-05-17"), is(true)); // week 20, shiftday

        // Pentecost
        assertThat(yearPlan.getRows().get(22).getColumns().get(1).contains("ONSDAG"), is(true));     // week 21, first assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(1).contains("2023-05-17"), is(true)); // week 21, first assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(2).contains("ONSDAG"), is(true));     // week 21, last assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(2).contains("2023-05-24"), is(true)); // week 21, last assignment
        assertThat(yearPlan.getRows().get(22).getColumns().get(3).contains("TORSDAG"), is(true));    // week 21, shiftday
        assertThat(yearPlan.getRows().get(22).getColumns().get(3).contains("2023-05-25"), is(true)); // week 21, shiftday

        // Week before "Grundlovsdag"
        assertThat(yearPlan.getRows().get(23).getColumns().get(1).contains("TORSDAG"), is(true));    // week 22, first assignment
        assertThat(yearPlan.getRows().get(23).getColumns().get(1).contains("2023-05-25"), is(true)); // week 22, first assignment
        assertThat(yearPlan.getRows().get(23).getColumns().get(2).contains("ONSDAG"), is(true));     // week 22, last assignment
        assertThat(yearPlan.getRows().get(23).getColumns().get(2).contains("2023-05-31"), is(true)); // week 22, last assignment
        assertThat(yearPlan.getRows().get(23).getColumns().get(3).contains("TORSDAG"), is(true));    // week 22, shiftday
        assertThat(yearPlan.getRows().get(23).getColumns().get(3).contains("2023-06-01"), is(true)); // week 22, shiftday

        // Weeks without production
        assertThat(yearPlan.getRows().get(1).getColumns().get(1), is("\"2022-12-16\""));  // week 52 previous year
        assertThat(yearPlan.getRows().get(1).getColumns().get(0), is("202302"));          // week 1 previous year
        assertThat(yearPlan.getRows().get(2).getColumns().get(1), is("\"2022-12-30\""));  // week 1
        assertThat(yearPlan.getRows().get(2).getColumns().get(0), is("202303"));          // week 1
        assertThat(yearPlan.getRows().get(15).getColumns().get(1), is(""));               // week 14
        assertThat(yearPlan.getRows().get(15).getColumns().get(0), is("202316"));         // week 14
        assertThat(yearPlan.getRows().get(52).getColumns().get(1), is("\"2023-12-15\"")); // week 52
        assertThat(yearPlan.getRows().get(52).getColumns().get(0), is("202402"));         // week 52
        yearPlan.getRows().forEach(r -> {
            if (!Set.of("202303", "202304", "202316", "202403").contains(r.getColumns().get(0))) {
                assertThat(r.getColumns().get(1).isEmpty(), is(false));
            }
        });
    }

    @Test
    void testGetYearPlanWithWeek2024() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        YearPlanResult yearPlan = wr.getYearPlan(2024);

        // Check size
        assertThat(yearPlan.size(), is(53));

        // Check alignment
        assertThat(yearPlan.getRows().get(1).getColumns().get(0), is("202403"));
        assertThat(yearPlan.getRows().get(52).getColumns().get(0), is("202503"));
    }

    @Test
    void testGetYearPlanWithWeek2025() {
        WeekResolver wr = new WeekResolver(zone).withCatalogueCode("BKM");
        YearPlanResult yearPlan = wr.getYearPlan(2025);

        // Check size
        assertThat(yearPlan.size(), is(53));

        // Check alignment
        assertThat(yearPlan.getRows().get(1).getColumns().get(0), is("202503"));
        assertThat(yearPlan.getRows().get(52).getColumns().get(0), is("202603"));
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

        results.forEach(r -> {
            LOGGER.info("{}", r.getWeekCode());
            if (r.getDescription().getWeekCodeFirst() != null) {
                LOGGER.info("  first {}", wr.fromDate(r.getDescription().getWeekCodeFirst()));
                LOGGER.info("  last  {}", wr.fromDate(r.getDescription().getWeekCodeLast()));
                LOGGER.info("  shift {}", wr.fromDate(r.getDescription().getShiftDay()));
            } else {
                LOGGER.info("  first --");
                LOGGER.info("  last  --");
                LOGGER.info("  shift --");
            }
        });

        assertThat(results.get(0).getWeekCode(), is("BKM202251"));
        assertThat(wr.stringFromDate(results.get(0).getDescription().getWeekCodeFirst()), is("2022-12-02"));
        assertThat(wr.stringFromDate(results.get(0).getDescription().getWeekCodeLast()), is("2022-12-08"));
        assertThat(wr.stringFromDate(results.get(0).getDescription().getShiftDay()), is("2022-12-09"));

        assertThat(results.get(1).getWeekCode(), is("BKM202252"));
        assertThat(wr.stringFromDate(results.get(1).getDescription().getWeekCodeFirst()), is("2022-12-09"));
        assertThat(wr.stringFromDate(results.get(1).getDescription().getWeekCodeLast()), is("2022-12-15"));
        assertThat(wr.stringFromDate(results.get(1).getDescription().getShiftDay()), is("2022-12-16"));

        assertThat(results.get(2).getWeekCode(), is("BKM202302"));
        assertThat(wr.stringFromDate(results.get(2).getDescription().getWeekCodeFirst()), is("2022-12-16"));
        assertThat(wr.stringFromDate(results.get(2).getDescription().getWeekCodeLast()), is("2022-12-22"));
        assertThat(wr.stringFromDate(results.get(2).getDescription().getShiftDay()), is("2022-12-23"));

        assertThat(results.get(3).getWeekCode(), is("BKM202302"));
        assertThat(wr.stringFromDate(results.get(3).getDescription().getWeekCodeFirst()), is("2022-12-23"));
        assertThat(wr.stringFromDate(results.get(3).getDescription().getWeekCodeLast()), is("2022-12-29"));
        assertThat(wr.stringFromDate(results.get(3).getDescription().getShiftDay()), is("2022-12-30"));

        assertThat(results.get(4).getWeekCode(), is("BKM202303"));
        assertThat(wr.stringFromDate(results.get(4).getDescription().getWeekCodeFirst()), is("2022-12-30"));
        assertThat(wr.stringFromDate(results.get(4).getDescription().getWeekCodeLast()), is("2023-01-05"));
        assertThat(wr.stringFromDate(results.get(4).getDescription().getShiftDay()), is("2023-01-06"));

        assertThat(results.get(5).getWeekCode(), is("BKM202304"));
        assertThat(wr.stringFromDate(results.get(5).getDescription().getWeekCodeFirst()), is("2023-01-06"));
        assertThat(wr.stringFromDate(results.get(5).getDescription().getWeekCodeLast()), is("2023-01-12"));
        assertThat(wr.stringFromDate(results.get(5).getDescription().getShiftDay()), is("2023-01-13"));

        assertThat(results.get(6).getWeekCode(), is("BKM202305"));
        assertThat(wr.stringFromDate(results.get(6).getDescription().getWeekCodeFirst()), is("2023-01-13"));
        assertThat(wr.stringFromDate(results.get(6).getDescription().getWeekCodeLast()), is("2023-01-19"));
        assertThat(wr.stringFromDate(results.get(6).getDescription().getShiftDay()), is("2023-01-20"));
    }
}
