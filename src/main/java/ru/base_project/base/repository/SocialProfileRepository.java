package ru.base_project.base.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.base_project.base.domain.entity.MaboyEntity;
import ru.base_project.base.domain.entity.SocialProfileEntity;

import java.util.Optional;
import java.util.UUID;

public interface SocialProfileRepository extends JpaRepository<SocialProfileEntity, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<SocialProfileEntity> findByUser(MaboyEntity user);

    @EntityGraph(attributePaths = "user")
    Optional<SocialProfileEntity> findByUserId(UUID userId);
}
