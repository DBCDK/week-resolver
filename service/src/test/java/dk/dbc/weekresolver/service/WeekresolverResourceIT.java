package dk.dbc.weekresolver.service;

import java.time.LocalDate;

import dk.dbc.httpclient.HttpGet;
import dk.dbc.weekresolver.connector.WeekResolverConnector;
import dk.dbc.weekresolver.connector.WeekResolverConnectorException;
import dk.dbc.weekresolver.model.WeekResolverResult;
import dk.dbc.weekresolver.model.YearPlanFormat;
import dk.dbc.weekresolver.model.YearPlanResult;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class WeekresolverResourceIT extends AbstractWeekresolverServiceContainerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekresolverResourceIT.class);

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
        assertThat(y.getRows().size(), is(52));
    }

    @Test
    void getYearPlanFor2022Json() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        YearPlanResult y = connector.getYearPlanForCodeAndYear(YearPlanFormat.JSON, "BKM", 2022);
        assertThat(y.getRows().size(), is(52));
        assertThat(y.getRows().get(1).getColumns().get(0), is("202203"));
    }

    @Test
    void getYearPlanForThisYearCsv() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        String csv = connector.getYearPlanCsvForCode(YearPlanFormat.CSV, "BKM");
        assertThat(csv.split("\n").length, is(52));
    }

    @Test
    void getYearPlanFor2022Csv() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        String csv = connector.getYearPlanCsvForCodeAndYear(YearPlanFormat.CSV, "BKM", 2022);
        assertThat(csv.split("\n").length, is(52));
        assertThat(csv.split("\n")[1].startsWith("202203;"), is(true));
    }
}
