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
        WeekResolverBean b = new WeekResolverBean();
        assertThrows(UnsupportedOperationException.class,() -> b.getWeekCode("qxz", "2019-11-29"));
    }

    @Test
    public void TestInvalidDate() {
        WeekResolverBean b = new WeekResolverBean();
        assertThrows(DateTimeParseException.class,() -> b.getWeekCode("bpf", "2019-13-29"));
        assertThrows(DateTimeParseException.class,() -> b.getWeekCode("bpf", "2019-11-32"));
        assertThrows(DateTimeParseException.class,() -> b.getWeekCode("bpf", "11-29"));
    }

    @Test
    public void TestValidCatalogueCode() {
        WeekResolverBean b = new WeekResolverBean();
        assertDoesNotThrow(() -> b.getWeekCode("bpf", "2019-11-29"));
        assertDoesNotThrow(() -> b.getWeekCode("BPF", "2019-11-29"));
    }

    @Test
    public void TestCatalogueCodeBPF() {
        WeekResolverBean b = new WeekResolverBean();

        assertDoesNotThrow(() -> b.getWeekCode("bpf", "2019-11-29"));

        WeekResolverResult result = b.getWeekCode("bpf", "2019-11-29");
        assertThat(result.getWeekNumber(), is(48));
        assertThat(result.getYear(), is(2019));
        assertThat(result.getCatalogueCode(), is("BPF"));
        assertThat(result.getWeekCode(), is("BPF201948"));

        result = b.getWeekCode("bpf", "2019-12-29");
        assertThat(result.getWeekNumber(), is(1));
        assertThat(result.getYear(), is(2020));
        assertThat(result.getCatalogueCode(), is("BPF"));
        assertThat(result.getWeekCode(), is("BPF202001"));
    }
}
