package com.clothingexchange.clothing_exchange_marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // ==============================
                        // PUBLIC PAGES
                        // ==============================

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/dashboard.html",
                                "/profile.html",
                                "/listings.html",
                                "/item-detail.html",
                                "/add-item.html",
                                "/edit-item.html",
                                "/swap-request.html",
                                "/swap-requests.html",
                                "/chat.html",
                                "/swap-calculator.html",
                                "/location-matching.html",
                                "/admin.html"
                        ).permitAll()

                        // ==============================
                        // STATIC FILES
                        // ==============================

                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ==============================
                        // AUTH / OTP
                        // ==============================

                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/auth/reset-password",
                                "/api/otp/**"
                        ).permitAll()

                        // ==============================
                        // WEBSOCKET
                        // ==============================

                        .requestMatchers("/ws/**")
                        .permitAll()

                        // ==============================
                        // ADMIN
                        // ==============================

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // ==============================
                        // AUTHENTICATED APIs
                        // ==============================

                        .requestMatchers(
                                "/api/auth/me",
                                "/api/auth/profile/**",
                                "/api/items/**",
                                "/api/swaps/**",
                                "/api/chat/**",
                                "/api/disputes/**"
                        )
                        .authenticated()

                        // ==============================
                        // EVERYTHING ELSE
                        // ==============================

                        .anyRequest()
                        .authenticated()
                )

                // We are using custom JSON login
                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}