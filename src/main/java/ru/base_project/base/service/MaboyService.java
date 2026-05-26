package ru.base_project.base.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.base_project.base.domain.api.MaboyRegisterRequest;
import ru.base_project.base.domain.entity.MaboyEntity;
import ru.base_project.base.domain.entity.Role;
import ru.base_project.base.repository.MaboyRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaboyService {

    private final PasswordEncoder passwordEncoder;
    private final MaboyRepository maboyRepository;

    @Transactional
    public void register(MaboyRegisterRequest request) {
        var maboy = new MaboyEntity();
        maboy.setUsername(request.username());
        maboy.setPassword(passwordEncoder.encode(request.password()));
        maboy.setRole(Role.USER);

        maboyRepository.save(maboy);
    }

    @Transactional
    public boolean existsByUsername(String username) {
        return maboyRepository.existsByUsername(username);
    }

    public Optional<MaboyEntity> getByUsername(String username) {
        return maboyRepository.findByUsername(username);
    }
}
