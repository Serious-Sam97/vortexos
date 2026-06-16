package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.messenger.FileTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFileTransferRepository extends JpaRepository<FileTransfer, String> {
}
