package dk.dbc.weekresolver.service;

import dk.dbc.weekresolver.model.YearPlanResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Poor man's html formatter, mostly for testing and internal use
 */
public class HtmlFormatter {

    public static String format(YearPlanResult result) {
        StringBuilder builder= new StringBuilder();

        final List<String> styles = List.of(
                "table {border:  solid 1px #000000;border-collapse: collapse}",
                "tr {border: solid 2px #000000}",
                "td {border:  solid 1px #aaaaaa; padding-left: 15px; padding-right: 15px; font-family: monospace; font-size: 10pt; text-align:right}",
                "p.normal {color:black}",
                "p.modified {color:darkred}");

        // Document begin and minimal stylesheet
        builder.append("<html>\n");
        builder.append("  <head>\n");
        builder.append("    <title>Årsplan ").append(result.getYear()).append("</title>\n");
        builder.append("    <style>\n");
        builder.append(styles.stream().map(s -> "      " + s + "\n").collect(Collectors.joining()));
        builder.append("    </style>\n");
        builder.append("    <meta charset=\"UTF-8\">\n");
        builder.append("  </head>\n");

        // Body
        builder.append("  <body>\n");
        builder.append("    <table>\n");
        result.getRows().stream().forEach(r -> builder.append(formatHtmlRow(r)));
        builder.append("    </table>\n");
        builder.append("  </body>\n");

        // End document
        builder.append("</html>\n");
        return builder.toString();
    }

    private static String formatHtmlRow(YearPlanResult.YearPlanRow row) {
        StringBuilder builder = new StringBuilder();

        builder.append("      <tr>\n");
        row.getColumns().stream().filter(YearPlanResult.YearPlanRowColumn::getIsVisible).forEach(c ->
                builder.append(formatHtmlColumn(c)));
        builder.append("      </tr>\n");

        return builder.toString();
    }

    private static String formatHtmlColumn(YearPlanResult.YearPlanRowColumn column) {
        StringBuilder builder = new StringBuilder();

        String content = column.getContent()
                .replaceAll("\"", "")
                .replaceAll(" ", "&nbsp;");

        builder.append("      <td>\n");
        builder.append("        <p class=").append(column.getIsAbnormalDay() ? "modified" : "normal").append(">");
        builder.append(column.getHeader() ? "<b>" : "");
        builder.append("<nobr>").append(content.length() == 0 ? "---" : content).append("</nobr>");
        builder.append(column.getHeader() ? "</b>" : "");
        builder.append("</p>\n");
        builder.append("      </td>\n");

        return builder.toString();
    }
}
