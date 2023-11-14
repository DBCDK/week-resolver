package dk.dbc.weekresolver.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import dk.dbc.weekresolver.connector.WeekResolverConnector;
import dk.dbc.weekresolver.connector.WeekResolverConnectorException;

import dk.dbc.weekresolver.model.WeekResolverResult;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeekResolverConnectorIT extends AbstractWeekResolverServiceContainerTest {

    @Test
    public void testConnector() throws WeekResolverConnectorException {
        Instant instant = Instant.ofEpochMilli(1572476400000L);
        Date date = Date.from(instant);

        WeekResolverConnector connector = new WeekResolverConnector(httpClient, weekresolverServiceBaseUrl);

        WeekResolverResult weekResolverResult =
                connector.getWeekCodeForDate("DPF", LocalDate.parse("2019-10-10"));
        assertThat(weekResolverResult.getWeekNumber(), is(44));
        assertThat(weekResolverResult.getCatalogueCode(), is("DPF"));
        assertThat(weekResolverResult.getWeekCode(), is("DPF201944"));
        assertThat(weekResolverResult.getYear(), is(2019));
        assertThat(weekResolverResult.getDate(), is(date));

        assertThrows(NullPointerException.class, () ->
                connector.getWeekCodeForDate(null, null));
    }
}
