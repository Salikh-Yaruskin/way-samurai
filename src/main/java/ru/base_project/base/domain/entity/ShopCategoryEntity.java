package ru.base_project.base.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "shop_category")
@NoArgsConstructor
@AllArgsConstructor
public class ShopCategoryEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "primaryCategory")
    private Set<ShopProductEntity> primaryProducts = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "categories")
    private Set<ShopProductEntity> products = new LinkedHashSet<>();
}
