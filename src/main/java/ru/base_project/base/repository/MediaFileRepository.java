package ru.base_project.base.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.base_project.base.domain.entity.MediaFileEntity;

import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFileEntity, UUID> {
}
