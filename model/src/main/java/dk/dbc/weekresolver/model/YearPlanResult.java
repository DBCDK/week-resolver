package dk.dbc.weekresolver.model;

import java.util.ArrayList;
import java.util.List;

public class YearPlanResult implements Result {

    public YearPlanResult() {
        rows = new ArrayList<>();
    }

    public static class YearPlanRowColumn {

        private String content;

        private Boolean isHeader;

        private Boolean isAbnormalDay;

        private Boolean isVisible;

        public YearPlanRowColumn() {
            this.content = "";
            this.isHeader = false;
            this.isAbnormalDay = false;
            this.isVisible = true;
        }

        public YearPlanRowColumn(String content) {
            this.content = content;
            this.isHeader = false;
            this.isAbnormalDay = false;
            this.isVisible = true;
        }

        public YearPlanRowColumn(String content, Boolean isAbnormalDay, Boolean isVisible) {
            this.content = content;
            isHeader = false;
            this.isAbnormalDay = isAbnormalDay;
            this.isVisible = isVisible;
        }

        public YearPlanRowColumn(String content, Boolean isAbnormalDay, Boolean isHeader, Boolean isVisible) {
            this.content = content;
            this.isHeader = isHeader;
            this.isAbnormalDay = isAbnormalDay;
            this.isVisible = isVisible;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public YearPlanRowColumn withContent(String content) {
            this.content = content;
            return this;
        }

        public Boolean getHeader() {
            return isHeader;
        }

        public void setHeader(Boolean header) {
            isHeader = header;
        }

        public YearPlanRowColumn withHeader(Boolean header) {
            isHeader = header;
            return this;
        }

        public Boolean getIsAbnormalDay() {
            return isAbnormalDay;
        }

        public void setAbnormalDay(Boolean abnormalDay) {
            isAbnormalDay = abnormalDay;
        }

        public YearPlanRowColumn withAbnormalDay(Boolean abnormalDay) {
            isAbnormalDay = abnormalDay;
            return this;
        }

        public Boolean getIsVisible() {
            return isVisible;
        }

        public void setVisible(Boolean visible) {
            isVisible = visible;
        }

        public YearPlanRowColumn withVisible(Boolean visible) {
            isVisible = visible;
            return this;
        }

        @Override
        public String toString() {
            return "YearPlanRowColumn{" +
                    "content='" + content + '\'' +
                    ", isHeader=" + isHeader +
                    ", isAbnormalDay=" + isAbnormalDay +
                    ", isVisible=" + isVisible +
                    '}';
        }
    }

    public static class YearPlanRow {
        private List<YearPlanRowColumn> columns;

        YearPlanRow() {}

        YearPlanRow(List<YearPlanRowColumn> columns) {
            this.columns = columns;
        }

        public List<YearPlanRowColumn> getColumns() {
            return columns;
        }

        public void setColumns(List<YearPlanRowColumn> columns) {
            this.columns = columns;
        }

        public YearPlanRow withColumns(List<YearPlanRowColumn> columns) {
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

    private String year;

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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public YearPlanResult withYear(String year) {
        this.year = year;
        return this;
    }

    public void add(List<YearPlanRowColumn> columns) {
        rows.add(new YearPlanRow(columns));
    }

    public int size() {
        return rows.size();
    }

    @Override
    public String toString() {
        return "YearPlanResult{" +
                "rows=" + rows +
                ", year='" + year + '\'' +
                '}';
    }
}
