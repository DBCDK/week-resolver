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
     * @throws ParseException if the specified cataloguecode is unkown or unsupported
     *
     */
    @GET
    @Path("v1/date/{catalogueCode}/{date}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getWeekCode(@PathParam("catalogueCode") final String catalogueCode,
                              @PathParam("date") final String date) throws DateTimeParseException, ParseException {
        LOGGER.trace("getWeekCode() method called");
        LOGGER.info("Week-code requested for catalogueCode={} and date={}", catalogueCode, date);

        // Todo: We are not entirely sure that this is the dateformat we want to use.
        //       Adjust when clients are better known.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateForRequestedWeekCode = LocalDate.parse(date, formatter);

        // Todo: Calculate the weekcode, here needs to be added logic based on input from ACC.
        //       Throw ParseException if the cataloguecode is unknown or unsupported
        final String weekCode = String.format("No week id implemented yet for catalogueCode: %s date:%s", catalogueCode, dateForRequestedWeekCode);

        // Return calculated weekcode
        LOGGER.info("getWeekCode returning: {}", weekCode);
        return Response.ok(weekCode).build();
    }
}
