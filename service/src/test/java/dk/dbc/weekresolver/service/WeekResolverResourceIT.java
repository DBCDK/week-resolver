package dk.dbc.weekresolver.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

import dk.dbc.httpclient.HttpGet;
import dk.dbc.weekresolver.connector.WeekResolverConnector;
import dk.dbc.weekresolver.connector.WeekResolverConnectorException;
import dk.dbc.weekresolver.model.WeekCodeFulfilledResult;
import dk.dbc.weekresolver.model.WeekResolverResult;
import dk.dbc.weekresolver.model.YearPlanFormat;
import dk.dbc.weekresolver.model.YearPlanResult;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class WeekResolverResourceIT extends AbstractWeekResolverServiceContainerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverResourceIT.class);

    @Test
    void openapi() {
        final HttpGet httpGet = new HttpGet(httpClient)
                .withBaseUrl(weekresolverServiceBaseUrl)
                .withPathElements("openapi");

        final Response response = httpClient.execute(httpGet);
        assertThat("status code", response.getStatus(), is(200));
        final String openapi = response.readEntity(String.class);
        assertThat("openapi", openapi, containsString("Provides weekcode calculations"));
    }

    @Test
    void testGetWeekCodeForDate() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        WeekResolverResult w = connector.getWeekCodeForDate("DPF", LocalDate.parse("2019-10-20"));
        assertThat(w.getWeekCode(), is("DPF201946"));
        assertThat(w.getYear(), is(2019));
        assertThat(w.getWeekNumber(), is(46));

        WeekResolverResult w1 = connector.getWeekCodeForDate("DPF", LocalDate.parse("2019-12-21"));
        assertThat(w1.getWeekCode(), is("DPF202003"));
        assertThat(w1.getYear(), is(2020));
        assertThat(w1.getWeekNumber(), is(3));
    }

    @Test
    void testGetTodaysWeekCode() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        WeekResolverResult w = connector.getWeekCode("DPF");
        assertThat(w.getCatalogueCode(), is("DPF"));
    }

    @Test
    void getCurrentWeekCodeToday() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        WeekResolverResult w = connector.getCurrentWeekCode("BKM");
        assertThat(w.getCatalogueCode(), is("BKM"));
    }

    @Test
    void getCurrentWeekCodeForDate() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        WeekResolverResult w = connector.getCurrentWeekCodeForDate("BKM", LocalDate.parse("2023-03-06"));
        assertThat(w.getCatalogueCode(), is("BKM"));
        assertThat(w.getWeekCode(), is("BKM202310"));

        w = connector.getCurrentWeekCodeForDate("BKM", LocalDate.parse("2023-03-10"));
        assertThat(w.getCatalogueCode(), is("BKM"));
        assertThat(w.getWeekCode(), is("BKM202311"));
    }

    @Test
    void getYearPlanForThisYearJson() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        YearPlanResult y = connector.getYearPlanForCode(YearPlanFormat.JSON, "BKM");
        assertThat(y.getRows().size(), is(53));
    }

    @Test
    void getYearPlanFor2022Json() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        YearPlanResult y = connector.getYearPlanForCodeAndYear(YearPlanFormat.JSON, "BKM", 2022);
        assertThat(y.getRows().size(), is(53));
        assertThat(y.getRows().get(1).getColumns().get(0).getContent(), is("202202"));
    }

    @Test
    void getYearPlanForThisYearCsv() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        String csv = connector.getYearPlanCsvForCode(YearPlanFormat.CSV, "BKM");
        assertThat(csv.split("\n").length, is(53));
    }

    @Test
    void getYearPlanFor2022Csv() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        String csv = connector.getYearPlanCsvForCodeAndYear(YearPlanFormat.CSV, "BKM", 2022);
        assertThat(csv.split("\n").length, is(53));
        assertThat(csv.split("\n")[1].startsWith("202202;"), is(true));
    }

    @Test
    void test2021CompleteCheck() throws IOException, WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        String[] actualCsv = connector.getYearPlanCsvForCodeAndYear(YearPlanFormat.CSV, "BKM", 2021).split("\n");

        String content = Files.readString(
                Path.of(Objects.requireNonNull(
                        getClass().getClassLoader().getResource("2021.csv")).getPath()));
        assertThat(content, is(notNullValue()));
        String[] expectedCsv = content.split("\n");

        assertThat(expectedCsv.length, is(actualCsv.length));

        // Make sure that the year plan for 2023 does not change when modifying output for other years
        for (int i = 0; i < actualCsv.length; i++) {
            assertThat(actualCsv[i], is(expectedCsv[i]));
        }
    }

    @Test
    void test2022CompleteCheck() throws IOException, WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        String[] actualCsv = connector.getYearPlanCsvForCodeAndYear(YearPlanFormat.CSV, "BKM", 2022).split("\n");

        String content = Files.readString(
                Path.of(Objects.requireNonNull(
                        getClass().getClassLoader().getResource("2022.csv")).getPath()));
        assertThat(content, is(notNullValue()));
        String[] expectedCsv = content.split("\n");

        assertThat(expectedCsv.length, is(actualCsv.length));

        // Make sure that the year plan for 2023 does not change when modifying output for other years
        for (int i = 0; i < actualCsv.length; i++) {
            assertThat(actualCsv[i], is(expectedCsv[i]));
        }
    }

    @Test
    void test2023CompleteCheck() throws IOException, WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        String[] actualCsv = connector.getYearPlanCsvForCodeAndYear(YearPlanFormat.CSV, "BKM", 2023).split("\n");

        String content = Files.readString(
                Path.of(Objects.requireNonNull(
                        getClass().getClassLoader().getResource("2023.csv")).getPath()));
        assertThat(content, is(notNullValue()));
        String[] expectedCsv = content.split("\n");

        assertThat(expectedCsv.length, is(actualCsv.length));

        // Make sure that the year plan for 2023 does not change when modifying output for other years
        for (int i = 0; i < actualCsv.length; i++) {
            assertThat(actualCsv[i], is(expectedCsv[i]));
        }
    }

    @Test
    void test2024CompleteCheck() throws IOException, WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        String[] actualCsv = connector.getYearPlanCsvForCodeAndYear(YearPlanFormat.CSV, "BKM", 2024).split("\n");

        String content = Files.readString(
                Path.of(Objects.requireNonNull(
                        getClass().getClassLoader().getResource("2024.csv")).getPath()));
        assertThat(content, is(notNullValue()));
        String[] expectedCsv = content.split("\n");

        assertThat(expectedCsv.length, is(actualCsv.length));

        // Make sure that the year plan for 2024 does not change when modifying output for other years
        for (int i = 0; i < actualCsv.length; i++) {
            assertThat(actualCsv[i], is(expectedCsv[i]));
        }
    }

    @Test
    void getPastWeekCodeFulfilled() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);

        String past = connector.getCurrentWeekCodeForDate("BKM", LocalDate.now().minusWeeks(1)).getWeekCode();

        WeekCodeFulfilledResult result = connector.getWeekCodeFulfilled(past);
        assertThat(result.getIsFulfilled(), is(true));
    }

    @Test
    void getPresentWeekCodeFulfilled() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);

        String present = connector.getCurrentWeekCode("BKM").getWeekCode();

        WeekCodeFulfilledResult result = connector.getWeekCodeFulfilled(present);
        assertThat(result.getIsFulfilled(), is(true));
    }

    @Test
    void getFutureWeekCodeFulfilled() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);

        String future = connector.getCurrentWeekCodeForDate("BKM", LocalDate.now().plusWeeks(1)).getWeekCode();

        WeekCodeFulfilledResult result = connector.getWeekCodeFulfilled(future);
        assertThat(result.getIsFulfilled(), is(false));
    }
}
