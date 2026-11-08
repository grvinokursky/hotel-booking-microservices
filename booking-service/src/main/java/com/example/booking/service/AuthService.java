package com.example.booking.service;

import com.example.booking.dto.*;
import com.example.booking.entity.*;
import com.example.booking.exception.*;
import com.example.booking.mapper.UserMapper;
import com.example.booking.repository.UserRepository;
import com.example.booking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Username already taken");
        }
        User user = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .role(Role.USER).build();
        userRepository.save(user);
        return new AuthResponse(jwtService.generate(user), user.getUsername(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new NotFoundException("Invalid username or password"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new NotFoundException("Invalid username or password");
        }
        return new AuthResponse(jwtService.generate(user), user.getUsername(), user.getRole().name());
    }
}
