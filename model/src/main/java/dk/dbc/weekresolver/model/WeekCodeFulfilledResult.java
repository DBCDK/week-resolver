package dk.dbc.weekresolver.model;

public class WeekCodeFulfilledResult implements Result {

    private Boolean isFulfilled;

    private String requestedWeekCode;

    private WeekResolverResult currentWeekCodeResult;

    public Boolean getIsFulfilled() {
        return isFulfilled;
    }

    public void setIsFulfilled(Boolean fulfilled) {
        isFulfilled = fulfilled;
    }

    public WeekCodeFulfilledResult withFulfilled(Boolean fulfilled) {
        isFulfilled = fulfilled;
        return this;
    }

    public String getRequestedWeekCode() {
        return requestedWeekCode;
    }

    public void setRequestedWeekCode(String requestedWeekCode) {
        this.requestedWeekCode = requestedWeekCode;
    }

    public WeekCodeFulfilledResult withRequestedWeekCode(String requestedWeekCode) {
        this.requestedWeekCode = requestedWeekCode;
        return this;
    }

    public WeekResolverResult getCurrentWeekCodeResult() {
        return currentWeekCodeResult;
    }

    public void setCurrentWeekCodeResult(WeekResolverResult currentWeekCodeResult) {
        this.currentWeekCodeResult = currentWeekCodeResult;
    }

    public WeekCodeFulfilledResult withCurrentWeekCodeResult(WeekResolverResult currentWeekCodeResult) {
        this.currentWeekCodeResult = currentWeekCodeResult;
        return this;
    }

    @Override
    public String toString() {
        return "WeekCodeFulfilledResult{" +
                "isFulfilled=" + isFulfilled +
                ", requestedWeekCode='" + requestedWeekCode + '\'' +
                ", currentWeekCodeResult=" + currentWeekCodeResult +
                '}';
    }
}
