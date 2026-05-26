package ru.base_project.base.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "social_profile")
public class SocialProfileEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "maboy_id", nullable = false, unique = true)
    private MaboyEntity user;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "interests", length = 1000)
    private String interests;

    @Column(name = "bio", length = 2000)
    private String bio;

    @Column(name = "avatar_filename")
    private String avatarFilename;

    @Column(name = "avatar_content_type", length = 100)
    private String avatarContentType;

    @Lob
    @Column(name = "avatar")
    private byte[] avatar;
}
