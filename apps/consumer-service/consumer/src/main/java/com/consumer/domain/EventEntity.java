package com.consumer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "events", uniqueConstraints = @UniqueConstraint(columnNames = "messageId"))
public class EventEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @Column(nullable = false, unique = true)
    @Getter @Setter
    private String messageId;

    @Column(nullable = false)
    @Getter @Setter
    private String userId;

    @Column(nullable = false)
    @Getter @Setter
    private String action;

    @Column(nullable = false)
    @Getter @Setter
    private Instant sentAt;      // from producer

    @Column(nullable = false)
    @Getter @Setter
    private Instant receivedAt;  // when consumer processed


}
