package vn.iotstar.starshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="vnpay_payments") @Getter @Setter
public class VnpayPayment {
    public enum State {PENDING,PAID,FAILED,REVIEW_REQUIRED}
    @Id @Column(length=40) private String reference;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal amount;
    @Column(nullable=false) private LocalDateTime createdAt;
    @Column(nullable=false) private LocalDateTime expiresAt;
    private LocalDateTime resolvedAt;
    @Column(length=20) private String merchantCode;
    private LocalDateTime gatewayStartedAt;
    private LocalDateTime returnReceivedAt;
    @Column(length=2) private String returnResponseCode;
    @Column(length=2) private String returnTransactionStatus;
    private LocalDateTime lastCheckedAt;
    @org.hibernate.annotations.Nationalized @Column(length=500) private String reconciliationNote;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=25) private State state=State.PENDING;
    @Column(nullable=false,unique=true,length=80) private String gatewayTransactionNo;
    @Column(length=2) private String responseCode;
    @Column(length=2) private String transactionStatus;
    @Column(nullable=false,length=45) private String clientIp;
    @ManyToMany
    @JoinTable(name="vnpay_payment_orders",joinColumns=@JoinColumn(name="payment_reference"),
            inverseJoinColumns=@JoinColumn(name="order_id"),uniqueConstraints=@UniqueConstraint(columnNames="order_id"))
    private Set<Order> orders=new LinkedHashSet<>();
}
