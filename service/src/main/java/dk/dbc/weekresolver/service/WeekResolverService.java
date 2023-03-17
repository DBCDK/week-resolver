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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    @Produces({MediaType.APPLICATION_JSON, TEXT_CSV, MediaType.TEXT_HTML})
    public Response getYearPlanForCodeAndYear(@PathParam("format") final YearPlanFormat format,
                                              @PathParam("catalogueCode") final String catalogueCode,
                                              @PathParam("year") final Integer year) {
        LOGGER.trace("getYearPlanForCodeAndYear({}, {}, {})", format, catalogueCode, year);
        return getYearPlanFromCodeAndYear(format, catalogueCode, year);
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
        LOGGER.trace("getDayPlan({}, {}, {})", catalogueCode, start, end);
        return getDayPlanFromDateToDate(catalogueCode, start, end);
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
                String csv = getCsvFormattedOutput(result);
                return Response.ok((csv), TEXT_CSV).build();
            } else if (format == YearPlanFormat.HTML) {
                String html = getHtmlFormattedOutput(result);
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
                days.put(wr.stringFromDate(wr.fromLocalDate(startDate)), wr.getWeekCode(startDate).getWeekCode());
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

    private String getCsvFormattedOutput(YearPlanResult result) {
        return result.getRows().stream()
                .map(row -> String.join(CSV_SEPARATOR, row.getColumns()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Poor man's html formatter, mostly for testing and internal use
     * @param result
     * @return string containing html document
     */
    private String getHtmlFormattedOutput(YearPlanResult result) {
        StringBuilder builder= new StringBuilder();

        final List<String> styles = List.of(
                "table {border:  solid 1px #000000;border-collapse: collapse}",
                "tr {border: solid 2px #000000}",
                "td {border:  solid 1px #aaaaaa; padding-left: 15px; padding-right: 15px; font-family: monospace; font-size: 10pt; text-align:right}",
                "p.normal {color:black}",
                "p.modified {color:darkred}");

        // Document begin and minimal stylesheet
        builder.append("<html>\n");
        builder.append("  <head>\n");
        builder.append("    <title>Årsplan ").append(result.getYear()).append("</title>\n");
        builder.append("    <style>\n");
        builder.append(styles.stream().map(s -> "      " + s + "\n").collect(Collectors.joining()));
        builder.append("    </style>\n");
        builder.append("    <meta charset=\"UTF-8\">\n");
        builder.append("  </head>\n");

        // Body
        builder.append("  <body>\n");
        builder.append("    <table>\n");
        builder.append(formatHtmlRow(result.getRows().get(0), true));
        result.getRows().stream().skip(1).forEach(r -> builder.append(formatHtmlRow(r, false)));
        builder.append("    </table>\n");
        builder.append("  </body>\n");

        // End document
        builder.append("</html>\n");
        return builder.toString();
    }

    private String formatHtmlRow(YearPlanResult.YearPlanRow row, boolean isHeader) {
        StringBuilder builder = new StringBuilder();

        builder.append("      <tr>\n");
        row.getColumns().forEach(c ->
                builder.append(formatHtmlColumn(c
                        .replaceAll("\"", "")
                        .replaceAll(" ", "&nbsp;"), isHeader)));
        builder.append("      </tr>\n");

        return builder.toString();
    }

    private String formatHtmlColumn(String content, boolean isHeader) {
        StringBuilder builder = new StringBuilder();
        boolean modified = !isHeader && content.length() > "yyyy-mm-dd".length();

        builder.append("      <td>\n");
        builder.append("        <p class=" + (modified ? "modified": "normal") + ">");
        builder.append(isHeader ? "<b>" : "");
        builder.append("<nobr>" + (content.length() == 0 ? "---" : content) + "</nobr>");
        builder.append(isHeader ? "</b>" : "");
        builder.append("</p>\n");
        builder.append("      </td>\n");

        return builder.toString();
    }
}
