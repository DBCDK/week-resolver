package dk.dbc.weekresolver.rest;

import dk.dbc.serviceutils.ServiceStatus;
import javax.ws.rs.Path;
import javax.ejb.Stateless;

@Stateless
@Path("/")
public class StatusBean implements ServiceStatus {
}