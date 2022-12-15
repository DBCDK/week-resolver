package dk.dbc.weekresolver.service;

import dk.dbc.jsonb.JSONBContext;
import dk.dbc.jsonb.JSONBException;

import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Path("/api")
public class WeekResolverService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverService.class);
    private static final JSONBContext jsonbContext = new JSONBContext();

    @Inject
    @ConfigProperty(name = "TZ")
    String timeZone;

    /**
     * Endpoint for getting the week code based on catalogueCode and todays date
     *
     * @param catalogueCode Cataloguecode
     * @return a HTTP 200 with the week-code as a string
     * @throws DateTimeParseException        if specified date is not parseable (should not be possible)
     * @throws UnsupportedOperationException if the specified cataloguecode is unkown or unsupported
     */
    @GET
    @Path("v1/date/{catalogueCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeekCode(@PathParam("catalogueCode") final String catalogueCode) {
        LOGGER.trace("getWeekCode() method called");

        return getWeekCodeFromDate(LocalDate.now().toString(), catalogueCode);
    }

    /**
     * Endpoint for getting the week code based on catalogueCode and a date.
     *
     * @param catalogueCode Cataloguecode
     * @param date (yyyy-MM-dd)
     * @return a HTTP 200 with the week-code as a string
     * @throws DateTimeParseException        if specified date is not parseable
     * @throws UnsupportedOperationException if the specified cataloguecode is unkown or unsupported
     */
    @GET
    @Path("v1/date/{catalogueCode}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeekCode(@PathParam("catalogueCode") final String catalogueCode,
                                @PathParam("date") final String date) {
        LOGGER.trace("getWeekCode() method called with specific date");

        return getWeekCodeFromDate (date, catalogueCode);
    }

    /**
     * Get week id based on catalogCode and a date
     * @param date
     * @param catalogueCode
     * @return
     */
    private Response getWeekCodeFromDate(final String date, final String catalogueCode) {
        WeekResolverResult result;

        try {
            result = new WeekResolver(timeZone)
                    .withDate(date)
                    .withCatalogueCode(catalogueCode)
                    .build();
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
        LOGGER.info("Calculated weekcode by use of cataloguecode {} is {}", result.getCatalogueCode(), result.getWeekCode());
        try {
            return Response.ok(jsonbContext.marshall(result), MediaType.APPLICATION_JSON).build();
        }
        catch( JSONBException jsonbException ) {
            LOGGER.error(String.format("Failed to serialize result object: %s", jsonbException.getCause()));
            return Response.status(500, "Internal error when serializing result").build();
        }
    }
}
