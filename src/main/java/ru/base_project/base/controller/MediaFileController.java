package ru.base_project.base.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.base_project.base.service.MediaFileService;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MediaFileController {

    private final MediaFileService mediaFileService;

    @GetMapping("/media")
    public String mediaPage(Model model) {
        model.addAttribute("files", mediaFileService.findAll());
        return "media/index";
    }

    @PostMapping("/media")
    public String upload(@RequestParam("file") MultipartFile file,
                         RedirectAttributes redirectAttributes) {
        var mediaFile = mediaFileService.save(file);
        redirectAttributes.addFlashAttribute("uploadedFileId", mediaFile.getId());
        return "redirect:/media";
    }

    @PostMapping("/media/{id}/delete")
    public String delete(@PathVariable UUID id,
                         RedirectAttributes redirectAttributes) {
        mediaFileService.delete(id);
        redirectAttributes.addFlashAttribute("deleted", true);
        return "redirect:/media";
    }

    @GetMapping("/media/{id}")
    public ResponseEntity<Resource> view(@PathVariable UUID id) {
        var mediaFile = mediaFileService.get(id);
        Resource resource = mediaFileService.loadAsResource(mediaFile);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaFile.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + mediaFile.getOriginalFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/media/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        var mediaFile = mediaFileService.get(id);
        Resource resource = mediaFileService.loadAsResource(mediaFile);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaFile.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + mediaFile.getOriginalFilename() + "\"")
                .body(resource);
    }
}
