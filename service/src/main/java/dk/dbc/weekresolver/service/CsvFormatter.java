package dk.dbc.weekresolver.service;

import dk.dbc.weekresolver.model.YearPlanResult;

import java.util.stream.Collectors;

public class CsvFormatter {

    private final static String CSV_SEPARATOR = ";";

    public static String format(YearPlanResult result) {
        return result.getRows().stream()
                .map(row -> row.getColumns().stream()
                        .map(YearPlanResult.YearPlanRowColumn::getContent)
                        .collect(Collectors.joining(CSV_SEPARATOR)))
                .collect(Collectors.joining("\n"));
    }
}
