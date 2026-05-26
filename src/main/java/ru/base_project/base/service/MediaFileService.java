package ru.base_project.base.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.base_project.base.domain.MediaType;
import ru.base_project.base.domain.entity.MediaFileEntity;
import ru.base_project.base.repository.MediaFileRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaFileService {

    private final MediaFileRepository mediaFileRepository;

    private final Path uploadRoot = Path.of("uploads").toAbsolutePath().normalize();

    @Transactional
    public MediaFileEntity save(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }

        try {
            Files.createDirectories(uploadRoot);

            String originalFilename = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()
            );
            String storageKey = UUID.randomUUID() + getExtension(originalFilename);
            Path target = uploadRoot.resolve(storageKey).normalize();

            if (!target.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Некорректное имя файла");
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            var entity = new MediaFileEntity();
            entity.setOriginalFilename(originalFilename);
            entity.setStorageKey(storageKey);
            entity.setContentType(resolveContentType(file));
            entity.setSizeBytes(file.getSize());
            entity.setType(resolveType(entity.getContentType()));

            return mediaFileRepository.save(entity);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить файл", e);
        }
    }

    @Transactional(readOnly = true)
    public List<MediaFileEntity> findAll() {
        return mediaFileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MediaFileEntity get(UUID id) {
        return mediaFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Файл не найден"));
    }

    @Transactional
    public void delete(UUID id) {
        MediaFileEntity mediaFile = get(id);

        try {
            Path file = uploadRoot.resolve(mediaFile.getStorageKey()).normalize();
            if (!file.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Некорректный путь к файлу");
            }
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось удалить файл", e);
        }

        mediaFileRepository.delete(mediaFile);
    }

    public Resource loadAsResource(MediaFileEntity mediaFile) {
        try {
            Path file = uploadRoot.resolve(mediaFile.getStorageKey()).normalize();

            if (!file.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Некорректный путь к файлу");
            }

            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Файл не найден на диске");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Некорректный путь к файлу", e);
        }
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType;
    }

    private MediaType resolveType(String contentType) {
        if (contentType.startsWith("image/")) {
            return MediaType.PHOTO;
        }
        if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        }
        return MediaType.DOCUMENT;
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}
