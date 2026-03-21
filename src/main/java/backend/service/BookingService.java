package backend.service;

import backend.entity.Booking;
import backend.repository.BookingRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public boolean hasConflict(Long resourceId,
                               LocalDateTime start,
                               LocalDateTime end) {

        List<Booking> conflicts =
                bookingRepository.findConflicts(resourceId, start, end);

        return !conflicts.isEmpty();
    }

    public Booking saveBooking(Booking booking){
        return bookingRepository.save(booking);
    }
}