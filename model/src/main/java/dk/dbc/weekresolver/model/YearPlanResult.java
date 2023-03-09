package dk.dbc.weekresolver.model;

import java.util.ArrayList;
import java.util.List;

public class YearPlanResult {

    public YearPlanResult() {
        rows = new ArrayList<>();
    }

    public class YearPlanRow {
        private List<String> columns;

        YearPlanRow(List<String> columns) {
            this.columns = columns;
        }

        public List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            this.columns = columns;
        }

        public YearPlanRow withColumns(List<String> columns) {
            this.columns = columns;
            return this;
        }

        @Override
        public String toString() {
            return "YearPlanRow{" +
                    "columns=" + columns +
                    '}';
        }
    }

    private List<YearPlanRow> rows;

    public List<YearPlanRow> getRows() {
        return rows;
    }

    public void setRows(List<YearPlanRow> rows) {
        this.rows = rows;
    }

    public YearPlanResult withRows(List<YearPlanRow> rows) {
        this.rows = rows;
        return this;
    }

    public void add(List<String> columns) {
        rows.add(new YearPlanRow(columns));
    }

    public int size() {
        return rows.size();
    }

    @Override
    public String toString() {
        return "YearPlanResult{" +
                "rows=" + rows +
                '}';
    }
}
