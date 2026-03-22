package backend.booking.exception;

/**
 * Exception thrown when user tries to perform an action they are not authorized for.
 * For example: a user trying to approve/reject a booking (admin-only operation).
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedAccessException() {
        super("User is not authorized to perform this action");
    }
}
