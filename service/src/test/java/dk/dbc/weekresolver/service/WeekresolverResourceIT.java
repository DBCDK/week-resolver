package dk.dbc.weekresolver.service;

import dk.dbc.weekresolver.WeekResolverResult;
import dk.dbc.weekresolver.WeekResolverConnectorException;
import dk.dbc.weekresolver.WeekResolverConnector;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class WeekresolverResourceIT extends AbstractWeekresolverServiceContainerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekresolverResourceIT.class);

    @Test
    void testGetWeekCodeForDate() throws WeekResolverConnectorException {
        LOGGER.info("Using this url:{}", weekresolverServiceBaseUrl);
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
        LOGGER.info("Using this url:{}", weekresolverServiceBaseUrl);
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        WeekResolverResult w = connector.getWeekCode("DPF");
        assertThat(w.getCatalogueCode(), is("DPF"));
    }

    @Test
    void getCurrentWeekCodeToday() throws WeekResolverConnectorException {
        LOGGER.info("Using this url:{}", weekresolverServiceBaseUrl);
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
    void getYearPlanForThisYear() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        // Todo:
    }

    @Test
    void getYearPlanFor2023() throws WeekResolverConnectorException {
        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);
        // Todo:
    }
}
