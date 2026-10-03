package vn.iotstar.starshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        nullable = false,
        unique = true,
        length = 100,
        columnDefinition = "nvarchar(100)"
    )
    private String name;

    private String image;

    @Column(
        length = 500,
        columnDefinition = "nvarchar(500)"
    )
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}