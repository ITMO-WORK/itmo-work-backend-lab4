package org.itmo.work.fileservice.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "application_registry")
public class ApplicationRegistry {

    @Id
    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}