package backend.booking.exception;

/**
 * Exception thrown when a booking cannot be modified in its current status.
 * For example: trying to update a booking that is already approved/rejected.
 */
public class InvalidBookingStatusException extends RuntimeException {

    public InvalidBookingStatusException(String message) {
        super(message);
    }

    public InvalidBookingStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidBookingStatusException(String currentStatus, String attemptedAction) {
        super("Cannot " + attemptedAction + " a booking with status: " + currentStatus);
    }
}
