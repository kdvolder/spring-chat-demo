package com.example.chatdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails alice = User.builder()
                .username("alice")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        UserDetails bob = User.builder()
                .username("bob")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(alice, bob);
    }

    @Bean
    @Order(1)  // Check assets first
    public SecurityFilterChain assetsChain(HttpSecurity http) throws Exception {
        return http
            .antMatcher("/assets/**")  // All static assets
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }

    @Bean 
    @Order(2)  // Check WebSocket second
    public SecurityFilterChain webSocketChain(HttpSecurity http) throws Exception {
        return http
            .antMatcher("/ws/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
            .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        return http.antMatcher("/api/**")
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .csrf(csrf -> csrf.disable())
            .build();
    }

    @Bean
    @Order(4)  // Handle everything else - AUTH REQUIRED FOR API AND MAIN PAGES
    public SecurityFilterChain mainAppChain(HttpSecurity http) throws Exception {
        return http
            // No .antMatcher() - handles all remaining requests
            .authorizeHttpRequests(auth -> auth
                .antMatchers("/", "/error").authenticated()  // Main app pages require auth
                .anyRequest().denyAll()  // Everything else is public
            )
            .formLogin(Customizer.withDefaults())
            .build();
    }

    // Let Spring Security handle /login automatically - no explicit chain needed

    // @Bean
    // @Order(4)  // Chat application pages
    // public SecurityFilterChain chatChain(HttpSecurity http) throws Exception {
    //     return http
    //         .antMatcher("/chat/**")
    //         .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
    //         .formLogin(form -> form.defaultSuccessUrl("/chat", true).permitAll())
    //         .build();
    // }

    // @Bean
    // @Order(Integer.MAX_VALUE)  // Lowest priority - DENY ALL others
    // public SecurityFilterChain denyAllChain(HttpSecurity http) throws Exception {
    //     return http
    //         .authorizeHttpRequests(auth -> auth.anyRequest().denyAll())
    //         .build();
    // }
} 