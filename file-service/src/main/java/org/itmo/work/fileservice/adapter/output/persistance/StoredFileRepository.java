package org.itmo.work.fileservice.adapter.output.persistance;

import org.itmo.work.fileservice.domain.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {

    Boolean existsByEntityId(UUID entityId);

    Optional<StoredFile> findByEntityId(UUID entityId);
}
