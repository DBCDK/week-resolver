package dk.dbc.weekresolver.rest;

import dk.dbc.weekresolver.ejb.WeekResolverBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * This class defines the other classes that make up this JAX-RS application by
 * having the getClasses method return a specific set of resources.
 */
@ApplicationPath("")
public class WeekResolverApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverApplication.class);

    private static final Set<Class<?>> Classes = new HashSet<>();

    public WeekResolverApplication() {
        Classes.add(WeekResolverBean.class);
        Classes.add(StatusBean.class);
        for (Class<?> clazz : Classes) {
            LOGGER.info("Registered {} resource", clazz.getName());
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Classes;
    }
}
