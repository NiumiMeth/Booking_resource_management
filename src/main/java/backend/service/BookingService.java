package backend.service;

import backend.booking.dto.BookingApprovalDTO;
import backend.booking.dto.BookingResponseDTO;
import backend.booking.dto.CreateBookingDTO;
import backend.booking.dto.UpdateBookingDTO;
import backend.booking.exception.BookingConflictException;
import backend.booking.exception.BookingNotFoundException;
import backend.booking.exception.InvalidBookingStatusException;
import backend.entity.Booking;
import backend.entity.Resource;
import backend.entity.User;
import backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    // ==================== CONFLICT DETECTION ====================

    /**
     * Check if a booking time slot conflicts with existing approved bookings
     */
    public boolean hasConflict(Long resourceId, LocalDateTime start, LocalDateTime end) {
        List<Booking> conflicts = bookingRepository.findConflicts(resourceId, start, end);
        return !conflicts.isEmpty();
    }

    // ==================== CREATE BOOKING ====================

    /**
     * Create a new booking from DTO
     * Sets initial status to PENDING and validates for conflicts
     */
    public BookingResponseDTO createBooking(CreateBookingDTO dto, User user, Resource resource) {
        // Check for scheduling conflicts
        if (hasConflict(resource.getId(), dto.getStartTime(), dto.getEndTime())) {
            throw new BookingConflictException("Time slot conflicts with existing approved bookings");
        }

        // Create new booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setResource(resource);
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(dto.getEndTime());
        booking.setPurpose(dto.getPurpose());
        booking.setExpectedAttendees(dto.getExpectedAttendees());
        booking.setStatus("PENDING");

        Booking savedBooking = bookingRepository.save(booking);
        return convertToResponseDTO(savedBooking);
    }

    // ==================== READ BOOKINGS ====================

    /**
     * Get booking by ID
     */
    public BookingResponseDTO getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        return convertToResponseDTO(booking);
    }

    /**
     * Get all bookings for a specific user
     */
    public List<BookingResponseDTO> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findUserBookingsByCreationTime(userId);
        return bookings.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings by status
     */
    public List<BookingResponseDTO> getBookingsByStatus(String status) {
        List<Booking> bookings = bookingRepository.findBookingsByStatusOrdered(status);
        return bookings.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for a specific resource
     */
    public List<BookingResponseDTO> getBookingsByResourceId(Long resourceId) {
        List<Booking> bookings = bookingRepository.findByResourceId(resourceId);
        return bookings.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings (admin view)
     */
    public List<BookingResponseDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE BOOKING ====================

    /**
     * Update an existing booking (only allowed if status is PENDING)
     */
    public BookingResponseDTO updateBooking(Long bookingId, UpdateBookingDTO dto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Can only update PENDING bookings
        if (!booking.getStatus().equals("PENDING")) {
            throw new InvalidBookingStatusException(
                    booking.getStatus(),
                    "update"
            );
        }

        // Check for conflicts with new time slot
        if (hasConflict(booking.getResource().getId(), dto.getStartTime(), dto.getEndTime())) {
            throw new BookingConflictException("New time slot conflicts with existing approved bookings");
        }

        // Update booking fields
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(dto.getEndTime());
        booking.setPurpose(dto.getPurpose());
        booking.setExpectedAttendees(dto.getExpectedAttendees());

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToResponseDTO(updatedBooking);
    }

    // ==================== CANCEL BOOKING ====================

    /**
     * Cancel a booking (allowed only if status is PENDING or APPROVED)
     */
    public BookingResponseDTO cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Can only cancel PENDING or APPROVED bookings
        if (!booking.getStatus().equals("PENDING") && !booking.getStatus().equals("APPROVED")) {
            throw new InvalidBookingStatusException(
                    booking.getStatus(),
                    "cancel"
            );
        }

        booking.setStatus("CANCELLED");
        Booking cancelledBooking = bookingRepository.save(booking);
        return convertToResponseDTO(cancelledBooking);
    }

    // ==================== ADMIN APPROVAL WORKFLOW ====================

    /**
     * Approve a booking (admin only, status must be PENDING)
     */
    public BookingResponseDTO approveBooking(Long bookingId, BookingApprovalDTO dto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Can only approve PENDING bookings
        if (!booking.getStatus().equals("PENDING")) {
            throw new InvalidBookingStatusException(
                    booking.getStatus(),
                    "approve"
            );
        }

        // Final conflict check before approval
        if (hasConflict(booking.getResource().getId(), 
                       booking.getStartTime(), 
                       booking.getEndTime())) {
            throw new BookingConflictException("Cannot approve: time slot now conflicts with other bookings");
        }

        booking.setStatus("APPROVED");
        booking.setApprovalReason(dto.getApprovalReason());

        Booking approvedBooking = bookingRepository.save(booking);
        return convertToResponseDTO(approvedBooking);
    }

    /**
     * Reject a booking (admin only, status must be PENDING)
     */
    public BookingResponseDTO rejectBooking(Long bookingId, BookingApprovalDTO dto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Can only reject PENDING bookings
        if (!booking.getStatus().equals("PENDING")) {
            throw new InvalidBookingStatusException(
                    booking.getStatus(),
                    "reject"
            );
        }

        booking.setStatus("REJECTED");
        booking.setApprovalReason(dto.getApprovalReason());

        Booking rejectedBooking = bookingRepository.save(booking);
        return convertToResponseDTO(rejectedBooking);
    }

    // ==================== DELETE BOOKING ====================

    /**
     * Delete a booking (only allowed if status is PENDING or REJECTED)
     */
    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Can only delete PENDING or REJECTED bookings
        if (!booking.getStatus().equals("PENDING") && !booking.getStatus().equals("REJECTED")) {
            throw new InvalidBookingStatusException(
                    booking.getStatus(),
                    "delete"
            );
        }

        bookingRepository.deleteById(bookingId);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Convert Booking entity to BookingResponseDTO
     */
    private BookingResponseDTO convertToResponseDTO(Booking booking) {
        return new BookingResponseDTO(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getName(),
                booking.getResource().getId(),
                booking.getResource().getName(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getExpectedAttendees(),
                booking.getStatus(),
                booking.getApprovalReason(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }

    /**
     * Legacy method for backward compatibility
     */
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }
}