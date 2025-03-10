package com.akkorhotel.domain.service;

import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.exception.UserNotFoundException;
import com.akkorhotel.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found", id);
                    return new UserNotFoundException("User not found");
                });
    }

    public List<User> getAllUsers() {
        return Collections.unmodifiableList(userRepository.findAll());
    }

    public User updateUser(Long id, User newUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        logger.info("Updating user with ID: {}", id);

        existingUser.setPseudo(newUser.getPseudo());

        if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
            existingUser.setPassword(newUser.getPassword());
        }

        return userRepository.save(existingUser);
    }



}