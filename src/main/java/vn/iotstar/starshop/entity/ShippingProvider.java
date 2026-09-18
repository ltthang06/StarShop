package vn.iotstar.starshop.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shipping_providers")
public class ShippingProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseFee;

    private Integer estimatedDays;

    @Column(nullable = false)
    private boolean active = true;
}