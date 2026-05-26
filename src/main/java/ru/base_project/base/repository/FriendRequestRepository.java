package ru.base_project.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.base_project.base.domain.FriendRequestStatus;
import ru.base_project.base.domain.entity.FriendRequestEntity;
import ru.base_project.base.domain.entity.MaboyEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequestEntity, UUID> {

    @EntityGraph(attributePaths = {"requester", "requester.profile", "receiver", "receiver.profile"})
    Optional<FriendRequestEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"requester", "requester.profile", "receiver", "receiver.profile"})
    List<FriendRequestEntity> findByReceiverAndStatusOrderByCreatedAtDesc(MaboyEntity receiver, FriendRequestStatus status);

    @EntityGraph(attributePaths = {"requester", "requester.profile", "receiver", "receiver.profile"})
    List<FriendRequestEntity> findByRequesterAndStatusOrderByCreatedAtDesc(MaboyEntity requester, FriendRequestStatus status);

    @Query("""
            select fr from FriendRequestEntity fr
            where fr.status = ru.base_project.base.domain.FriendRequestStatus.PENDING
              and ((fr.requester = :first and fr.receiver = :second)
                or (fr.requester = :second and fr.receiver = :first))
            """)
    Optional<FriendRequestEntity> findPendingBetween(@Param("first") MaboyEntity first,
                                                     @Param("second") MaboyEntity second);

    @Query("""
            select fr from FriendRequestEntity fr
            where fr.status = ru.base_project.base.domain.FriendRequestStatus.ACCEPTED
              and ((fr.requester = :first and fr.receiver = :second)
                or (fr.requester = :second and fr.receiver = :first))
            """)
    Optional<FriendRequestEntity> findAcceptedBetween(@Param("first") MaboyEntity first,
                                                      @Param("second") MaboyEntity second);

    @EntityGraph(attributePaths = {"requester", "requester.profile", "receiver", "receiver.profile"})
    @Query("""
            select fr from FriendRequestEntity fr
            where fr.status = ru.base_project.base.domain.FriendRequestStatus.ACCEPTED
              and (
                (fr.requester = :user and (:name is null
                    or lower(fr.receiver.username) like lower(concat('%', :name, '%'))
                    or lower(fr.receiver.profile.displayName) like lower(concat('%', :name, '%'))))
                or
                (fr.receiver = :user and (:name is null
                    or lower(fr.requester.username) like lower(concat('%', :name, '%'))
                    or lower(fr.requester.profile.displayName) like lower(concat('%', :name, '%'))))
              )
              and (:addedFrom is null or fr.answeredAt >= :addedFrom)
              and (:addedTo is null or fr.answeredAt <= :addedTo)
            order by fr.answeredAt desc
            """)
    List<FriendRequestEntity> findAcceptedFriends(@Param("user") MaboyEntity user,
                                                  @Param("name") String name,
                                                  @Param("addedFrom") LocalDateTime addedFrom,
                                                  @Param("addedTo") LocalDateTime addedTo);

    @EntityGraph(attributePaths = {"requester", "requester.profile", "receiver", "receiver.profile"})
    @Query("""
            select fr from FriendRequestEntity fr
            where fr.status = ru.base_project.base.domain.FriendRequestStatus.ACCEPTED
              and (fr.requester = :user or fr.receiver = :user)
            """)
    Page<FriendRequestEntity> findAcceptedFriendsPage(@Param("user") MaboyEntity user, Pageable pageable);
}
