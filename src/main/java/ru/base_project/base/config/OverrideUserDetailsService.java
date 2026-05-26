package ru.base_project.base.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.base_project.base.service.MaboyService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverrideUserDetailsService implements UserDetailsService {

    private final MaboyService maboyService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = maboyService.getByUsername(username).orElseThrow(() -> {
            log.info("User with username = {} not found", username);
            return new UsernameNotFoundException("Пользователь с именем %s не найден!".formatted(username));
        });

        return new User(user.getUsername(), user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name())));
    }
}
