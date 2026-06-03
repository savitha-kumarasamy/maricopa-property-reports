package gov.maricopa.reports.common.error;

import org.springframework.http.HttpStatus;

/** Base exception carrying an HTTP status and a message rendered as {@code {"detail": ...}}. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
