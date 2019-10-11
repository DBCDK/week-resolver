package dk.dbc.weekresolver.rest;

/**
 * Result data from resolving a weeknumber by use of a specific catalogue code
 */
public class WeekResolverResult {

    // ISO week number
    public int WeekNumber;

    // The given cataloguecode, in upper case
    public String CatalogueCode;

    // The resulting weekcode ready for insertion into records
    public String WeekCode;
}
