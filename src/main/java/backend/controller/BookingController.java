package backend.controller;

import backend.booking.dto.BookingApprovalDTO;
import backend.booking.dto.BookingResponseDTO;
import backend.booking.dto.CreateBookingDTO;
import backend.booking.dto.UpdateBookingDTO;
import backend.entity.Resource;
import backend.entity.User;
import backend.repository.ResourceRepository;
import backend.repository.UserRepository;
import backend.service.BookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Booking management operations.
 * Handles user booking requests and admin approval workflows.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    // ==================== CREATE BOOKING ====================

    /**
     * Create a new booking
     * POST /api/bookings
     * 
     * @param dto CreateBookingDTO with user, resource, time slot, and purpose
     * @return Created booking details
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingDTO dto) {
        try {
            // Validate and load user
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate and load resource
            Resource resource = resourceRepository.findById(dto.getResourceId())
                    .orElseThrow(() -> new RuntimeException("Resource not found"));

            // Create booking via service
            BookingResponseDTO booking = bookingService.createBooking(dto, user, resource);

            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // ==================== READ BOOKINGS ====================

    /**
     * Get booking by ID
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        try {
            BookingResponseDTO booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Get all bookings (admin only)
     * GET /api/bookings
     */
    @GetMapping
    public ResponseEntity<?> getAllBookings() {
        try {
            List<BookingResponseDTO> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Get user's bookings
     * GET /api/bookings/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBookings(@PathVariable Long userId) {
        try {
            List<BookingResponseDTO> bookings = bookingService.getBookingsByUserId(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Get bookings by status
     * GET /api/bookings/status/{status}
     * 
     * @param status PENDING, APPROVED, REJECTED, CANCELLED
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getBookingsByStatus(@PathVariable String status) {
        try {
            List<BookingResponseDTO> bookings = bookingService.getBookingsByStatus(status);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Get bookings for a resource
     * GET /api/bookings/resource/{resourceId}
     */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<?> getResourceBookings(@PathVariable Long resourceId) {
        try {
            List<BookingResponseDTO> bookings = bookingService.getBookingsByResourceId(resourceId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // ==================== UPDATE BOOKING ====================

    /**
     * Update an existing booking (only if PENDING)
     * PUT /api/bookings/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody UpdateBookingDTO dto) {
        try {
            BookingResponseDTO updatedBooking = bookingService.updateBooking(id, dto);
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // ==================== CANCEL BOOKING ====================

    /**
     * Cancel a booking (only if PENDING or APPROVED)
     * DELETE /api/bookings/{id}/cancel or PATCH /api/bookings/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            BookingResponseDTO cancelledBooking = bookingService.cancelBooking(id);
            return ResponseEntity.ok(cancelledBooking);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // ==================== ADMIN APPROVAL WORKFLOW ====================

    /**
     * Approve a booking (admin only, status must be PENDING)
     * PATCH /api/bookings/{id}/approve
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable Long id, @RequestBody BookingApprovalDTO dto) {
        try {
            // TODO: Add @PreAuthorize("hasRole('ADMIN')") in Phase 6
            BookingResponseDTO approvedBooking = bookingService.approveBooking(id, dto);
            return ResponseEntity.ok(approvedBooking);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Reject a booking (admin only, status must be PENDING)
     * PATCH /api/bookings/{id}/reject
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable Long id, @RequestBody BookingApprovalDTO dto) {
        try {
            // TODO: Add @PreAuthorize("hasRole('ADMIN')") in Phase 6
            BookingResponseDTO rejectedBooking = bookingService.rejectBooking(id, dto);
            return ResponseEntity.ok(rejectedBooking);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // ==================== DELETE BOOKING ====================

    /**
     * Delete a booking (only if PENDING or REJECTED)
     * DELETE /api/bookings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBooking(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Booking deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // ==================== ERROR HANDLING ====================

    /**
     * Centralized exception handling
     */
    private ResponseEntity<?> handleException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());

        if (e.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } else if (e.getMessage().contains("conflict")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } else if (e.getMessage().contains("Cannot")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } else if (e.getMessage().contains("not authorized")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}