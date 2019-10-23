/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.weekresolver.service;

import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpClient;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public abstract class AbstractWeekresolverServiceContainerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWeekresolverServiceContainerTest.class);

    static final GenericContainer weekresolverServiceContainer;
    static final String weekresolverServiceBaseUrl;
    static final FailSafeHttpClient httpClient;

    static {
        weekresolverServiceContainer = new GenericContainer("docker-io.dbc.dk/weekresolver:devel")
                .withLogConsumer(new Slf4jLogConsumer(LOGGER))
                .withEnv("JAVA_MAX_HEAP_SIZE", "2G")
                .withEnv("LOG_FORMAT", "text")
                .withEnv("TZ", "Europe/Copenhagen")
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/status"))
                .withStartupTimeout(Duration.ofMinutes(5));
        weekresolverServiceContainer.start();
        weekresolverServiceBaseUrl = "http://" + weekresolverServiceContainer.getContainerIpAddress() +
                ":" + weekresolverServiceContainer.getMappedPort(8080);
        httpClient = FailSafeHttpClient.create(HttpClient.newClient(), new RetryPolicy().withMaxRetries(0));
    }

}
