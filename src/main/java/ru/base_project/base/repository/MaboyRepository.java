package ru.base_project.base.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.base_project.base.domain.entity.MaboyEntity;

import java.util.Optional;
import java.util.UUID;

public interface MaboyRepository extends JpaRepository<MaboyEntity, UUID> {

    boolean existsByUsername(String username);

    Optional<MaboyEntity> findByUsername(String username);
}
