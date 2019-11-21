/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.weekresolver.service;

import dk.dbc.jsonb.JSONBContext;
import dk.dbc.weekresolver.WeekResolverResult;
import dk.dbc.weekresolver.WeekresolverConnectorException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dk.dbc.weekresolver.WeekresolverConnector;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class WeekresolverResourceIT extends AbstractWeekresolverServiceContainerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeekresolverResourceIT.class);

    private final JSONBContext jsonbContext = new JSONBContext();

    @Test
    void getWeekCode_standardDate() throws WeekresolverConnectorException {
        LOGGER.info("Using this url:{}", weekresolverServiceBaseUrl);
        WeekresolverConnector connector = new WeekresolverConnector(httpClient, weekresolverServiceBaseUrl);
        WeekResolverResult w = connector.getWeekCode("DPF", LocalDate.parse("2019-10-20"));
        assertThat(w.getWeekCode(), is("DPF201945"));
        assertThat(w.getYear(), is(2019));
        assertThat(w.getWeekNumber(), is(45));

        WeekResolverResult w1 = connector.getWeekCode("DPF", LocalDate.parse("2019-12-21"));
        assertThat(w1.getWeekCode(), is("DPF202002"));
        assertThat(w1.getYear(), is(2020));
        assertThat(w1.getWeekNumber(), is(2));
    }

    @Test
    void getWeekCode_todaysDate() throws WeekresolverConnectorException {
        LOGGER.info("Using this url:{}", weekresolverServiceBaseUrl);
        WeekresolverConnector connector = new WeekresolverConnector(httpClient, weekresolverServiceBaseUrl);
        WeekResolverResult w = connector.getWeekCode("DPF");
        assertThat(w.getCatalogueCode(), is("DPF"));
    }
}