package backend.booking.exception;

/**
 * Exception thrown when a booking time slot conflicts with an existing booking.
 */
public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }

    public BookingConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public BookingConflictException(Long resourceId) {
        super("Booking conflict detected for resource: " + resourceId);
    }
}
