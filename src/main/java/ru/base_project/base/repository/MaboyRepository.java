package ru.base_project.base.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.base_project.base.domain.entity.MaboyEntity;

import java.util.Optional;
import java.util.UUID;

public interface MaboyRepository extends JpaRepository<MaboyEntity, UUID> {

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = "profile")
    Optional<MaboyEntity> findByUsername(String username);

    @EntityGraph(attributePaths = "profile")
    Optional<MaboyEntity> findWithProfileById(UUID id);

    @EntityGraph(attributePaths = "profile")
    @Query("""
            select u from MaboyEntity u
            left join u.profile p
            where (:city is null or lower(p.city) like lower(concat('%', :city, '%')))
              and (:interests is null or lower(p.interests) like lower(concat('%', :interests, '%')))
              and (:name is null or lower(u.username) like lower(concat('%', :name, '%'))
                   or lower(p.displayName) like lower(concat('%', :name, '%')))
            order by u.username asc
            """)
    Page<MaboyEntity> searchUsers(@Param("city") String city,
                                  @Param("interests") String interests,
                                  @Param("name") String name,
                                  Pageable pageable);
}
