package dk.dbc.weekresolver.rest;

import dk.dbc.serviceutils.ServiceStatus;

import javax.ejb.Stateless;

@Stateless
@javax.ws.rs.Path("/")
public class StatusBean implements ServiceStatus {
}