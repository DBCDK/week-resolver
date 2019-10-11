/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.weekresolver.rest;

import dk.dbc.serviceutils.ServiceStatus;

import javax.ws.rs.Path;

import javax.ejb.Stateless;

@Stateless
@Path("/")
public class StatusBean implements ServiceStatus {
}
