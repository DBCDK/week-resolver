package dk.dbc.weekresolver.service;

import dk.dbc.commons.jsonb.JSONBContext;
import dk.dbc.commons.jsonb.JSONBException;

import dk.dbc.weekresolver.model.WeekCodeConfiguration;
import dk.dbc.weekresolver.model.WeekCodeFulfilledResult;
import dk.dbc.weekresolver.model.WeekResolverQueryParameterDays;
import dk.dbc.weekresolver.model.WeekResolverQueryParameterDisplay;
import dk.dbc.weekresolver.model.WeekResolverResult;
import dk.dbc.weekresolver.model.YearPlanFormat;
import dk.dbc.weekresolver.model.YearPlanResult;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Stateless
@Path("/api")
public class WeekResolverService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverService.class);
    private static final JSONBContext jsonbContext = new JSONBContext();

    public final static String TEXT_CSV = "text/csv";

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
        LOGGER.info("getWeekCode({})", catalogueCode);

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
        LOGGER.info("getWeekCode({}, {})", catalogueCode, date);

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
        LOGGER.info("getCurrentWeekCode({})", catalogueCode);

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
        LOGGER.info("getCurrentWeekCode({}, {})", catalogueCode, date);

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
                                       @PathParam("catalogueCode") final String catalogueCode,
                                       @DefaultValue ("ON") @QueryParam("days") final String days,
                                       @DefaultValue("BKM") @QueryParam("display") final String display) {
        LOGGER.info("getYearPlanForCode({}, {}, {})", format, catalogueCode, days);

        // Avoid week 53 problems by moving to no later than november
        LocalDate now = LocalDate.now();
        if (now.getMonth() == Month.DECEMBER) {
            now = now.minusMonths(1);
        }

        return getYearPlanFromCodeAndYear(format, catalogueCode, now.getYear(),
                days.equals(WeekResolverQueryParameterDays.ON.name()),
                display.equals(WeekResolverQueryParameterDisplay.ALL.name()));
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
    @Produces({MediaType.APPLICATION_JSON, TEXT_CSV, MediaType.TEXT_HTML})
    public Response getYearPlanForCodeAndYear(@PathParam("format") final YearPlanFormat format,
                                              @PathParam("catalogueCode") final String catalogueCode,
                                              @PathParam("year") final Integer year,
                                              @DefaultValue ("ON") @QueryParam("days") final String days,
                                              @DefaultValue("BKM") @QueryParam("display") final String display) {
        LOGGER.info("getYearPlanForCodeAndYear({}, {}, {}, {})", format, catalogueCode, year, days);
        return getYearPlanFromCodeAndYear(format, catalogueCode, year,
                days.equals(WeekResolverQueryParameterDays.ON.name()),
                display.equals(WeekResolverQueryParameterDisplay.ALL.name()));
    }

    /**
     * Endpoint for getting a list of weekcodes for a range of days
     *
     * @param catalogueCode Cataloguecode
     * @param start Start date
     * @return a HTTP 200 with a csv document containing the year plan
     * @throws UnsupportedOperationException if the specified cataloguecode is unkown or unsupported
     */
    @GET
    @Path("v1/day/{catalogueCode}/{start}/{end}")
    @Produces({MediaType.APPLICATION_JSON, TEXT_CSV, MediaType.TEXT_HTML})
    public Response getDayPlan(@PathParam("catalogueCode") final String catalogueCode,
                               @PathParam("start") final String start,
                               @PathParam("end") final String end) {
        LOGGER.info("getDayPlan({}, {}, {})", catalogueCode, start, end);
        return getDayPlanFromDateToDate(catalogueCode, start, end);
    }

    /**
     * Endpoint for checking if a weekcode has been fulfilled (current weekcode is same or newer)
     *
     * @param weekCode Week code
     * @return a HTTP 200 with a WeekCodeFulfilledResult object
     * @throws UnsupportedOperationException if the specified catalogue code is unknown or unsupported
     */
    @GET
    @Path("v1/fulfilled/{weekCode}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getWeekCodeFulfilled(@PathParam("weekCode") final String weekCode) {
        LOGGER.info("getWeekCodeFulfilled({})", weekCode);

        if (weekCode == null || weekCode.length() != 9) {
            LOGGER.error("Incorrect weekcode in request to getWeekCodeFulfilled({})", weekCode);
            return Response.status(400, "Invalid week code").build();
        }

        try {
            WeekResolverResult currentResult = new WeekResolver(timeZone)
                    .withDate(LocalDate.now().toString())
                    .withCatalogueCode(weekCode.substring(0, 3).toUpperCase())
                    .getCurrentWeekCode();
            LOGGER.debug("Current weekcode for {} is {}", currentResult.getCatalogueCode(), currentResult.getWeekCode());

            // Extract weeks and compare them
            Integer currentYearWeek = Integer.parseInt(currentResult.getWeekCode().substring(3));
            Integer requestedYearWeek = Integer.parseInt(weekCode.substring(3));

            LOGGER.debug("Checking if (current) {} is equal to or later than (requested) {}", currentYearWeek, requestedYearWeek);
            WeekCodeFulfilledResult result = new WeekCodeFulfilledResult()
                    .withRequestedWeekCode(weekCode.toUpperCase())
                    .withCurrentWeekCodeResult(currentResult)
                    .withFulfilled(currentYearWeek >= requestedYearWeek);
            LOGGER.info("Requested week code {} is fulfilled = {}", weekCode.toUpperCase(), result.getIsFulfilled());
            return Response.ok(jsonbContext.marshall(result), MediaType.APPLICATION_JSON).build();
        } catch( UnsupportedOperationException unsupportedOperationException) {
            LOGGER.error("Unsupported cataloguecode {}", weekCode.substring(0, 3));
            return Response.status(400, "Unsupported cataloguecode").build();
        } catch( DateTimeParseException dateTimeParseException ) {
            LOGGER.error("Invalid date {}: {}", LocalDate.now().toString(), dateTimeParseException.getCause());
            return Response.status( 400, "Unable to parse the date").build();
        } catch (JSONBException jsonbException) {
            LOGGER.error(String.format("Failed to serialize result object: %s", jsonbException.getCause()));
            return Response.status(500, "Internal error when serializing result").build();
        }
    }

    /**
     * Endpoint for getting current code configuration
     *
     * @return a HTTP 200 with the configuration
     */
    @GET
    @Path("v1/codes")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCodes() throws JSONBException {
        LOGGER.info("getCodes()");

        SortedMap<String, WeekCodeConfiguration> sortedMap = new TreeMap<>();
        for (String code : WeekResolver.CODES.keySet().stream().sorted().collect(Collectors.toCollection(ArrayList::new))) {
            sortedMap.putIfAbsent(code, WeekResolver.CODES.get(code));
        }

        return Response.ok(jsonbContext.marshall(sortedMap), MediaType.APPLICATION_JSON).build();
    }

    /**
     * Get week code based on catalogCode and a date
     * @param date Date
     * @param catalogueCode Catalogue code
     * @return A weekcode result on success
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
     * @param date Date
     * @param catalogueCode Catalogue code
     * @return A weekcode result on success
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
     * @param format Format
     * @param catalogueCode  Catalogue code
     * @param year Year
     * @return A year plan on success
     */
    private Response getYearPlanFromCodeAndYear(final YearPlanFormat format, final String catalogueCode, final Integer year,
                                                Boolean showAbnormalDayNames, Boolean displayAllDates) {
        YearPlanResult result;

        try {
            result = new WeekResolver(timeZone)
                    .withCatalogueCode(catalogueCode)
                    .getYearPlan(year, showAbnormalDayNames, displayAllDates);

            if (format == YearPlanFormat.JSON) {
                return Response.ok(jsonbContext.marshall(result), MediaType.APPLICATION_JSON).build();
            } else if (format == YearPlanFormat.CSV) {
                String csv = CsvFormatter.format(result);
                return Response.ok((csv), TEXT_CSV).build();
            } else if (format == YearPlanFormat.HTML) {
                String html = HtmlFormatter.format(result);
                return Response.ok((html), MediaType.TEXT_HTML).build();
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

    private Response getDayPlanFromDateToDate(final String catalogueCode, final String start, final String end) {
        Map<String, String> days = new LinkedHashMap<>();

        try {
            WeekResolver wr = new WeekResolver(timeZone).withCatalogueCode(catalogueCode);

            LocalDate startDate = wr.fromString(start);
            LocalDate endDate = wr.fromString(end);

            while (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
                days.put(wr.rowContentFromDate(wr.fromLocalDate(startDate)).getContent(), wr.getWeekCode(startDate).getWeekCode());
                startDate = startDate.plusDays(1);
            }
            return Response.ok(jsonbContext.marshall(days), MediaType.APPLICATION_JSON).build();
        }
        catch( UnsupportedOperationException unsupportedOperationException) {
            LOGGER.error("Unsupported cataloguecode {}", catalogueCode);
            return Response.status(400, "Unsupported cataloguecode").build();
        }
        catch( DateTimeParseException dateTimeParseException ) {
            LOGGER.error("Invalid date {} and/or {}: {}", start, end, dateTimeParseException.getCause());
            return Response.status( 400, "Unable to parse the dates").build();
        }
        catch( JSONBException jsonbException ) {
            LOGGER.error(String.format("Failed to serialize result object: %s", jsonbException.getCause()));
            return Response.status(500, "Internal error when serializing result").build();
        }
    }
}
