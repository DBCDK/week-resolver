/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.weekresolver.rest;

/**
 * Result data from resolving a weeknumber by use of a specific catalogue code
 */
public class WeekResolverResult {

    // Week number
    public int WeekNumber;

    // Year
    public int Year;

    // The given cataloguecode, in upper case
    public String CatalogueCode;

    // The resulting weekcode ready for insertion into records
    public String WeekCode;
}
