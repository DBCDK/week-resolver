package dk.dbc.weekresolver.rest;

import dk.dbc.weekresolver.service.WeekResolverService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

/**
 * This class defines the other classes that make up this JAX-RS application by
 * having the getClasses method return a specific set of resources.
 */
@ApplicationPath("/")
public class WeekResolverApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverApplication.class);

    private static final Set<Class<?>> classes = new HashSet<>();
    static {
        classes.add(WeekResolverService.class);
    }

    public WeekResolverApplication() {
        for (Class<?> clazz : classes) {
            LOGGER.info("Registered {} resource", clazz.getName());
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
