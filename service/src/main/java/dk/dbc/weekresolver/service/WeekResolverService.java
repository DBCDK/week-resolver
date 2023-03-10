package dk.dbc.weekresolver.service;

import dk.dbc.commons.jsonb.JSONBContext;
import dk.dbc.commons.jsonb.JSONBException;

import dk.dbc.weekresolver.model.WeekResolverResult;
import dk.dbc.weekresolver.model.YearPlanFormat;
import dk.dbc.weekresolver.model.YearPlanResult;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

@Path("/api")
public class WeekResolverService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverService.class);
    private static final JSONBContext jsonbContext = new JSONBContext();

    public final static String TEXT_CSV = "text/csv";

    private final static String CSV_SEPARATOR = ";";

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
        LOGGER.trace("getWeekCode({})", catalogueCode);

        return getWeekCodeFromDate(catalogueCode, LocalDate.now().toString());
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
    public Response getWeekCodeForDate(@PathParam("catalogueCode") final String catalogueCode,
                                @PathParam("date") final String date) {
        LOGGER.trace("getWeekCode({}, {})", catalogueCode, date);

        return getWeekCodeFromDate(catalogueCode, date);
    }

    /**
     * Endpoint for getting the current week code based on catalogueCode and todays date
     *
     * @param catalogueCode Cataloguecode
     * @return a HTTP 200 with the week-code as a string
     * @throws DateTimeParseException        if specified date is not parseable
     * @throws UnsupportedOperationException if the specified cataloguecode is unkown or unsupported
     */
    @GET
    @Path("v1/current/{catalogueCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentWeekCode(@PathParam("catalogueCode") final String catalogueCode) {
        LOGGER.trace("getCurrentWeekCode({})", catalogueCode);

        return getCurrentWeekCodeFromDate(catalogueCode, LocalDate.now().toString());
    }

    /**
     * Endpoint for getting the current week code based on catalogueCode and a date
     *
     * @param catalogueCode Cataloguecode
     * @return a HTTP 200 with the week-code as a string
     * @throws DateTimeParseException        if specified date is not parseable
     * @throws UnsupportedOperationException if the specified cataloguecode is unkown or unsupported
     */
    @GET
    @Path("v1/current/{catalogueCode}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentWeekCodeForDate(@PathParam("catalogueCode") final String catalogueCode,
                                              @PathParam("date") final String date) {
        LOGGER.trace("getCurrentWeekCode({}, {})", catalogueCode, date);

        return getCurrentWeekCodeFromDate(catalogueCode, date);
    }

    /**
     * Endpoint for getting a year plan for the given code and the current year
     *
     * @param catalogueCode Cataloguecode
     * @return a HTTP 200 with a csv document containing the year plan
     * @throws UnsupportedOperationException if the specified cataloguecode is unkown or unsupported
     */
    @GET
    @Path("v1/year/{format}/{catalogueCode}")
    @Produces({MediaType.APPLICATION_JSON, TEXT_CSV})
    public Response getYearPlanForCode(@PathParam("format") final YearPlanFormat format,
                                       @PathParam("catalogueCode") final String catalogueCode) {
        LOGGER.trace("getYearPlanForCode({}, {})", format, catalogueCode);

        // Avoid week 53 problems by moving to no later than november
        LocalDate now = LocalDate.now();
        if (now.getMonth() == Month.DECEMBER) {
            now = now.minusMonths(1);
        }

        return getYearPlanFromCodeAndYear(format, catalogueCode, now.getYear());
    }


    /**
     * Endpoint for getting a year plan for the given code and year
     *
     * @param catalogueCode Cataloguecode
     * @param year Year
     * @return a HTTP 200 with a csv document containing the year plan
     * @throws UnsupportedOperationException if the specified cataloguecode is unkown or unsupported
     */
    @GET
    @Path("v1/year/{format}/{catalogueCode}/{year}")
    @Produces({MediaType.APPLICATION_JSON, TEXT_CSV})
    public Response getYearPlanForCodeAndYear(@PathParam("format") final YearPlanFormat format,
                                              @PathParam("catalogueCode") final String catalogueCode,
                                              @PathParam("year") final Integer year) {
        LOGGER.trace("getYearPlanForCodeAndYear({}, {}, {})", format, catalogueCode, year);
        return getYearPlanFromCodeAndYear(format, catalogueCode, year);
    }

    /**
     * Get week code based on catalogCode and a date
     * @param date
     * @param catalogueCode
     * @return
     */
    private Response getWeekCodeFromDate(final String catalogueCode, final String date) {
        WeekResolverResult result;

        try {
            result = new WeekResolver(timeZone)
                    .withDate(date)
                    .withCatalogueCode(catalogueCode)
                    .getWeekCode();

            LOGGER.info("Calculated weekcode by use of cataloguecode {} is {}", result.getCatalogueCode(), result.getWeekCode());
            return Response.ok(jsonbContext.marshall(result), MediaType.APPLICATION_JSON).build();
        }
        catch( UnsupportedOperationException unsupportedOperationException) {
            LOGGER.error("Unsupported cataloguecode {}", catalogueCode);
            return Response.status(400, "Unsupported cataloguecode").build();
        }
        catch( DateTimeParseException dateTimeParseException ) {
            LOGGER.error("Invalid date {}: {}", date, dateTimeParseException.getCause());
            return Response.status( 400, "Unable to parse the date").build();
        }
        catch( JSONBException jsonbException ) {
            LOGGER.error(String.format("Failed to serialize result object: %s", jsonbException.getCause()));
            return Response.status(500, "Internal error when serializing result").build();
        }
    }

    /**
     * Get current week code based on catalogCode and a date
     * @param date
     * @param catalogueCode
     * @return
     */
    private Response getCurrentWeekCodeFromDate(final String catalogueCode, final String date) {
        WeekResolverResult result;

        try {
            result = new WeekResolver(timeZone)
                    .withDate(date)
                    .withCatalogueCode(catalogueCode)
                    .getCurrentWeekCode();

            LOGGER.info("Calculated weekcode by use of cataloguecode {} is {}", result.getCatalogueCode(), result.getWeekCode());
            return Response.ok(jsonbContext.marshall(result), MediaType.APPLICATION_JSON).build();
        }
        catch( UnsupportedOperationException unsupportedOperationException) {
            LOGGER.error("Unsupported cataloguecode {}", catalogueCode);
            return Response.status(400, "Unsupported cataloguecode").build();
        }
        catch( DateTimeParseException dateTimeParseException ) {
            LOGGER.error("Invalid date {}: {}", date, dateTimeParseException.getCause());
            return Response.status( 400, "Unable to parse the date").build();
        }
        catch( JSONBException jsonbException ) {
            LOGGER.error(String.format("Failed to serialize result object: %s", jsonbException.getCause()));
            return Response.status(500, "Internal error when serializing result").build();
        }
    }

    /**
     * Get year plan based on format, catalogCode and a year
     * @param format
     * @param catalogueCode
     * @param year
     * @return
     */
    private Response getYearPlanFromCodeAndYear(final YearPlanFormat format, final String catalogueCode, final Integer year) {
        YearPlanResult result;

        try {
            result = new WeekResolver(timeZone)
                    .withCatalogueCode(catalogueCode)
                    .getYearPlan(year);

            if (format == YearPlanFormat.JSON) {
                return Response.ok(jsonbContext.marshall(result), MediaType.APPLICATION_JSON).build();
            } else if (format == YearPlanFormat.CSV) {
                String csv = result.getRows().stream()
                        .map(row -> String.join(CSV_SEPARATOR, row.getColumns()))
                        .collect(Collectors.joining("\n"));
                return Response.ok((csv), TEXT_CSV).build();
            } else {
                LOGGER.error("Unsupported format {}", format);
                return Response.status(400, "Unsupported format").build();
            }
        }
        catch( UnsupportedOperationException unsupportedOperationException) {
            LOGGER.error("Unsupported cataloguecode {}", catalogueCode);
            return Response.status(400, "Unsupported cataloguecode").build();
        }
        catch( JSONBException jsonbException ) {
            LOGGER.error(String.format("Failed to serialize result object: %s", jsonbException.getCause()));
            return Response.status(500, "Internal error when serializing result").build();
        }
    }
}
