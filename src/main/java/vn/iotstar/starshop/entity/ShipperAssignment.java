package vn.iotstar.starshop.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shipper_assignments")
public class ShipperAssignment {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    private LocalDateTime pickedUpAt;

    private LocalDateTime deliveredAt;

    @Column(length = 500)
    private String note;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", nullable = false)
    private User shipper;

    @Transient
    public String getAssignedAtFormatted() {
        return formatDateTime(assignedAt);
    }

    @Transient
    public String getPickedUpAtFormatted() {
        return formatDateTime(pickedUpAt);
    }

    @Transient
    public String getDeliveredAtFormatted() {
        return formatDateTime(deliveredAt);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.format(DISPLAY_FORMATTER);
    }
}