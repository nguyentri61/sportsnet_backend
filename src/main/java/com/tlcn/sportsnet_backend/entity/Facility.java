package com.tlcn.sportsnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        name = "facilities",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "address"})
        }
)
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String name;

    String address;

    String district;
    String city;

    @Column(columnDefinition = "TEXT")
    String location;

    Double latitude;
    Double longitude;

    String image;

    @PrePersist
    @PreUpdate
    public void generateLocation() {
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.isBlank()) sb.append(name);
        if (address != null && !address.isBlank()) sb.append(", ").append(address);
        if (district != null && !district.isBlank()) sb.append(", ").append(district);
        if (city != null && !city.isBlank()) sb.append(", ").append(city);
        this.location = sb.toString();
    }
}
