package com.example.booking.config;

import com.example.booking.entity.*;
import com.example.booking.entity.Role;
import com.example.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedUsers() {
        return args -> {
            if (userRepository.count() > 0) return;
            userRepository.save(User.builder().username("admin")
                    .password(passwordEncoder.encode("admin123")).role(Role.ADMIN).build());
            userRepository.save(User.builder().username("user")
                    .password(passwordEncoder.encode("user123")).role(Role.USER).build());
        };
    }
}
