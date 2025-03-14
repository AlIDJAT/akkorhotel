package com.akkorhotel.domain.service;

import com.akkorhotel.domain.entity.Hotel;
import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.entity.UserRole;
import com.akkorhotel.domain.exception.UserNotFoundException;
import com.akkorhotel.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crée un nouvel utilisateur.
     * @param user les informations de l'utilisateur
     * @return l'utilisateur créé
     * @throws IllegalArgumentException si l'email est déjà utilisé
     */
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Récupère un utilisateur par son ID.
     * @param id l'ID de l'utilisateur
     * @return l'utilisateur trouvé
     * @throws UserNotFoundException si l'utilisateur n'existe pas
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found", id);
                    return new UserNotFoundException("User not found");
                });
    }

    /**
     * Récupère un utilisateur si le demandeur a la permission (géré via @PreAuthorize dans le controller).
     * @param id l'ID de l'utilisateur cible
     * @param requester l'utilisateur faisant la requête
     * @return l'utilisateur trouvé
     * @throws SecurityException si le demandeur n'a pas la permission
     */
    public User getUserById(Long id, User requester) {
        User user = getUserById(id);
        return user;
    }

    /**
     * Récupère tous les utilisateurs (uniquement pour les admins).
     * @return la liste des utilisateurs
     */
    public List<User> getAllUsers() {
        return Collections.unmodifiableList(userRepository.findAll());
    }

    /**
     * Met à jour un utilisateur (géré via @PreAuthorize dans le controller).
     * @param id l'ID de l'utilisateur à modifier
     * @param updatedUser les nouvelles données
     * @return l'utilisateur mis à jour
     */
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getPseudo() != null && !updatedUser.getPseudo().equals(existingUser.getPseudo())) {
            existingUser.setPseudo(updatedUser.getPseudo());
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    /**
     * Supprime un utilisateur (géré via @PreAuthorize dans le controller).
     * @param id l'ID de l'utilisateur à supprimer
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    public List<User> listUsers(int limit, String sortBy) {
        if (sortBy != null && !sortBy.isEmpty()) {
            return userRepository.findAll(PageRequest.of(0, limit, Sort.by(sortBy))).getContent();
        } else {
            return userRepository.findAll(PageRequest.of(0, limit)).getContent();
        }
    }
}
