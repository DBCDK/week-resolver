package dk.dbc.weekresolver.ejb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class WeekResolverBeanTest {

    @Test
    public void TestInvalidCatalogueCode() {
        WeekResolverBean b = new WeekResolverBean();
        Assertions.assertThrows(UnsupportedOperationException.class,() -> b.getWeekCode("qxz", "2019-11-29"));
    }

    @Test
    public void TestInvalidDate() {
        WeekResolverBean b = new WeekResolverBean();
        Assertions.assertThrows(DateTimeParseException.class,() -> b.getWeekCode("bpf", "2019-13-29"));
        Assertions.assertThrows(DateTimeParseException.class,() -> b.getWeekCode("bpf", "2019-11-32"));
        Assertions.assertThrows(DateTimeParseException.class,() -> b.getWeekCode("bpf", "11-29"));
    }

    @Test
    public void TestValidCatalogueCode() {
        WeekResolverBean b = new WeekResolverBean();
        Assertions.assertDoesNotThrow(() -> b.getWeekCode("bpf", "2019-11-29"));
        Assertions.assertDoesNotThrow(() -> b.getWeekCode("BPF", "2019-11-29"));
    }

    @Test
    public void TestCatalogueCodeBPF() {
        WeekResolverBean b = new WeekResolverBean();

        WeekResolverResult result = b.getWeekCode("bpf", "2019-11-29");
        Assertions.assertEquals(result.WeekNumber, 48);
        Assertions.assertEquals(result.Year, 2019);
        Assertions.assertEquals(result.CatalogueCode, "BPF");
        Assertions.assertEquals(result.WeekCode, "BPF201948");

        result = b.getWeekCode("bpf", "2019-12-29");
        Assertions.assertEquals(result.WeekNumber, 1);
        Assertions.assertEquals(result.Year, 2020);
        Assertions.assertEquals(result.CatalogueCode, "BPF");
        Assertions.assertEquals(result.WeekCode, "BPF202001");
    }
}
