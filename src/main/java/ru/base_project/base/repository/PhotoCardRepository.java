package ru.base_project.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.base_project.base.domain.entity.PhotoCardEntity;

import java.util.UUID;

public interface PhotoCardRepository extends JpaRepository<PhotoCardEntity, UUID> {

    @Query("""
            select p from PhotoCardEntity p
            where :search is null
               or lower(p.title) like lower(concat('%', :search, '%'))
               or lower(p.description) like lower(concat('%', :search, '%'))
            """)
    Page<PhotoCardEntity> search(@Param("search") String search, Pageable pageable);
}
