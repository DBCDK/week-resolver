package dk.dbc.weekresolver.ejb;


import dk.dbc.jsonb.JSONBContext;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;

@Stateless
@Path("")
public class WeekResolverBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolverBean.class);
    private static final JSONBContext jsonbContext = new JSONBContext();

    /**
     * Get week id based on type(to be elaborated) and a date.
     *
     * @param dateType   (todo: add types of week)
     * @param date (yyyy-MM-dd)
     * @return a HTTP 200 with the week-id as a string
     * @throws ParseException if specified date is not parseable
     *
     * todo: implement !
     */
    @GET
    @Path("api/v1/date/{type}/{date}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getWeekId(@PathParam("type") final String dateType,
                              @PathParam("date") final String date) throws ParseException {
        LOGGER.trace("getWeekId() method called");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date d = sdf.parse(date);
            final String weekId = String.format("No week id implemented yet for type: %s date:%s", dateType, d);
            LOGGER.info("getWeekId called. Returning:{}",weekId);
            return Response.ok(weekId).build();
        } finally {

        }
    }
}
