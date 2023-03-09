package dk.dbc.weekresolver.service;

import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpClient;
import jakarta.ws.rs.core.Response;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public abstract class AbstractWeekresolverServiceContainerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWeekresolverServiceContainerTest.class);

    static final GenericContainer<?> weekresolverServiceContainer;
    static final String weekresolverServiceBaseUrl;
    static final FailSafeHttpClient httpClient;

    static {
        //noinspection resource
        weekresolverServiceContainer = new GenericContainer<>("docker-metascrum.artifacts.dbccloud.dk/weekresolver:devel")
                .withLogConsumer(new Slf4jLogConsumer(LOGGER))
                .withEnv("JAVA_MAX_HEAP_SIZE", "2G")
                .withEnv("LOG_FORMAT", "text")
                .withEnv("TZ", "Europe/Copenhagen")
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/api/v1/date/dpf"))
                .withStartupTimeout(Duration.ofMinutes(5));
        weekresolverServiceContainer.start();
        weekresolverServiceBaseUrl = "http://" + weekresolverServiceContainer.getHost() +
                ":" + weekresolverServiceContainer.getMappedPort(8080);
        httpClient = FailSafeHttpClient.create(HttpClient.newClient(), new RetryPolicy<Response>().withMaxRetries(0));
    }

}
