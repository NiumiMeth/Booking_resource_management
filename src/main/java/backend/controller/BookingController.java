package backend.controller;

import backend.entity.Booking;
import backend.service.BookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/api/bookings")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking){

        boolean conflict = bookingService.hasConflict(
                booking.getResourceId(),
                booking.getStartTime(),
                booking.getEndTime()
        );

        if(conflict){
            return ResponseEntity.status(409)
                    .body("Booking conflict detected");
        }

        booking.setStatus("PENDING");

        return ResponseEntity.ok(
                bookingService.saveBooking(booking)
        );
    }
}