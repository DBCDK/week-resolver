package dk.dbc.weekresolver.ejb;

import dk.dbc.weekresolver.service.WeekResolver;
import dk.dbc.weekresolver.service.WeekResolverResult;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.format.DateTimeParseException;

public class WeekResolverTest {

    @Test
    public void TestInvalidCatalogueCode() {
        WeekResolver b = new WeekResolver()
                .withDate("2019-11-29")
                .withCatalogueCode("qxz");
        assertThrows(UnsupportedOperationException.class,() -> b.build());
    }

    @Test
    public void TestInvalidDates() {
        assertThrows(DateTimeParseException.class, () -> new WeekResolver().withDate("2019-13-29"));
        assertThrows(DateTimeParseException.class, () -> new WeekResolver().withDate("11-29"));
    }

    @Test
    public void TestValidCatalogueCodeLowerCase() {
        WeekResolver b = new WeekResolver()
                .withDate("2019-11-29");
        assertDoesNotThrow(() -> b.withCatalogueCode("bpf").build());
        assertDoesNotThrow(() -> b.withCatalogueCode("BPF").build());
    }

    @Test
    public void TestCatalogueCodeBPF() {
        WeekResolver b = new WeekResolver()
                .withCatalogueCode("bpf");

        assertDoesNotThrow(() -> b.withDate("2019-11-29").build());

        WeekResolverResult result = b.withDate("2019-11-29").build();
        assertThat(result.getWeekNumber(), is(48));
        assertThat(result.getYear(), is(2019));
        assertThat(result.getCatalogueCode(), is("BPF"));
        assertThat(result.getWeekCode(), is("BPF201948"));

        result = b.withDate("2019-12-29").build();
        assertThat(result.getWeekNumber(), is(1));
        assertThat(result.getYear(), is(2020));
        assertThat(result.getCatalogueCode(), is("BPF"));
        assertThat(result.getWeekCode(), is("BPF202001"));
    }
}
