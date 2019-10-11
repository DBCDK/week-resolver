package dk.dbc.weekresolver.service;

import dk.dbc.jsonb.JSONBContext;
import dk.dbc.jsonb.JSONBException;

import dk.dbc.weekresolver.ejb.WeekResolverBean;
import dk.dbc.weekresolver.ejb.WeekResolverResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.time.format.DateTimeParseException;

@Path("/api")
public class WeekResolverService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverService.class);
    private static final JSONBContext jsonbContext = new JSONBContext();

    /**
     * Get week id based on type(to be elaborated) and a date.
     *
     * @param catalogueCode
     * @param date          (yyyy-MM-dd)
     * @return a HTTP 200 with the week-code as a string
     * @throws DateTimeParseException        if specified date is not parseable
     * @throws UnsupportedOperationException if the specified cataloguecode is unkown or unsupported
     * @throws JSONBException                if valid json could not be generated
     */
    @GET
    @Path("v1/date/{catalogueCode}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeekCode(@PathParam("catalogueCode") final String catalogueCode,
                                @PathParam("date") final String date) {
        LOGGER.trace("getWeekCode() method called");

        WeekResolverBean weekResolver = new WeekResolverBean();
        WeekResolverResult result;
        try {
            result = weekResolver.getWeekCode(catalogueCode, date);
        }
        catch( UnsupportedOperationException unsupportedOperationException) {
            LOGGER.error("Unsupported cataloguecode {}", catalogueCode);
            return Response.status(400, "Unsupported cataloguecode").build();
        }
        catch( DateTimeParseException dateTimeParseException ) {
            LOGGER.error("Invalid date {}: {}", date, dateTimeParseException.getCause());
            return Response.status( 400, "Unable to parse the date").build();
        }

        // Return calculated weekcode
        LOGGER.info("getWeekCode returning: {}", result);
        try {
            return Response.ok(jsonbContext.marshall(result), MediaType.APPLICATION_JSON).build();
        }
        catch( JSONBException jsonbException ) {
            LOGGER.error(String.format("Failed to serialize result object: %s", jsonbException.getCause()));
            return Response.status(500, "Internal error when serializing result").build();
        }
    }
}
