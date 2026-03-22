package backend.booking.dto;

/**
 * DTO for admin approval/rejection decisions.
 * Admins provide their decision and optional feedback reason.
 */
public class BookingApprovalDTO {

    private String status; // "APPROVED" or "REJECTED"
    private String approvalReason; // Admin feedback

    // Constructors

    public BookingApprovalDTO() {
    }

    public BookingApprovalDTO(String status, String approvalReason) {
        this.status = status;
        this.approvalReason = approvalReason;
    }

    // Getters and Setters

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApprovalReason() {
        return approvalReason;
    }

    public void setApprovalReason(String approvalReason) {
        this.approvalReason = approvalReason;
    }
}
