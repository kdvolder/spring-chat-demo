# Spring Boot Chat Demo with Security

A simple WebSocket-based chat application built with Spring Boot, demonstrating:

## Features

- **WebSocket Communication**: Real-time messaging using STOMP over WebSocket
- **Spring Security Integration**: Form-based authentication with session management
- **Multi-Chain Security**: Separate security configurations for different endpoints
- **User Authentication**: In-memory users (alice/bob) with password authentication
- **REST API**: User info endpoint (`/api/user`) with proper security
- **Real Username Display**: Shows authenticated user's name in chat and UI

## Security Architecture

### Multiple Security Filter Chains

1. **Assets Chain** (`/assets/**`): Public access for static resources
2. **WebSocket Chain** (`/ws/**`): Authenticated access for WebSocket connections  
3. **API Chain** (`/api/**`): Authenticated REST endpoints with 401 responses
4. **Main App Chain**: Form login for web pages with redirect behavior

### Authentication Methods

- **Session-based**: HTTP-only cookies for browser security
- **HTTP Basic**: Optional API access for testing/development
- **CSRF Protection**: Enabled for web forms, disabled for APIs

## Key Security Learnings

- HTTP status codes: `401 Unauthorized` vs `403 Forbidden`
- Client-side security: Never trust user-provided identity claims
- Principal injection: Server-side user identity in WebSocket handlers
- Session cookie security: HttpOnly, automatic browser inclusion

## Usage

1. **Login**: Use `alice/password` or `bob/password`
2. **Chat**: Type messages in the input field
3. **API Testing**: `curl -u alice:password http://localhost:8080/api/user`

## Technologies

- Spring Boot 2.x/3.x
- Spring Security
- Spring WebSocket/STOMP
- SockJS for WebSocket fallback
- Vanilla JavaScript frontend

---

*Note: This project was created as a learning exercise for Spring Security WebSocket integration and CSRF migration patterns.* 