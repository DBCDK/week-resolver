package dk.dbc.weekresolver.service;

import dk.dbc.weekresolver.model.YearPlanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.stream.Collectors;

public class CsvFormatter {

    private final static String CSV_SEPARATOR = ";";

    public static String format(YearPlanResult result) {
        return result.getRows().stream()
                .map(row -> row.getColumns().stream()
                        .filter(YearPlanResult.YearPlanRowColumn::getIsVisible)
                        .map(YearPlanResult.YearPlanRowColumn::getContent)
                        .collect(Collectors.joining(CSV_SEPARATOR)))
                .collect(Collectors.joining("\n"));
    }
}
