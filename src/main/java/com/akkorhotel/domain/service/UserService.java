package com.akkorhotel.domain.service;

import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.entity.UserRole;
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

    /**
     * Creates a new user.
     * @param user the user to create
     * @return the created user
     * @throws IllegalArgumentException if the email is already in use
     */
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     * @param id the ID of the user
     * @return the user with the given ID
     * @throws UserNotFoundException if the user is not found
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found", id);
                    return new UserNotFoundException("User not found");
                });
    }

    /**
     * Retrieves all users.
     * @return an unmodifiable list of all users
     */
    public List<User> getAllUsers() {
        return Collections.unmodifiableList(userRepository.findAll());
    }

    /**
     * Updates an existing user.
     * @param id the ID of the user to update
     * @param newUser the new user data
     * @return the updated user
     * @throws IllegalArgumentException if the user is not found
     */
    public User updateUser(Long id, User newUser) {
        User existingUser = getUserById(id);

        logger.info("Updating user with ID: {}", id);

        updateUserData(existingUser, newUser);

        return userRepository.save(existingUser);
    }

    /**
     * Retrieves a user by their ID if the requester has permission.
     * @param id the ID of the user
     * @param requester the user making the request
     * @return the user with the given ID
     * @throws SecurityException if the requester does not have permission
     */
    public User getUserById(Long id, User requester) {
        return userRepository.findById(id)
                .filter(user -> hasAccess(user, requester))
                .orElseThrow(() -> new SecurityException("You are not allowed to access this user"));
    }

    private boolean hasAccess(User user, User requester) {
        return requester.getRole().equals(UserRole.ADMIN) || user.getId().equals(requester.getId());
    }

    private void updateUserData(User existingUser, User newUser) {
        existingUser.setPseudo(newUser.getPseudo());
        if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
            existingUser.setPassword(newUser.getPassword());
        }
    }

    public void updateUser(Long id, User updatedUser, User requester) {
        checkUserPermission(id, requester, "update");
        User existingUser = getUserById(id);
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPseudo(updatedUser.getPseudo());
        existingUser.setPassword(updatedUser.getPassword());
        userRepository.save(existingUser);
    }

    public void deleteUser(Long id, User requester) {
        checkUserPermission(id, requester, "delete");
        userRepository.deleteById(id);
    }


    private void checkUserPermission(Long id, User requester, String action) {
        if (!requester.getId().equals(id) && !requester.getRole().equals(UserRole.ADMIN)) {
            throw new SecurityException("You are not allowed to " + action + " this user");
        }
    }


}