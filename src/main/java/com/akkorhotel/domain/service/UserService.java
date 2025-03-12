package com.akkorhotel.domain.service;

import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.entity.UserRole;
import com.akkorhotel.domain.exception.UserNotFoundException;
import com.akkorhotel.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        // Hash du mot de passe
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
     * Récupère un utilisateur si le demandeur a la permission.
     * @param id l'ID de l'utilisateur cible
     * @param requester l'utilisateur faisant la requête
     * @return l'utilisateur trouvé
     * @throws SecurityException si le demandeur n'a pas la permission
     */
    public User getUserById(Long id, User requester) {
        User user = getUserById(id);
        if (hasAccess(user, requester)) {
            return user;
        }
        throw new SecurityException("You are not allowed to access this user");
    }

    /**
     * Récupère tous les utilisateurs (uniquement pour les admins).
     * @return la liste des utilisateurs
     */
    public List<User> getAllUsers(User requester) {
        if (requester.getRole() == UserRole.ADMIN) {
            return Collections.unmodifiableList(userRepository.findAll());
        }
        throw new SecurityException("Only admins can view all users.");
    }

    /**
     * Met à jour un utilisateur en vérifiant les permissions.
     * @param id l'ID de l'utilisateur à modifier
     * @param updatedUser les nouvelles données
     * @param requester l'utilisateur faisant la requête
     * @return l'utilisateur mis à jour
     * @throws SecurityException si le demandeur n'a pas la permission
     */
    @Transactional
    public User updateUser(Long id, User updatedUser, User requester) {
        checkUserPermission(id, requester, "update");

        User existingUser = getUserById(id);

        if (updatedUser.getEmail() != null) existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getPseudo() != null) existingUser.setPseudo(updatedUser.getPseudo());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    /**
     * Supprime un utilisateur en vérifiant les permissions.
     * @param id l'ID de l'utilisateur à supprimer
     * @param requester l'utilisateur faisant la requête
     * @throws SecurityException si le demandeur n'a pas la permission
     */
    public void deleteUser(Long id, User requester) {
        checkUserPermission(id, requester, "delete");
        userRepository.deleteById(id);
    }

    /**
     * Vérifie si un utilisateur a accès à un autre utilisateur.
     */
    private boolean hasAccess(User user, User requester) {
        return requester.getRole().equals(UserRole.ADMIN) || user.getId().equals(requester.getId());
    }

    /**
     * Vérifie si le demandeur a la permission d'effectuer une action.
     */
    private void checkUserPermission(Long id, User requester, String action) {
        if (!requester.getId().equals(id) && !requester.getRole().equals(UserRole.ADMIN)) {
            throw new SecurityException("You are not allowed to " + action + " this user");
        }
    }
}
