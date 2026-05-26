package ru.base_project.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.base_project.base.domain.OrderStatus;
import ru.base_project.base.domain.entity.ShopOrderEntity;

import java.util.Optional;
import java.util.UUID;

public interface ShopOrderRepository extends JpaRepository<ShopOrderEntity, UUID> {

    @EntityGraph(attributePaths = {"products", "products.primaryCategory", "products.categories"})
    @Query("select o from ShopOrderEntity o where o.id = :id")
    Optional<ShopOrderEntity> findWithProductsById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"products"})
    @Query("""
            select distinct o from ShopOrderEntity o
            left join o.products p
            where (:search is null
                or lower(o.customerName) like lower(concat('%', :search, '%'))
                or lower(o.customerEmail) like lower(concat('%', :search, '%')))
              and (:status is null or o.status = :status)
              and (:productId is null or p.id = :productId)
            """)
    Page<ShopOrderEntity> search(@Param("search") String search,
                                 @Param("status") OrderStatus status,
                                 @Param("productId") UUID productId,
                                 Pageable pageable);
}
