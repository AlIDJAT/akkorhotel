package com.akkorhotel.interface_adapters.controller;

import com.akkorhotel.domain.entity.Hotel;
import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Seul l'utilisateur lui-même ou un admin peut accéder à un utilisateur
    @PreAuthorize("hasRole('ADMIN') or principal.domainUser.id == #id")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id,
                                        @AuthenticationPrincipal(expression = "domainUser") User requester) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ADMIN') or principal.domainUser.id == #id")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user,
                                        @AuthenticationPrincipal(expression = "domainUser") User requester) {
        try {
            User updated = userService.updateUser(id, user);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: You do not have permission to perform this action."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN') or principal.domainUser.id == #id")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @AuthenticationPrincipal(expression = "domainUser") User requester) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: You do not have permission to perform this action."));
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> listUsers(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String sortBy) {
        List<User> users = userService.listUsers(limit, sortBy);
        return ResponseEntity.ok(users);
    }
}
