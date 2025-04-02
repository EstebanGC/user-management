package com.dev.user_manage.service;

import com.dev.user_manage.dto.AuthUser;
import com.dev.user_manage.dto.RegisterUser;
import com.dev.user_manage.dto.UpdateUser;
import com.dev.user_manage.entity.Role;
import com.dev.user_manage.entity.User;
import com.dev.user_manage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public User register(RegisterUser registerUser) {
        User user = mapToUser(registerUser);
        return userRepository.save(user);
    }

    public String auth(AuthUser authUser) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authUser.getUsername(), authUser.getPassword())
        );
        return jwtService.generateJwtToken(authUser.getUsername());
    }

    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateUser(String id, UpdateUser updateUser) {
        User user = getUserById(id);
        
        if (updateUser.getFirstname() != null) {
            user.setFirstname(updateUser.getFirstname());
        }
        if (updateUser.getLastname() != null) {
            user.setLastname(updateUser.getLastname());
        }
        if (updateUser.getUsername() != null) {
            user.setUsername(updateUser.getUsername());
        }
        
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void changePassword(String id, String oldPassword, String newPassword) {
        User user = getUserById(id);
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean isUserOwner(String userId, String username) {
        User user = getUserById(userId);
        return user.getUsername().equals(username);
    }

    private User mapToUser(RegisterUser registerUser) {
        return User.builder()
                .username(registerUser.getUsername())
                .firstname(registerUser.getFirstname())
                .lastname(registerUser.getLastname())
                .password(passwordEncoder.encode(registerUser.getPassword()))
                .roles(List.of(Role.USER.name()))
                .build();
    }
}
