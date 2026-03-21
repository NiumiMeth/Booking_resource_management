package backend.repository;

import backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
    SELECT b FROM Booking b
    WHERE b.resourceId = :resourceId
    AND b.status = 'APPROVED'
    AND (:start < b.endTime AND :end > b.startTime)
    """)
    List<Booking> findConflicts(
        Long resourceId,
        LocalDateTime start,
        LocalDateTime end
    );
}