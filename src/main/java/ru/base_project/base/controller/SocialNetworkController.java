package ru.base_project.base.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.base_project.base.domain.api.SocialProfileForm;
import ru.base_project.base.service.SocialNetworkService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class SocialNetworkController {

    private final SocialNetworkService socialNetworkService;

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        var profile = socialNetworkService.getOrCreateProfile(principal.getName());
        model.addAttribute("profile", profile);
        return "social/profile/view";
    }

    @GetMapping("/profile/edit")
    public String editProfilePage(Principal principal, Model model) {
        var profile = socialNetworkService.getOrCreateProfile(principal.getName());
        model.addAttribute("profile", profile);
        model.addAttribute("form", new SocialProfileForm(
                profile.getDisplayName(),
                profile.getCity(),
                profile.getInterests(),
                profile.getBio(),
                null
        ));
        return "social/profile/form";
    }

    @PostMapping("/profile")
    public String updateProfile(Principal principal,
                                @Valid @ModelAttribute("form") SocialProfileForm form,
                                BindingResult bindingResult,
                                Model model) {
        var profile = socialNetworkService.getOrCreateProfile(principal.getName());
        if (form.avatar() != null
                && !form.avatar().isEmpty()
                && (form.avatar().getContentType() == null || !form.avatar().getContentType().startsWith("image/"))) {
            bindingResult.rejectValue("avatar", "avatar.image", "Загрузите файл изображения");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            return "social/profile/form";
        }
        socialNetworkService.updateProfile(principal.getName(), form);
        return "redirect:/profile";
    }

    @GetMapping("/profiles/{userId}/avatar")
    public ResponseEntity<byte[]> avatar(@PathVariable UUID userId) {
        var profile = socialNetworkService.getProfileByUserId(userId);
        if (profile.getAvatar() == null || profile.getAvatar().length == 0) {
            return ResponseEntity.notFound().build();
        }
        var contentType = profile.getAvatarContentType() == null
                ? "application/octet-stream"
                : profile.getAvatarContentType();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(profile.getAvatar());
    }

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String city,
                        @RequestParam(required = false) String interests,
                        @RequestParam(required = false) String name,
                        @PageableDefault(size = 10) Pageable pageable,
                        Model model) {
        model.addAttribute("users", socialNetworkService.searchUsers(city, interests, name, pageable));
        model.addAttribute("city", city);
        model.addAttribute("interests", interests);
        model.addAttribute("name", name);
        return "social/users/list";
    }

    @GetMapping("/users/{id}")
    public String userDetails(@PathVariable UUID id, Principal principal, Model model) {
        model.addAttribute("user", socialNetworkService.getUserWithProfile(id));
        if (principal != null) {
            model.addAttribute("friendshipStatus", socialNetworkService.friendshipStatus(principal.getName(), id));
        }
        return "social/users/details";
    }

    @PostMapping("/users/{id}/friend-requests")
    public String sendFriendRequest(@PathVariable UUID id,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            socialNetworkService.sendFriendRequest(principal.getName(), id);
            redirectAttributes.addFlashAttribute("message", "Заявка отправлена");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/users/" + id;
    }

    @GetMapping("/friends")
    public String friends(Principal principal,
                          @RequestParam(required = false) String name,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate addedFrom,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate addedTo,
                          Model model) {
        model.addAttribute("friends", socialNetworkService.friends(principal.getName(), name, addedFrom, addedTo));
        model.addAttribute("name", name);
        model.addAttribute("addedFrom", addedFrom);
        model.addAttribute("addedTo", addedTo);
        return "social/friends/list";
    }

    @GetMapping("/friend-requests")
    public String friendRequests(Principal principal, Model model) {
        model.addAttribute("incomingRequests", socialNetworkService.incomingRequests(principal.getName()));
        model.addAttribute("outgoingRequests", socialNetworkService.outgoingRequests(principal.getName()));
        return "social/friend-requests/list";
    }

    @PostMapping("/friend-requests/{id}/accept")
    public String acceptFriendRequest(@PathVariable UUID id,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            socialNetworkService.acceptFriendRequest(principal.getName(), id);
            redirectAttributes.addFlashAttribute("message", "Заявка принята");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/friend-requests";
    }

    @PostMapping("/friend-requests/{id}/reject")
    public String rejectFriendRequest(@PathVariable UUID id,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            socialNetworkService.rejectFriendRequest(principal.getName(), id);
            redirectAttributes.addFlashAttribute("message", "Заявка отклонена");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/friend-requests";
    }

    @PostMapping("/friend-requests/{id}/cancel")
    public String cancelFriendRequest(@PathVariable UUID id,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            socialNetworkService.cancelOutgoingRequest(principal.getName(), id);
            redirectAttributes.addFlashAttribute("message", "Заявка отменена");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/friend-requests";
    }
}
