package com.example.chatdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.saml2.provider.service.metadata.Saml2MetadataResolver;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails alice = User.builder()
                .username("alice")
                .password(passwordEncoder.encode("password"))
                .roles("USER")
                .build();

        UserDetails bob = User.builder()
                .username("bob")
                .password(passwordEncoder.encode("password"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(alice, bob);
    }

    @Bean
    public Saml2MetadataResolver saml2MetadataResolver() {
        return new OpenSamlMetadataResolver();
    }

    // EXACT PATTERN FROM SPRING SECURITY 5.7 DOCS
    @Bean
    public Saml2MetadataFilter saml2MetadataFilter(
            RelyingPartyRegistrationRepository repository,
            Saml2MetadataResolver metadataResolver) {

        var relyingPartyRegistrationResolver =
                new DefaultRelyingPartyRegistrationResolver(repository);
        
        // Cast to RelyingPartyRegistrationResolver to resolve constructor ambiguity
        return new Saml2MetadataFilter(
                (RelyingPartyRegistrationResolver) relyingPartyRegistrationResolver,
                metadataResolver);
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
            .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.ALWAYS))
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
    @Order(5) // Handle everything else - AUTH REQUIRED FOR API AND MAIN PAGES
    public SecurityFilterChain mainAppChain(
            HttpSecurity http, 
            RelyingPartyRegistrationRepository repository,
            OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authorizationCodeAccessTokenResponseClient) throws Exception {
        return http
            // No .antMatcher() - handles all remaining requests
            .authorizeHttpRequests(auth -> auth
                .antMatchers("/", "/error").authenticated() // Main app pages require auth
                .antMatchers("/test/**").permitAll() // Security test pages folder - public access for vulnerability testing
                .antMatchers("/test-endpoint", "/csrf-info").authenticated() // Test endpoints
                .antMatchers("/saml2/metadata", "/saml2/metadata/**").permitAll() // SAML metadata should be public
                .antMatchers("/favicon.ico", "/.well-known/**").permitAll() // Allow common browser requests
                .anyRequest().denyAll() // Everything else is denied
            )
            // Enable both form login and SAML login - following Spring 5.8.16 patterns
            .formLogin(Customizer.withDefaults())
            .saml2Login(Customizer.withDefaults()) // Add SAML 2.0 support
            .oauth2Login(oauth2 -> oauth2
                .tokenEndpoint(token -> token
                    .accessTokenResponseClient(authorizationCodeAccessTokenResponseClient)
                )
            ) // Use our custom token response client with deprecated converter
            // Add our metadata filter to the chain - following Spring Security 5.7 docs
            .addFilterBefore(saml2MetadataFilter(repository, saml2MetadataResolver()), org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter.class)
            // Disable CSRF for SAML endpoints - required for SAML assertion consumer service
            .csrf(csrf -> csrf.ignoringAntMatchers("/saml2/**"))
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

    // NOTE: In Spring Security 5.7, SAML metadata endpoints are not automatically enabled
    // Unlike Spring Security 6, we need to implement the metadata serving functionality manually
    // This represents the deprecated pattern we want to migrate in our OpenRewrite recipe
} 