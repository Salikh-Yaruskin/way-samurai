package ru.base_project.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.base_project.base.domain.entity.ShopProductEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopProductRepository extends JpaRepository<ShopProductEntity, UUID> {

    @EntityGraph(attributePaths = {"primaryCategory", "categories", "orders"})
    @Query("select p from ShopProductEntity p where p.id = :id")
    Optional<ShopProductEntity> findWithRelationsById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"primaryCategory", "categories"})
    @Query("""
            select distinct p from ShopProductEntity p
            left join p.primaryCategory pc
            left join p.categories c
            where (:search is null
                or lower(p.name) like lower(concat('%', :search, '%'))
                or lower(p.sku) like lower(concat('%', :search, '%')))
              and (:categoryId is null or pc.id = :categoryId or c.id = :categoryId)
              and (:active is null or p.active = :active)
            """)
    Page<ShopProductEntity> search(@Param("search") String search,
                                   @Param("categoryId") UUID categoryId,
                                   @Param("active") Boolean active,
                                   Pageable pageable);

    List<ShopProductEntity> findAllByOrderByNameAsc();
}
