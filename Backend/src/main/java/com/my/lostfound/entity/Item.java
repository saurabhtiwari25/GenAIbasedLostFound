package com.my.lostfound.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(nullable = false)
    private boolean found;

    @Column(nullable = false, length = 50)
    private String contactInfo;

    @Column(length = 200)
    private String foundLocation;

    @Column(length = 100)
    private String founderName;

    @Column(length = 50)
    private String founderContactInfo;

    @Column(length = 255)
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User reporter;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }
}