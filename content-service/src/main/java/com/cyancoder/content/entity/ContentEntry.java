package com.cyancoder.content.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "content_entries")
@Data
public class ContentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false, unique = true)
    private String slug;

    private String title;

    @Column(length = 4000)
    private String summary;

    @Column(length = 20000)
    private String body;

    private String templateKey;

    private String publicationStatus;

    private String seoTitle;

    private String seoDescription;

    private String author;

    @Column(length = 4000)
    private String tags;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
