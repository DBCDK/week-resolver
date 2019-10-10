package dk.dbc.weekresolver.ejb;

import dk.dbc.jsonb.JSONBContext;

import java.text.ParseException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.ejb.Stateless;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Path("/api")
public class WeekResolverBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverBean.class);
    private static final JSONBContext jsonbContext = new JSONBContext();

    /**
     * Get week id based on type(to be elaborated) and a date.
     *
     * @param catalogueCode
     * @param date (yyyy-MM-dd)
     * @return a HTTP 200 with the week-code as a string
     * @throws DateTimeParseException if specified date is not parseable
     * @throws UnsupportedOperationException if the specified cataloguecode is unkown or unsupported
     *
     */
    @GET
    @Path("v1/date/{catalogueCode}/{date}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getWeekCode(@PathParam("catalogueCode") final String catalogueCode,
                              @PathParam("date") final String date) throws DateTimeParseException, UnsupportedOperationException {
        LOGGER.trace("getWeekCode() method called");
        LOGGER.info("Week-code requested for catalogueCode={} and date={}", catalogueCode, date);

        // Todo: We are not entirely sure that this is the dateformat we want to use.
        //       Adjust when clients are better known.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateForRequestedWeekCode = LocalDate.parse(date, formatter);

        // Calculate the weekcode
        final String weekCode = CalculateWeekCodeForCatalogueCode(catalogueCode, dateForRequestedWeekCode);

        // Return calculated weekcode
        LOGGER.info("getWeekCode returning: {}", weekCode);
        return Response.ok(weekCode).build();
    }

    /**
     * Calculate the weekcode for the given date depending on the cataloguecode
     * @param catalogueCode
     * @param date
     * @return a string with the weekcode
     * @throws ParseException if the cataloguecode is not supported
     */
    private String CalculateWeekCodeForCatalogueCode(final String catalogueCode, final LocalDate date)
        throws UnsupportedOperationException {
        LOGGER.info("Calculating weekcode for catalogueCode={} and date={}", catalogueCode, date);

        switch( catalogueCode.toLowerCase() ) {
            case "bpf": return "1946"; // Todo: calculate date+2weeks untill we have the final logic
            default: throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }
    }
}
