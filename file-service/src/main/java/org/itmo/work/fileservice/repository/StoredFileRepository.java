package org.itmo.work.fileservice.repository;

import org.itmo.work.fileservice.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
}
