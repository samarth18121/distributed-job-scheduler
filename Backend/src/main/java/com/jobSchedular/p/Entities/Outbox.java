package com.jobSchedular.p.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "Outbox")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String jobId;
    private boolean published;


    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }




}
