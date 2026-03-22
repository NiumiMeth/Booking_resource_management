package backend.repository;

import backend.entity.Booking;
import backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
    SELECT b FROM Booking b
    WHERE b.resource.id = :resourceId
    AND b.status = 'APPROVED'
    AND (:start < b.endTime AND :end > b.startTime)
    """)
    List<Booking> findConflicts(
        Long resourceId,
        LocalDateTime start,
        LocalDateTime end
    );

    List<Booking> findByUser(User user);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatus(String status);

    List<Booking> findByResourceId(Long resourceId);

    @Query("""
    SELECT b FROM Booking b
    WHERE b.user.id = :userId
    ORDER BY b.startTime DESC
    """)
    List<Booking> findUserBookingsByCreationTime(@Param("userId") Long userId);

    @Query("""
    SELECT b FROM Booking b
    WHERE b.status = :status
    ORDER BY b.createdAt DESC
    """)
    List<Booking> findBookingsByStatusOrdered(@Param("status") String status);
}