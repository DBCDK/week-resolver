package dk.dbc.weekresolver.connector;

public class WeekResolverUnexpectedStatusCodeException extends WeekResolverConnectorException {
    private final int statusCode;

    /**
     * Constructs a new exception with the specified detail message
     * <p>
     * The cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message detail message saved for later retrieval by the
     *                {@link #getMessage()} method. May be null.
     *
     * @param statusCode the http status code returned by the REST service
     */
    public WeekResolverUnexpectedStatusCodeException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * @return the status code
     */
    @SuppressWarnings("unused")
    public int getStatusCode() {
        return statusCode;
    }
}
