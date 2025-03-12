package com.akkorhotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "hotels")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String description;

    @ElementCollection
    private List<String> pictureList;

    @Version
    @Column(nullable = false)
    private Integer version;

    // Constructeur sans version pour les nouveaux objets
    public Hotel(Long id, String name, String location, String description, List<String> pictureList) {
        this(id, name, location, description, pictureList, 0);
    }
}
