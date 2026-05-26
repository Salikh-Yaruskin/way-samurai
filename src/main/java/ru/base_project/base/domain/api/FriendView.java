package ru.base_project.base.domain.api;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendView(
        UUID userId,
        String username,
        String displayName,
        String city,
        String interests,
        LocalDateTime addedAt
) {
}
