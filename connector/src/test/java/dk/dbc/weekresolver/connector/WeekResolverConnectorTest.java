package dk.dbc.weekresolver.connector;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.httpclient.HttpClient;
import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WeekResolverConnectorTest {
    private static WireMockServer wireMockServer;
    private static String wireMockHost;

    final static Client CLIENT = HttpClient.newClient(new ClientConfig()
            .register(new JacksonFeature()));
    static WeekResolverConnector connector;

    @BeforeAll
    static void startWireMockServer() {

        wireMockServer = new WireMockServer(options().dynamicPort()
                .dynamicHttpsPort());
        wireMockServer.start();
        wireMockHost = "http://localhost:" + wireMockServer.port();
        configureFor("localhost", wireMockServer.port());
    }

    @BeforeAll
    static void setConnector() {
        connector = new WeekResolverConnector(CLIENT, wireMockHost);
    }

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    public void testGetWeekcode() throws WeekResolverConnectorException {
        Instant instant = Instant.ofEpochMilli(1576191600000L);
        Date date = Date.from(instant);

        WeekResolverResult weekResolverResult =
                connector.getWeekCodeForDate("DPF", LocalDate.parse("2019-10-10"));
        assertThat(weekResolverResult.getWeekNumber(), is(43));
        assertThat(weekResolverResult.getCatalogueCode(), is("DPF"));
        assertThat(weekResolverResult.getWeekCode(), is("DPF201943"));
        assertThat(weekResolverResult.getYear(), is(2019));
        assertThat(weekResolverResult.getDate(), is(date));

        WeekResolverResult weekResolverResult1 =
                connector.getWeekCodeForDate("DPF", LocalDate.parse("2019-12-31"));
        assertThat(weekResolverResult.getWeekNumber(), is(43));
        assertThat(weekResolverResult1.getCatalogueCode(), is("DPF"));
        assertThat(weekResolverResult1.getWeekCode(), is("DPF202003"));
        assertThat(weekResolverResult1.getYear(), is(2020));
        assertThat(weekResolverResult1.getDate(), is(date));

        assertThrows(NullPointerException.class, () ->
                connector.getWeekCodeForDate(null, null));
    }
}
