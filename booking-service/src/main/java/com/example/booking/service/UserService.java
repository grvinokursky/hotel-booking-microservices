package com.example.booking.service;

import com.example.booking.dto.*;
import com.example.booking.entity.User;
import com.example.booking.exception.*;
import com.example.booking.mapper.UserMapper;
import com.example.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    @Transactional
    public UserDto create(String username, String password, String role) {
        if (userRepository.existsByUsername(username)) throw new ConflictException("Username taken");
        User user = User.builder().username(username).password(passwordEncoder.encode(password))
                .role(role == null ? com.example.booking.entity.Role.USER : com.example.booking.entity.Role.valueOf(role)).build();
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto update(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User " + id + " not found"));
        user.setUsername(req.username());
        if (req.password() != null && !req.password().isBlank()) user.setPassword(passwordEncoder.encode(req.password()));
        if (req.role() != null) user.setRole(com.example.booking.entity.Role.valueOf(req.role()));
        return userMapper.toDto(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) throw new NotFoundException("User " + id + " not found");
        userRepository.deleteById(id);
    }
}
