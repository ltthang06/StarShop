package vn.iotstar.starshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @org.hibernate.annotations.Nationalized
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 255)
    @org.hibernate.annotations.Nationalized
    private String addressLine;

    @Column(length = 100)
    @org.hibernate.annotations.Nationalized
    private String ward;

    @Column(length = 100)
    @org.hibernate.annotations.Nationalized
    private String district;

    @Column(length = 100)
    @org.hibernate.annotations.Nationalized
    private String province;

    @Column(nullable = false)
    private boolean defaultAddress = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}