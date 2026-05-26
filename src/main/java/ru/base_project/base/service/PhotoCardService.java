package ru.base_project.base.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.base_project.base.domain.api.PhotoCardForm;
import ru.base_project.base.domain.entity.PhotoCardEntity;
import ru.base_project.base.repository.PhotoCardRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoCardService {

    private final PhotoCardRepository photoCardRepository;

    @Transactional
    public PhotoCardEntity create(PhotoCardForm form) {
        validatePhotoRequired(form.getPhoto());

        var photoCard = new PhotoCardEntity();
        applyTextFields(photoCard, form);
        applyPhoto(photoCard, form.getPhoto());
        return photoCardRepository.save(photoCard);
    }

    @Transactional
    public PhotoCardEntity update(UUID id, PhotoCardForm form) {
        var photoCard = get(id);
        applyTextFields(photoCard, form);

        if (form.getPhoto() != null && !form.getPhoto().isEmpty()) {
            validatePhotoRequired(form.getPhoto());
            applyPhoto(photoCard, form.getPhoto());
        }

        return photoCardRepository.save(photoCard);
    }

    @Transactional
    public void delete(UUID id) {
        photoCardRepository.delete(get(id));
    }

    @Transactional(readOnly = true)
    public PhotoCardEntity get(UUID id) {
        return photoCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Фотокарточка не найдена"));
    }

    public PhotoCardForm toForm(PhotoCardEntity photoCard) {
        var form = new PhotoCardForm();
        form.setTitle(photoCard.getTitle());
        form.setDescription(photoCard.getDescription());
        return form;
    }

    private void applyTextFields(PhotoCardEntity photoCard, PhotoCardForm form) {
        photoCard.setTitle(form.getTitle());
        photoCard.setDescription(form.getDescription());
    }

    private void applyPhoto(PhotoCardEntity photoCard, MultipartFile file) {
        try {
            photoCard.setOriginalFilename(StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "photo" : file.getOriginalFilename()
            ));
            photoCard.setContentType(file.getContentType());
            photoCard.setPhoto(file.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать файл фотографии", e);
        }
    }

    private void validatePhotoRequired(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Загрузите фотографию");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Можно загрузить только изображение");
        }
    }
}
