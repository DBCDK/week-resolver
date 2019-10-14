package dk.dbc.weekresolver.ejb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.format.DateTimeParseException;

public class WeekResolverBeanTest {

    @Test
    public void TestInvalidCatalogueCode() {
        WeekResolverBean b = new WeekResolverBean()
                .forDate("2019-11-29")
                .withCatalogueCode("qxz");
        assertThrows(UnsupportedOperationException.class,() -> b.getWeekCode());
    }

    @Test
    public void TestInvalidDates() {
        assertThrows(DateTimeParseException.class, () -> new WeekResolverBean().forDate("2019-13-29"));
        assertThrows(DateTimeParseException.class, () -> new WeekResolverBean().forDate("11-29"));
    }

    @Test
    public void TestValidCatalogueCodeLowerCase() {
        WeekResolverBean b = new WeekResolverBean()
                .forDate("2019-11-29");
        assertDoesNotThrow(() -> b.withCatalogueCode("bpf").getWeekCode());
        assertDoesNotThrow(() -> b.withCatalogueCode("BPF").getWeekCode());
    }

    @Test
    public void TestCatalogueCodeBPF() {
        WeekResolverBean b = new WeekResolverBean()
                .withCatalogueCode("bpf");

        assertDoesNotThrow(() -> b.forDate("2019-11-29").getWeekCode());

        WeekResolverResult result = b.forDate("2019-11-29").getWeekCode();
        assertThat(result.getWeekNumber(), is(48));
        assertThat(result.getYear(), is(2019));
        assertThat(result.getCatalogueCode(), is("BPF"));
        assertThat(result.getWeekCode(), is("BPF201948"));

        result = b.forDate("2019-12-29").getWeekCode();
        assertThat(result.getWeekNumber(), is(1));
        assertThat(result.getYear(), is(2020));
        assertThat(result.getCatalogueCode(), is("BPF"));
        assertThat(result.getWeekCode(), is("BPF202001"));
    }
}
