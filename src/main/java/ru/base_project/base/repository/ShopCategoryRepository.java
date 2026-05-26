package ru.base_project.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.base_project.base.domain.entity.ShopCategoryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopCategoryRepository extends JpaRepository<ShopCategoryEntity, UUID> {

    @EntityGraph(attributePaths = {"products", "primaryProducts"})
    @Query("select c from ShopCategoryEntity c where c.id = :id")
    Optional<ShopCategoryEntity> findWithProductsById(@Param("id") UUID id);

    @Query("""
            select c from ShopCategoryEntity c
            where (:search is null
                or lower(c.name) like lower(concat('%', :search, '%'))
                or lower(c.slug) like lower(concat('%', :search, '%')))
              and (:active is null or c.active = :active)
            """)
    Page<ShopCategoryEntity> search(@Param("search") String search,
                                    @Param("active") Boolean active,
                                    Pageable pageable);

    List<ShopCategoryEntity> findAllByOrderByNameAsc();
}
