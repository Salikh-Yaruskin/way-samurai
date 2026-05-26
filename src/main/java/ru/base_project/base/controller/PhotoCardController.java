package ru.base_project.base.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.base_project.base.domain.api.PhotoCardForm;
import ru.base_project.base.repository.PhotoCardRepository;
import ru.base_project.base.service.PhotoCardService;

import java.util.UUID;

@Controller
@RequestMapping("/photo-cards")
@RequiredArgsConstructor
public class PhotoCardController {

    private final PhotoCardRepository photoCardRepository;
    private final PhotoCardService photoCardService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        var pageable = PageRequest.of(Math.max(page, 0), 9, Sort.by("createdAt").descending());
        var photoCards = photoCardRepository.search(blankToNull(search), pageable);

        model.addAttribute("photoCards", photoCards);
        model.addAttribute("search", search);
        return "photo-cards/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new PhotoCardForm());
        model.addAttribute("mode", "create");
        return "photo-cards/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") PhotoCardForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "photo-cards/form";
        }

        try {
            var photoCard = photoCardService.create(form);
            redirectAttributes.addFlashAttribute("message", "Фотокарточка создана");
            return "redirect:/photo-cards/" + photoCard.getId();
        } catch (IllegalArgumentException e) {
            bindingResult.reject("photo", e.getMessage());
            model.addAttribute("mode", "create");
            return "photo-cards/form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id, Model model) {
        model.addAttribute("photoCard", photoCardService.get(id));
        return "photo-cards/details";
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable UUID id) {
        var photoCard = photoCardService.get(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photoCard.getContentType()))
                .cacheControl(CacheControl.noCache())
                .body(photoCard.getPhoto());
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        var photoCard = photoCardService.get(id);
        model.addAttribute("photoCard", photoCard);
        model.addAttribute("form", photoCardService.toForm(photoCard));
        model.addAttribute("mode", "edit");
        return "photo-cards/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute("form") PhotoCardForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("photoCard", photoCardService.get(id));
            model.addAttribute("mode", "edit");
            return "photo-cards/form";
        }

        try {
            photoCardService.update(id, form);
            redirectAttributes.addFlashAttribute("message", "Фотокарточка обновлена");
            return "redirect:/photo-cards/" + id;
        } catch (IllegalArgumentException e) {
            bindingResult.reject("photo", e.getMessage());
            model.addAttribute("photoCard", photoCardService.get(id));
            model.addAttribute("mode", "edit");
            return "photo-cards/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        photoCardService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Фотокарточка удалена");
        return "redirect:/photo-cards";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
