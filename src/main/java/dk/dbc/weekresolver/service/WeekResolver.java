/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 *  See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

package dk.dbc.weekresolver.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeekResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekResolver.class);

    private LocalDate date = LocalDate.now();
    private String catalogueCode = "";

    public WeekResolver withDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.date = LocalDate.parse(date, formatter);
        return this;
    }

    public WeekResolver withCatalogueCode(String catalogueCode) {
        this.catalogueCode = catalogueCode;
        return this;
    }

    /**
     * Calculate the weekcode for the given date depending on the cataloguecode
     *
     * @return a string with the weekcode
     * @throws UnsupportedOperationException if the cataloguecode is not supported
     */
    public WeekResolverResult build() throws UnsupportedOperationException {
        LOGGER.info("Calculating weekcode for catalogueCode={} and date={}", catalogueCode, date);

        // Adjust date to match the given cataloguecode
        LocalDate forwardDate;
        switch (catalogueCode.toLowerCase()) {
            case "bpf":
                forwardDate = date.plusWeeks(2);
                break;
            default:
                throw new UnsupportedOperationException(String.format("Cataloguecode %s is not supported", catalogueCode));
        }

        // Adjust the expected release date for christmas, easter and other non-working days
        LocalDate adjustedDate = getDateAdjustedForRestrictions(forwardDate);

        // Build final result
        WeekResolverResult result = new WeekResolverResult()
                .withDate(adjustedDate)
                .withCatalogueCode(catalogueCode.toUpperCase());
        return result.build();
    }

    /**
     * Calculate the next possible date for release of this record, taking into account
     * christmas, easter and other restricted dates as well as manually added restricted dates.
     * @param expectedDate The calendar date expected as release date by simple forward adjusting today's date
     * @return The actual release date
     */
    private LocalDate getDateAdjustedForRestrictions(LocalDate expectedDate) {
        LOGGER.info("Expected release date is {}", expectedDate);

        // Todo, make sure that the date choosen is a working day
        // 1) Adjust for easter, christmas and a few other selected dayes
        // 2) Adjust for manually added restrictions
        LocalDate adjustedDate = expectedDate;

        LOGGER.info("Adjusted release date {}", adjustedDate);
        return adjustedDate;
    }
}
