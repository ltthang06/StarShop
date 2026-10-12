package vn.iotstar.starshop.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
@Table(name = "product_moderations")
public class ProductModeration {
    @Id private Long productId;
    @Column(nullable=false) private boolean blocked;
    @Column(length=500) private String reason;
    @Column(nullable=false, length=150) private String moderatorEmail;
    @Column(nullable=false) private LocalDateTime updatedAt;
}
