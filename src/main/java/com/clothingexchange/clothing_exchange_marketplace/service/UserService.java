package com.clothingexchange.clothing_exchange_marketplace.service;

import com.clothingexchange.clothing_exchange_marketplace.model.User;
import com.clothingexchange.clothing_exchange_marketplace.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }


    // =====================================================
    // REGISTER USER
    // =====================================================

    public User registerUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException(
                    "Email already registered"
            );
        }

        // Never allow public registration
        // to create an ADMIN account.
        user.setRole("USER");

        // Hash password before saving
        String hashedPassword =
                passwordEncoder.encode(
                        user.getPassword()
                );

        user.setPassword(hashedPassword);

        User savedUser =
                userRepository.save(user);

        // Never return password
        savedUser.setPassword(null);

        return savedUser;
    }


    // =====================================================
    // FIND USER BY EMAIL
    // =====================================================

    public Optional<User> findByEmail(String email) {

        return userRepository.findByEmail(email);
    }


    // =====================================================
    // FIND USER BY ID
    // =====================================================

    public Optional<User> findById(Long id) {

        return userRepository.findById(id);
    }


    // =====================================================
    // VERIFY PASSWORD
    // =====================================================

    public boolean verifyPassword(
            String rawPassword,
            String hashedPassword) {

        return passwordEncoder.matches(
                rawPassword,
                hashedPassword
        );
    }


    // =====================================================
    // UPDATE USER PROFILE
    // =====================================================

    public User updateProfile(
            Long id,
            User updatedUser) {

        return userRepository.findById(id)
                .map(user -> {

                    user.setName(
                            updatedUser.getName()
                    );

                    user.setPhone(
                            updatedUser.getPhone()
                    );

                    user.setLocation(
                            updatedUser.getLocation()
                    );

                    // IMPORTANT:
                    // Role is NOT updated from profile.
                    // A normal user cannot make themselves ADMIN.

                    User savedUser =
                            userRepository.save(user);

                    // Never return password
                    savedUser.setPassword(null);

                    return savedUser;
                })
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));
    }


    // =====================================================
    // RESET PASSWORD
    // =====================================================

    public void resetPassword(
            String email,
            String newPassword) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        // Hash new password before saving
        String hashedPassword =
                passwordEncoder.encode(
                        newPassword
                );

        user.setPassword(hashedPassword);

        userRepository.save(user);
    }
}