package ru.base_project.base.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.base_project.base.domain.FriendRequestStatus;
import ru.base_project.base.domain.api.FriendView;
import ru.base_project.base.domain.api.SocialProfileForm;
import ru.base_project.base.domain.entity.FriendRequestEntity;
import ru.base_project.base.domain.entity.MaboyEntity;
import ru.base_project.base.domain.entity.SocialProfileEntity;
import ru.base_project.base.repository.FriendRequestRepository;
import ru.base_project.base.repository.MaboyRepository;
import ru.base_project.base.repository.SocialProfileRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialNetworkService {

    private final MaboyRepository maboyRepository;
    private final SocialProfileRepository socialProfileRepository;
    private final FriendRequestRepository friendRequestRepository;

    @Transactional(readOnly = true)
    public MaboyEntity getCurrentUser(String username) {
        return maboyRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    @Transactional
    public SocialProfileEntity getOrCreateProfile(String username) {
        var user = getCurrentUser(username);
        return socialProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));
    }

    @Transactional(readOnly = true)
    public SocialProfileEntity getProfileByUserId(UUID userId) {
        return socialProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден"));
    }

    @Transactional
    public void updateProfile(String username, SocialProfileForm form) {
        var profile = getOrCreateProfile(username);
        profile.setDisplayName(form.displayName());
        profile.setCity(blankToNull(form.city()));
        profile.setInterests(blankToNull(form.interests()));
        profile.setBio(blankToNull(form.bio()));
        saveAvatar(profile, form.avatar());
        socialProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public Page<MaboyEntity> searchUsers(String city, String interests, String name, Pageable pageable) {
        return maboyRepository.searchUsers(blankToNull(city), blankToNull(interests), blankToNull(name), pageable);
    }

    @Transactional(readOnly = true)
    public MaboyEntity getUserWithProfile(UUID id) {
        return maboyRepository.findWithProfileById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    @Transactional
    public void sendFriendRequest(String requesterUsername, UUID receiverId) {
        var requester = getCurrentUser(requesterUsername);
        var receiver = getUserWithProfile(receiverId);

        if (requester.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Нельзя отправить заявку самому себе");
        }
        if (friendRequestRepository.findAcceptedBetween(requester, receiver).isPresent()) {
            throw new IllegalArgumentException("Этот пользователь уже в друзьях");
        }
        if (friendRequestRepository.findPendingBetween(requester, receiver).isPresent()) {
            throw new IllegalArgumentException("Заявка уже ожидает ответа");
        }

        var request = new FriendRequestEntity();
        request.setRequester(requester);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        friendRequestRepository.save(request);
    }

    @Transactional
    public void acceptFriendRequest(String username, UUID requestId) {
        var currentUser = getCurrentUser(username);
        var request = getRequestForReceiver(currentUser, requestId);
        request.setStatus(FriendRequestStatus.ACCEPTED);
        request.setAnsweredAt(LocalDateTime.now());
        friendRequestRepository.save(request);
    }

    @Transactional
    public void rejectFriendRequest(String username, UUID requestId) {
        var currentUser = getCurrentUser(username);
        var request = getRequestForReceiver(currentUser, requestId);
        request.setStatus(FriendRequestStatus.REJECTED);
        request.setAnsweredAt(LocalDateTime.now());
        friendRequestRepository.save(request);
    }

    @Transactional
    public void cancelOutgoingRequest(String username, UUID requestId) {
        var currentUser = getCurrentUser(username);
        var request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        if (!request.getRequester().getId().equals(currentUser.getId()) || request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Эту заявку нельзя отменить");
        }
        friendRequestRepository.delete(request);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestEntity> incomingRequests(String username) {
        var currentUser = getCurrentUser(username);
        return friendRequestRepository.findByReceiverAndStatusOrderByCreatedAtDesc(currentUser, FriendRequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestEntity> outgoingRequests(String username) {
        var currentUser = getCurrentUser(username);
        return friendRequestRepository.findByRequesterAndStatusOrderByCreatedAtDesc(currentUser, FriendRequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<FriendView> friends(String username, String name, LocalDate addedFrom, LocalDate addedTo) {
        var currentUser = getCurrentUser(username);
        var from = addedFrom == null ? null : addedFrom.atStartOfDay();
        var to = addedTo == null ? null : addedTo.atTime(LocalTime.MAX);
        return friendRequestRepository.findAcceptedFriends(currentUser, blankToNull(name), from, to).stream()
                .map(request -> toFriendView(currentUser, request))
                .toList();
    }

    @Transactional(readOnly = true)
    public String friendshipStatus(String username, UUID otherUserId) {
        var currentUser = getCurrentUser(username);
        var other = getUserWithProfile(otherUserId);
        if (currentUser.getId().equals(other.getId())) {
            return "SELF";
        }
        if (friendRequestRepository.findAcceptedBetween(currentUser, other).isPresent()) {
            return "FRIEND";
        }
        return friendRequestRepository.findPendingBetween(currentUser, other)
                .map(request -> request.getRequester().getId().equals(currentUser.getId()) ? "OUTGOING" : "INCOMING")
                .orElse("NONE");
    }

    private FriendRequestEntity getRequestForReceiver(MaboyEntity receiver, UUID requestId) {
        var request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        if (!request.getReceiver().getId().equals(receiver.getId()) || request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Эту заявку нельзя обработать");
        }
        return request;
    }

    private SocialProfileEntity createDefaultProfile(MaboyEntity user) {
        var profile = new SocialProfileEntity();
        profile.setUser(user);
        profile.setDisplayName(user.getUsername());
        return socialProfileRepository.save(profile);
    }

    private FriendView toFriendView(MaboyEntity currentUser, FriendRequestEntity request) {
        var friend = request.getRequester().getId().equals(currentUser.getId())
                ? request.getReceiver()
                : request.getRequester();
        var profile = friend.getProfile();
        return new FriendView(
                friend.getId(),
                friend.getUsername(),
                profile == null ? friend.getUsername() : profile.getDisplayName(),
                profile == null ? null : profile.getCity(),
                profile == null ? null : profile.getInterests(),
                request.getAnsweredAt()
        );
    }

    private void saveAvatar(SocialProfileEntity profile, MultipartFile avatar) {
        if (avatar == null || avatar.isEmpty()) {
            return;
        }
        try {
            profile.setAvatar(avatar.getBytes());
            profile.setAvatarFilename(avatar.getOriginalFilename());
            profile.setAvatarContentType(avatar.getContentType());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Не удалось сохранить фото профиля", exception);
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
