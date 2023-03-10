package dk.dbc.weekresolver.connector;

import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpGet;
import dk.dbc.invariant.InvariantUtil;
import dk.dbc.util.Stopwatch;
import dk.dbc.weekresolver.model.WeekResolverResult;
import dk.dbc.weekresolver.model.YearPlanFormat;
import dk.dbc.weekresolver.model.YearPlanResult;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * WeekresolverConnector - Weekresolver client
 * <p>
 * To use this class, you construct an instance, specifying a web resources client as well as
 * a base URL for the Weekresolver service endpoint you will be communicating with.
 * </p>
 * <p>
 * This class is thread safe, as long as the given web resources client remains thread safe.
 * </p>
 */
public class WeekResolverConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverConnector.class);
    private static final RetryPolicy<Response> RETRY_POLICY = new RetryPolicy<Response>()
            .handle(ProcessingException.class)
            .handleResultIf(response -> response.getStatus() == 404
                    || response.getStatus() == 500
                    || response.getStatus() == 502)
            .withDelay(Duration.ofSeconds(5))
            .withMaxRetries(3);

    private final FailSafeHttpClient failSafeHttpClient;
    private final String baseUrl;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public WeekResolverConnector(Client httpClient, String baseUrl) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for record service endpoint
     */
    public WeekResolverConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        this.failSafeHttpClient = InvariantUtil.checkNotNullOrThrow(
                failSafeHttpClient, "failSafeHttpClient");
        this.baseUrl = InvariantUtil.checkNotNullNotEmptyOrThrow(
                baseUrl, "baseUrl");
    }

    @SuppressWarnings("unused")
    public WeekResolverResult getWeekCode(String catalogueCode) throws WeekResolverConnectorException {
        return getWeekCodeForDate(catalogueCode, LocalDate.now());
    }

    public WeekResolverResult getWeekCodeForDate(String catalogueCode, LocalDate date) throws WeekResolverConnectorException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final Response response = new HttpGet(failSafeHttpClient).withBaseUrl(baseUrl)
                    .withPathElements("api", "v1", "date", catalogueCode, date.format(formatter)).execute();
            assertResponseStatus(response);

            return response.readEntity(WeekResolverResult.class);
        } finally {
            LOGGER.info("getWeekCode took {} ms", stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));

        }
    }

    @SuppressWarnings("unused")
    public WeekResolverResult getCurrentWeekCode(String catalogueCode) throws WeekResolverConnectorException {
        return getCurrentWeekCodeForDate(catalogueCode, LocalDate.now());
    }

    public WeekResolverResult getCurrentWeekCodeForDate(String catalogueCode, LocalDate date) throws WeekResolverConnectorException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final Response response = new HttpGet(failSafeHttpClient).withBaseUrl(baseUrl)
                    .withPathElements("api", "v1", "current", catalogueCode, date.format(formatter)).execute();
            assertResponseStatus(response);

            return response.readEntity(WeekResolverResult.class);
        } finally {
            LOGGER.info("getWeekCode took {} ms", stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    @SuppressWarnings("unused")
    public YearPlanResult getYearPlanForCode(YearPlanFormat format, String catalogueCode) throws WeekResolverConnectorException {
        return getYearPlanForCodeAndYear(format, catalogueCode, LocalDate.now().getYear());
    }

    public YearPlanResult getYearPlanForCodeAndYear(YearPlanFormat format, String catalogueCode, Integer year) throws WeekResolverConnectorException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final Response response = new HttpGet(failSafeHttpClient).withBaseUrl(baseUrl)
                    .withPathElements("api", "v1", "year", format.name(), catalogueCode, String.format("%04d", year)).execute();
            assertResponseStatus(response);

            return response.readEntity(YearPlanResult.class);
        } finally {
            LOGGER.info("getWeekCode took {} ms", stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public String getYearPlanCsvForCode(YearPlanFormat format, String catalogueCode) throws WeekResolverConnectorException {
        return getYearPlanCsvForCodeAndYear(format, catalogueCode, LocalDate.now().getYear());
    }

    public String getYearPlanCsvForCodeAndYear(YearPlanFormat format, String catalogueCode, Integer year) throws WeekResolverConnectorException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final Response response = new HttpGet(failSafeHttpClient).withBaseUrl(baseUrl)
                    .withPathElements("api", "v1", "year", format.name(), catalogueCode, String.format("%04d", year)).execute();
            assertResponseStatus(response);

            return response.readEntity(String.class);
        } finally {
            LOGGER.info("getWeekCode took {} ms", stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public void close() {
        failSafeHttpClient.getClient().close();
    }

    private void assertResponseStatus(Response response)
            throws WeekResolverUnexpectedStatusCodeException {
        final Response.Status actualStatus =
                Response.Status.fromStatusCode(response.getStatus());
        if (actualStatus != Response.Status.OK) {
            throw new WeekResolverUnexpectedStatusCodeException(
                    String.format("Weekresolver service returned with unexpected status code: %s",
                            actualStatus), actualStatus.getStatusCode());
        }
    }
}
