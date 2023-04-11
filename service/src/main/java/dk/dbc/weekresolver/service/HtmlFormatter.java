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
        builder.append(formatHtmlRow(result.getRows().get(0), true));
        result.getRows().stream().skip(1).forEach(r -> builder.append(formatHtmlRow(r, false)));
        builder.append("    </table>\n");
        builder.append("  </body>\n");

        // End document
        builder.append("</html>\n");
        return builder.toString();
    }

    private static String formatHtmlRow(YearPlanResult.YearPlanRow row, boolean isHeader) {
        StringBuilder builder = new StringBuilder();

        builder.append("      <tr>\n");
        row.getColumns().forEach(c ->
                builder.append(formatHtmlColumn(c
                        .replaceAll("\"", "")
                        .replaceAll(" ", "&nbsp;"), isHeader)));
        builder.append("      </tr>\n");

        return builder.toString();
    }

    private static String formatHtmlColumn(String content, boolean isHeader) {
        StringBuilder builder = new StringBuilder();
        boolean modified = !isHeader && content.length() > "yyyy-mm-dd".length();

        builder.append("      <td>\n");
        builder.append("        <p class=" + (modified ? "modified": "normal") + ">");
        builder.append(isHeader ? "<b>" : "");
        builder.append("<nobr>" + (content.length() == 0 ? "---" : content) + "</nobr>");
        builder.append(isHeader ? "</b>" : "");
        builder.append("</p>\n");
        builder.append("      </td>\n");

        return builder.toString();
    }
}
